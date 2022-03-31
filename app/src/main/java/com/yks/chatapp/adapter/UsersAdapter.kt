package com.yks.chatapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yks.chatapp.databinding.UsersItemBinding
import com.yks.chatapp.model.User
import com.yks.chatapp.utils.download

class UsersAdapter: PagingDataAdapter<User, UsersAdapter.UserViewHolder>(Companion){

    companion object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            UsersItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val context = holder.itemView.context
        val user = getItem(position) ?: return
        holder.binding.apply {
            image.download(context, user.profile_uri)

            holder.itemView.setOnClickListener {
                onItemClickListener?.let {
                    it(user)
                }
            }

        }
    }

    inner class UserViewHolder(val binding: UsersItemBinding):
        RecyclerView.ViewHolder(binding.root)

    private var onItemClickListener: ((User) -> Unit)? = null

    fun setOnItemClickListener(listener: (User) -> Unit) { onItemClickListener = listener }

}