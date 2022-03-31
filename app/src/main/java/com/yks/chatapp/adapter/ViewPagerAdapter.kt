package com.yks.chatapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yks.chatapp.ui.view.ChatsFragment
import com.yks.chatapp.ui.view.UsersFragment
import com.yks.chatapp.utils.Constants.NUM_TABS

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UsersFragment()
            else -> ChatsFragment()
        }
    }
}