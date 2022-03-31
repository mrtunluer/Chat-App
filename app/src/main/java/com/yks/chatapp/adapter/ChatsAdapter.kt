package com.yks.chatapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.yks.chatapp.R
import com.yks.chatapp.databinding.ChatsItemBinding
import com.yks.chatapp.model.Conversation
import com.yks.chatapp.model.User
import com.yks.chatapp.ui.viewmodel.ChatsViewModel
import com.yks.chatapp.utils.Constants.DATE_PATTERN
import com.yks.chatapp.utils.State
import com.yks.chatapp.utils.download
import com.yks.chatapp.utils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter(private val viewModel: ChatsViewModel): RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list: List<Conversation>) {
        differ.submitList(list)
    }

    private fun getItem(position: Int): Conversation {
        return differ.currentList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ChatsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val conversation = getItem(position)

        viewModel.viewModelScope.launch {
            getUserData(conversation, holder, context)
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private suspend fun getUserData(conversation: Conversation, holder: ViewHolder, context: Context){
        viewModel.getUserData(conversation.id).collectLatest {
            if (it is State.Success)
                holder.bind(conversation, it.data, context)
            else if (it is State.Failed)
                context.toast(it.message)
        }
    }

    inner class ViewHolder(val binding: ChatsItemBinding):
        RecyclerView.ViewHolder(binding.root){

        fun bind(conversation: Conversation, user: User, context: Context){

            if (user.profile_uri.isNotBlank())
                binding.profImg.download(context, user.profile_uri)
            else
                binding.profImg.setImageResource(R.drawable.profile)

            binding.usernameTxt.text = user.username

            if (conversation.image_uri.isNotBlank())
                binding.lastMessageTxt.text = context.getString(R.string.image)
            else
                binding.lastMessageTxt.text = conversation.message

            conversation.created_at?.let {
                binding.dateTxt.text = convertDate(it)
            }

            itemView.setOnClickListener {
                onItemClickListener?.let {
                    it(conversation)
                }
            }

        }

    }

    private fun convertDate (time: Timestamp): String {
        val date = time.toDate()
        val dateFormat = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        return dateFormat.format(date)
    }

    private var onItemClickListener: ((Conversation) -> Unit)? = null

    fun setOnItemClickListener(listener: (Conversation) -> Unit) { onItemClickListener = listener }

}