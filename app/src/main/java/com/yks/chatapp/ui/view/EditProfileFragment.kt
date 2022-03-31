package com.yks.chatapp.ui.view

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yks.chatapp.R
import com.yks.chatapp.databinding.FragmentEditProfileBinding
import com.yks.chatapp.model.User
import com.yks.chatapp.ui.viewmodel.ProfileViewModel
import com.yks.chatapp.utils.*
import com.yks.chatapp.utils.Constants.BIO_FIELD
import com.yks.chatapp.utils.Constants.READ_EXTERNAL_STORAGE_PERMISSION
import com.yks.chatapp.utils.Constants.OPEN_GALLERY
import com.yks.chatapp.utils.Constants.USER_NAME_FIELD
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var progress: Dialog
    private lateinit var signOutDialog: Dialog
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bundle = bundleOf("uri" to it)
            findNavController().navigate(R.id.action_profileFragment_to_cropFragment, bundle)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        initSignOutDialog()
        initProgressDialog()
        getProfileImgUri()
        observeDownloadUrlState()
        getProfileData()

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.profileImg.setOnClickListener {
            permissionLauncher.launch(READ_EXTERNAL_STORAGE_PERMISSION)
        }

        binding.saveBtn.setOnClickListener {
            if (textUtilsIsNullOrBlank()){
                requireContext().toast("please fill in all the blanks")
            }else{
                progress.show()
                val data: HashMap<String, Any> = hashMapOf(
                    USER_NAME_FIELD to binding.username.text.toString(),
                    BIO_FIELD to binding.bio.text.toString()
                )
                updateProfileData(data)
            }
        }

        binding.signOutBtn.setOnClickListener {
            signOutDialog.show()
            val signOutTxt = signOutDialog.findViewById<TextView>(R.id.signOutTxt)
            signOutTxt.setOnClickListener {
                signOut()
                findNavController().navigate(R.id.action_profileFragment_to_registerFragment)
                signOutDialog.dismiss()
            }
        }

    }

    private fun signOut() =
        viewModel.signOut()

    private fun initSignOutDialog(){
        signOutDialog = Dialog(requireContext())
        signOutDialog.setCancelable(true)
        signOutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        signOutDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_window_bg)
        signOutDialog.setContentView(R.layout.sign_out)
    }

    private fun textUtilsIsNullOrBlank(): Boolean {
        return (binding.username.text.isNullOrBlank()
                || binding.bio.text.isNullOrBlank()
                )
    }

    private fun updateProfileData(data: HashMap<String, Any>){
        viewModel.updateProfileData(data)?.addOnSuccessListener {
            progress.dismiss()
            requireContext().toast("Updated")
        }?.addOnFailureListener { exception ->
            requireContext().toast(exception.message.toString())
        }
    }

    private fun getProfileImgUri(){
        viewModel.getProfileImgUri()
    }

    private fun observeDownloadUrlState(){
        progress.show()
        viewModel.downloadUrl.observe(viewLifecycleOwner, { uri ->
            uri?.let {
                if (it.isNotBlank()){
                    binding.profileImg.download(requireContext(), it)
                    progress.dismiss()
                }else{
                    binding.profileImg.setImageResource(R.drawable.profile)
                    progress.dismiss()
                }
            }
        })

        viewModel.downloadUrlException.observe(viewLifecycleOwner, { exception ->
            exception?.let {
                progress.dismiss()
                requireContext().toast(it.message.toString())
            }
        })
    }

    private fun getProfileData(){
        viewModel.getProfileData()
            ?.addOnSuccessListener { document ->
                document?.let { documentSnapshot ->
                    val user = documentSnapshot.toObject(User::class.java)
                    val bio = user?.bio
                    val name = user?.username
                    binding.bio.setText(bio.toString())
                    binding.username.setText(name.toString())
                } ?: requireContext().toast("Failed to fetch data")
            }
            ?.addOnFailureListener { exception ->
                requireContext().toast(exception.message.toString())
            }
    }

    private fun initProgressDialog(){
        progress = Dialog(requireContext())
        progress.progressDialog()
    }

    private fun requestPermissions(){
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) requireContext().toast("Permission needed")
            else pickImageLauncher.launch(OPEN_GALLERY)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}