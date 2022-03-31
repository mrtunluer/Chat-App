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
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.yks.chatapp.R
import com.yks.chatapp.adapter.UsersAdapter
import com.yks.chatapp.databinding.FragmentUsersBinding
import com.yks.chatapp.ui.viewmodel.UsersViewModel
import com.yks.chatapp.utils.Constants.SPAN_COUNT
import com.yks.chatapp.utils.Constants.UID_FIELD
import com.yks.chatapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UsersViewModel by viewModels()
    private lateinit var usersAdapter: UsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        getUsers()
        loadStateListener()

        binding.swipeRefreshLayout.setOnRefreshListener {
            usersAdapter.refresh()
        }

        usersAdapter.setOnItemClickListener { user ->
            val bundle = bundleOf(
                UID_FIELD to user.uid
            )
            findNavController().navigate(R.id.action_homeFragment_to_chatFragment, bundle)
        }

    }

    private fun visibilityOfViewComponent(it: CombinedLoadStates){
        binding.swipeRefreshLayout.isRefreshing = it.source.refresh is LoadState.Loading
        if (it.source.refresh is LoadState.Error)
            requireContext().toast("Failed to fetch data")
        }

    private fun loadStateListener(){
        usersAdapter.addLoadStateListener {
            visibilityOfViewComponent(it)
        }
    }

    private fun initAdapter(){
        usersAdapter = UsersAdapter()
        binding.recyclerView.apply {
            adapter = usersAdapter
            layoutManager = StaggeredGridLayoutManager(SPAN_COUNT, LinearLayoutManager.VERTICAL)
        }
    }

    private fun getUsers(){
        lifecycleScope.launch {
            viewModel.users.collectLatest {
                usersAdapter.submitData(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}