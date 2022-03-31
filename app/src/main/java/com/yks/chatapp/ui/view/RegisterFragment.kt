package com.yks.chatapp.ui.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FieldValue
import com.yks.chatapp.R
import com.yks.chatapp.databinding.FragmentRegisterBinding
import com.yks.chatapp.ui.viewmodel.AuthViewModel
import com.yks.chatapp.utils.Constants.BIO_FIELD
import com.yks.chatapp.utils.Constants.CREATED_AT_FIELD
import com.yks.chatapp.utils.Constants.EMAIL_FIELD
import com.yks.chatapp.utils.Constants.PROFILE_URI_FIELD
import com.yks.chatapp.utils.Constants.UID_FIELD
import com.yks.chatapp.utils.Constants.USER_NAME_FIELD
import com.yks.chatapp.utils.progressDialog
import com.yks.chatapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var progress: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeLoginState()
        initProgressDialog()

        binding.registerBtn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val name = binding.username.text.toString()

            when {
                textUtilsIsNullOrBlank() -> requireContext().toast("please fill in all the blanks")
                binding.rePassword.text.toString() == binding.password.text.toString() -> signUp(email, password, name)
                else -> requireContext().toast("your passwords must match")
            }
        }

        binding.loginTxt.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

    }

    private fun writeUser(uid: String, username: String){
        val user = hashMapOf(
            UID_FIELD to uid,
            EMAIL_FIELD to binding.email.text.toString(),
            CREATED_AT_FIELD to FieldValue.serverTimestamp(),
            PROFILE_URI_FIELD to "",
            BIO_FIELD to "",
            USER_NAME_FIELD to username
        )
        viewModel.addUserIntoDb(uid, user).addOnSuccessListener {
            progress.dismiss()
            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
        }.addOnFailureListener {exception ->
            progress.dismiss()
            requireContext().toast(exception.message.toString())
        }
    }

    private fun loginState(){
        viewModel.loginState()
    }

    private fun observeLoginState(){
        viewModel.isLoggedIn.observe(viewLifecycleOwner,{login ->
            login?.let {
                if (it)
                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
            }
        })
    }

    private fun initProgressDialog(){
        progress = Dialog(requireContext())
        progress.progressDialog()
    }

    private fun textUtilsIsNullOrBlank(): Boolean {
        return (binding.email.text.isNullOrBlank()
                || binding.password.text.isNullOrBlank()
                || binding.username.text.isNullOrBlank())
    }

    private fun signUp(email: String, password: String, name: String){
        progress.show()
        viewModel.signUp(email, password).addOnSuccessListener {authResult ->
            authResult?.let {
                it.user?.let { user ->
                    writeUser(user.uid, name)
                }
            }
        }.addOnFailureListener { exception ->
            progress.dismiss()
            requireContext().toast(exception.message.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        loginState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}