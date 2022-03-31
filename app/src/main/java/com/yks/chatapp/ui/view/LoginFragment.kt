package com.yks.chatapp.ui.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yks.chatapp.R
import com.yks.chatapp.databinding.FragmentLoginBinding
import com.yks.chatapp.ui.viewmodel.AuthViewModel
import com.yks.chatapp.utils.progressDialog
import com.yks.chatapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var progress: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initProgressDialog()

        binding.loginBtn.setOnClickListener {
            if (textUtilsIsNullOrBlank())
                requireContext().toast("please fill in all the blanks")
            else
                signIn(binding.email.text.toString(), binding.password.text.toString())
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun initProgressDialog(){
        progress = Dialog(requireContext())
        progress.progressDialog()
    }

    private fun textUtilsIsNullOrBlank(): Boolean {
        return (binding.email.text.isNullOrBlank()
                || binding.password.text.isNullOrBlank())
    }

    private fun signIn(email: String, password: String){
        progress.show()
        viewModel.signIn(email, password).addOnSuccessListener {
            progress.dismiss()
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }.addOnFailureListener {
            progress.dismiss()
            requireContext().toast(it.message.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}