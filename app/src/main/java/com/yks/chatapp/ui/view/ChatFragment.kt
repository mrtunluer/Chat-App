package com.yks.chatapp.ui.view

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.canhub.cropper.CropImageView
import com.google.firebase.firestore.FieldValue
import com.yks.chatapp.R
import com.yks.chatapp.adapter.ChatAdapter
import com.yks.chatapp.databinding.FragmentChatBinding
import com.yks.chatapp.model.Chat
import com.yks.chatapp.model.User
import com.yks.chatapp.ui.viewmodel.ChatViewModel
import com.yks.chatapp.utils.*
import com.yks.chatapp.utils.Constants.CREATED_AT_FIELD
import com.yks.chatapp.utils.Constants.ID_FIELD
import com.yks.chatapp.utils.Constants.IMAGE_URI_FIELD
import com.yks.chatapp.utils.Constants.MESSAGE_FIELD
import com.yks.chatapp.utils.Constants.RECEIVER_ID_FIELD
import com.yks.chatapp.utils.Constants.SENDER_ID_FIELD
import com.yks.chatapp.utils.Constants.UID_FIELD
import com.yks.chatapp.utils.Constants.USERS_FIELD
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var progress: Dialog

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            whenSelectImg(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = args.uid // uid of the person I'm chatting with
        initProgressDialog()
        requestPermissions()
        initAdapter(uid)
        getProfile(uid)
        getMessages(uid)

        binding.sendImageLayout.cropImg.setOnCropImageCompleteListener { _, result ->
            if (result.isSuccessful){
                cropImgListener(result, uid)
            }
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.sendBtn.setOnClickListener {
            if (!binding.messageTxt.text.isNullOrBlank()){
                viewModel.mUid?.let {
                    val message = binding.messageTxt.text.toString()
                    binding.messageTxt.setText("")
                    sendMessage(it, uid, message)
                }
            }
        }

        binding.galleryImg.setOnClickListener {
            permissionLauncher.launch(Constants.READ_EXTERNAL_STORAGE_PERMISSION)
        }

        binding.toolbar.setOnClickListener {
            val bundle = bundleOf(UID_FIELD to uid)
            findNavController().navigate(R.id.action_chatFragment_to_profileFragment, bundle)
        }

        binding.sendImageLayout.doneBtn.setOnClickListener {
            progress.show()
            binding.sendImageLayout.cropImg.croppedImageAsync()
        }

        binding.sendImageLayout.backBtn.setOnClickListener {
            binding.sendImageLayout.root.visibility = View.GONE
        }

        chatAdapter.setOnItemClickListener { chat ->
            if (chat.image_uri.isNotEmpty()){
                val bundle = bundleOf("uri" to chat.image_uri)
                findNavController().navigate(R.id.action_chatFragment_to_zoomFragment, bundle)
            }
        }

    }

    private fun whenSelectImg(uri: Uri){
        binding.sendImageLayout.cropImg.setImageUriAsync(uri)
        binding.sendImageLayout.messageTxt.setText(binding.messageTxt.text.toString())
        binding.sendImageLayout.root.visibility = View.VISIBLE
    }

    private fun cropImgListener(result: CropImageView.CropResult, uid: String){
        lifecycleScope.launch {
            val croppedUri = result.getUriFilePath(requireContext(),true)?.toUri()
            val file = File(croppedUri.toString())
            val compressedImageFile = file.compress(requireContext())
            addChatImgIntoStorage(compressedImageFile.toUri(), uid)
        }
    }

    private fun addChatImgIntoStorage(uri: Uri, uid: String){
        viewModel.addChatImgIntoStorage(uri)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { uri ->
                    uri?.let { sendImgUri ->
                        sendImage(viewModel.mUid, uid, sendImgUri.toString())
                    }
                }.addOnFailureListener { exception -> onFailedForSendImg(exception.message.toString()) }
            }.addOnFailureListener { exception -> onFailedForSendImg(exception.message.toString()) }
    }

    private fun onFailedForSendImg(exception: String){
        progress.dismiss()
        binding.sendImageLayout.root.visibility = View.GONE
        binding.sendImageLayout.messageTxt.setText("")
        requireContext().toast(exception)
    }

    private fun onSuccessForSendImg(){
        progress.dismiss()
        binding.sendImageLayout.root.visibility = View.GONE
        binding.sendImageLayout.messageTxt.setText("")
        binding.messageTxt.setText("")
    }

    private fun sendImage(senderId: String?, receiverId: String, imageUri: String){
        if (senderId != null){
            val uuid = UUID.randomUUID().toString()
            val data = hashMapOf(
                ID_FIELD to uuid,
                IMAGE_URI_FIELD to imageUri,
                USERS_FIELD to mutableListOf(senderId.plus(receiverId), receiverId.plus(senderId)),
                SENDER_ID_FIELD to senderId,
                RECEIVER_ID_FIELD to receiverId,
                MESSAGE_FIELD to binding.sendImageLayout.messageTxt.text.toString(),
                CREATED_AT_FIELD to FieldValue.serverTimestamp()
            )
            viewModel.addMessage(uuid, data)
                .addOnSuccessListener {
                    onSuccessForSendImg()

                    val dataToSender = hashMapOf(
                        "id" to receiverId,
                        "created_at" to FieldValue.serverTimestamp(),
                        "message" to "",
                        IMAGE_URI_FIELD to imageUri
                    )

                    val dataToReceiver = hashMapOf(
                        "id" to senderId,
                        "created_at" to FieldValue.serverTimestamp(),
                        "message" to "",
                        IMAGE_URI_FIELD to imageUri
                    )

                    addConversation(
                        senderId,
                        dataToSender,
                        receiverId,
                        dataToReceiver)

                }
                .addOnFailureListener { onFailedForSendImg(it.message.toString()) }
        }
    }

    private fun addConversation(
        senderId: String
        ,dataToSender: HashMap<String,Any>
        ,receiverId: String
        ,dataToReceiver: HashMap<String,Any>
    ){
        val myConversation = viewModel.senderConversation(
            senderId
            ,receiverId
        )
        val receiverConversation = viewModel.receiverConversation(
            senderId
            ,receiverId
        )
        viewModel.runBatchForConversation(
            myConversation,
            dataToSender,
            receiverConversation,
            dataToReceiver
        )
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String){
        val uuid = UUID.randomUUID().toString()
        val data = hashMapOf(
            ID_FIELD to uuid,
            IMAGE_URI_FIELD to "",
            USERS_FIELD to mutableListOf(senderId.plus(receiverId), receiverId.plus(senderId)),
            SENDER_ID_FIELD to senderId,
            RECEIVER_ID_FIELD to receiverId,
            MESSAGE_FIELD to message,
            CREATED_AT_FIELD to FieldValue.serverTimestamp()
        )
        viewModel.addMessage(uuid, data)
            .addOnSuccessListener {
                val dataToSender = hashMapOf(
                    "id" to receiverId,
                    "created_at" to FieldValue.serverTimestamp(),
                    "message" to message,
                    IMAGE_URI_FIELD to ""
                )

                val dataToReceiver = hashMapOf(
                    "id" to senderId,
                    "created_at" to FieldValue.serverTimestamp(),
                    "message" to message,
                    IMAGE_URI_FIELD to ""
                )

                addConversation(
                    senderId,
                    dataToSender,
                    receiverId,
                    dataToReceiver)
            }
            .addOnFailureListener { requireContext().toast(it.message.toString()) }
    }

    private fun getMessages(uid: String){
        lifecycleScope.launch {
            viewModel.getMessages(uid).collectLatest {
                when (it) {
                    is State.Success -> onSuccessForChat(it.data)
                    is State.Failed -> onFailed(it.message)
                    else -> binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getProfile(uid: String) {
        lifecycleScope.launch {
            viewModel.getProfile(uid).collect {
                when (it) {
                    is State.Success ->  onSuccessForProfile(it.data)
                    is State.Failed ->  onFailed(it.message)
                    else -> binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun initAdapter(receiverId: String){
        chatAdapter = ChatAdapter(receiverId)
        linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
        binding.recyclerView.apply {
            adapter = chatAdapter
            layoutManager = linearLayoutManager
        }
    }

    private fun onSuccessForChat(list: List<Chat>){
        binding.progressBar.visibility = View.GONE
        chatAdapter.submitList(list)
        binding.recyclerView.smoothScrollToPosition(0)
    }

    private fun onSuccessForProfile(user: User){
        binding.progressBar.visibility = View.GONE
        binding.profileImg.download(requireContext(), user.profile_uri)
        binding.usernameTxt.text = user.username
    }

    private fun onFailed(message: String){
        binding.progressBar.visibility = View.GONE
        requireContext().toast(message)
    }

    private fun requestPermissions(){
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) requireContext().toast("Permission needed")
            else pickImageLauncher.launch(Constants.OPEN_GALLERY)
        }
    }

    private fun initProgressDialog(){
        progress = Dialog(requireContext())
        progress.progressDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}