package com.yks.chatapp.ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yks.chatapp.R
import com.yks.chatapp.adapter.ChatsAdapter
import com.yks.chatapp.databinding.FragmentChatsBinding
import com.yks.chatapp.model.Conversation
import com.yks.chatapp.ui.viewmodel.ChatsViewModel
import com.yks.chatapp.utils.Constants.UID_FIELD
import com.yks.chatapp.utils.State
import com.yks.chatapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatsAdapter: ChatsAdapter
    private val viewModel: ChatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        getChats()

        chatsAdapter.setOnItemClickListener { conversation ->
            val bundle = bundleOf(
                UID_FIELD to conversation.id
            )
            findNavController().navigate(R.id.action_homeFragment_to_chatFragment, bundle)
        }

    }

    private fun getChats(){
        lifecycleScope.launch {
            viewModel.getChats().collectLatest {
                when(it){
                    is State.Success -> onSuccess(it.data)
                    is State.Failed -> onFailed(it.message)
                    else -> binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun onSuccess(list: List<Conversation>){
        chatsAdapter.submitList(list)
        binding.progressBar.visibility =View.GONE
    }

    private fun onFailed(message: String){
        requireContext().toast(message)
        binding.progressBar.visibility =View.GONE
    }

    private fun initAdapter(){
        chatsAdapter = ChatsAdapter(viewModel)
        binding.recyclerView.apply {
            adapter = chatsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}