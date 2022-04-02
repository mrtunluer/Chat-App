package com.yks.chatapp.ui.view

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yks.chatapp.databinding.FragmentCropBinding
import com.yks.chatapp.ui.viewmodel.CropViewModel
import com.yks.chatapp.utils.compress
import com.yks.chatapp.utils.progressDialog
import com.yks.chatapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception

@AndroidEntryPoint
class CropFragment : Fragment() {

    private var _binding: FragmentCropBinding? = null
    private val binding get() = _binding!!
    private val args: CropFragmentArgs by navArgs()
    private val viewModel: CropViewModel by viewModels()
    private lateinit var progress: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initProgressDialog()

        val uri = args.uri
        binding.cropImg.setImageUriAsync(uri)

        binding.cropImg.setOnCropImageCompleteListener { _, result ->
            if (result.isSuccessful){
                lifecycleScope.launch {
                    val croppedUri = result.getUriFilePath(requireContext(),true)?.toUri()
                    val file = File(croppedUri.toString())
                    val compressedImageFile = file.compress(requireContext())
                    addProfileImgIntoStorage(compressedImageFile.toUri())
                }
            }
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.doneBtn.setOnClickListener {
            progress.show()
            binding.cropImg.croppedImageAsync()
        }

    }

    private fun addProfileImgIntoStorage(uri: Uri){
        viewModel.addProfileImgIntoStorage(uri)
            ?.addOnSuccessListener { taskSnapshot ->
                taskSnapshot?.let {
                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                        uri?.let { profileUri ->
                            updateProfileUri(profileUri.toString())
                                ?.addOnSuccessListener { onSuccess() }
                                ?.addOnFailureListener {exception -> onFailure(exception) }
                        }
                    }
                }
            }
            ?.addOnFailureListener{ exception -> onFailure(exception) }
    }

    private fun onSuccess(){
        progress.dismiss()
        requireContext().toast("Saved")
        findNavController().popBackStack()
    }

    private fun onFailure(e: Exception){
        progress.dismiss()
        requireContext().toast(e.message.toString())
    }

    private fun updateProfileUri(profileUri: String) = viewModel.updateProfileUri(profileUri)

    private fun initProgressDialog(){
        progress = Dialog(requireContext())
        progress.progressDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}