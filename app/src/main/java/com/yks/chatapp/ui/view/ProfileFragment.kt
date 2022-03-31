package com.yks.chatapp.ui.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yks.chatapp.R
import com.yks.chatapp.databinding.FragmentProfileBinding
import com.yks.chatapp.model.User
import com.yks.chatapp.ui.viewmodel.ProfileViewModel
import com.yks.chatapp.utils.download
import com.yks.chatapp.utils.progressDialog
import com.yks.chatapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var progress: Dialog
    private val args: ProfileFragmentArgs by navArgs()
    private var imageUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = args.uid
        initProgressDialog()
        getProfileImgUri(uid)
        observeDownloadUrlState()
        getProfileData(uid)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.profileImg.setOnClickListener{
            imageUri?.let {
                if (it.isNotBlank()){
                    val bundle = bundleOf("uri" to it)
                    findNavController().navigate(R.id.action_profileFragment_to_zoomFragment, bundle)
                }
            }
        }

    }

    private fun getProfileImgUri(uid: String){
        viewModel.getProfileImgUri(uid)
    }

    private fun observeDownloadUrlState(){
        progress.show()
        viewModel.downloadUrl.observe(viewLifecycleOwner, { uri ->
            uri?.let {
                if (it.isNotBlank()){
                    imageUri = uri
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

    private fun getProfileData(uid: String){
        viewModel.getProfileData(uid)
            ?.addOnSuccessListener { document ->
                document?.let { documentSnapshot ->
                    val user = documentSnapshot.toObject(User::class.java)
                    val bio = user?.bio
                    val name = user?.username
                    binding.bio.text = bio.toString()
                    binding.username.text = name.toString()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}