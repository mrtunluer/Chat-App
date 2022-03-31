package com.yks.chatapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yks.chatapp.databinding.ChatItemReceivedBinding
import com.yks.chatapp.databinding.ChatItemReceivedImgBinding
import com.yks.chatapp.databinding.ChatItemSentBinding
import com.yks.chatapp.databinding.ChatItemSentImgBinding
import com.yks.chatapp.model.Chat
import com.yks.chatapp.utils.Constants.RECEIVED_IMG_VIEW_TYPE
import com.yks.chatapp.utils.Constants.RECEIVED_VIEW_TYPE
import com.yks.chatapp.utils.Constants.SENT_IMG_VIEW_TYPE
import com.yks.chatapp.utils.Constants.SENT_VIEW_TYPE
import com.yks.chatapp.utils.download

class ChatAdapter(private val receiverId: String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
    private val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list: List<Chat>) {
        differ.submitList(list)
    }

    private fun getItem(position: Int): Chat {
        return differ.currentList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            SENT_VIEW_TYPE -> {
                return SentViewHolder(
                    ChatItemSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            RECEIVED_VIEW_TYPE -> {
                return ReceivedViewHolder(
                    ChatItemReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            SENT_IMG_VIEW_TYPE -> {
                return SentImgViewHolder(
                    ChatItemSentImgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            else -> {
                return ReceivedImgViewHolder(
                    ChatItemReceivedImgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val context = holder.itemView.context
        val item = getItem(position)

        when {
            getItemViewType(position) == SENT_VIEW_TYPE -> {
                val sentViewHolder = holder as SentViewHolder
                sentViewHolder.setData(item)
            }
            getItemViewType(position) == RECEIVED_VIEW_TYPE -> {
                val receivedViewHolder = holder as ReceivedViewHolder
                receivedViewHolder.setData(item)
            }
            getItemViewType(position) == SENT_IMG_VIEW_TYPE -> {
                val sentImgViewHolder = holder as SentImgViewHolder
                sentImgViewHolder.setData(item, context)
            }
            else -> {
                val receivedImgViewHolder = holder as ReceivedImgViewHolder
                receivedImgViewHolder.setData(item, context)
            }
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(item)
            }
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).receiver_id == receiverId && getItem(position).image_uri.isEmpty()){
            SENT_VIEW_TYPE
        }else if(getItem(position).receiver_id != receiverId && getItem(position).image_uri.isEmpty()){
            RECEIVED_VIEW_TYPE
        }else if (getItem(position).receiver_id == receiverId && getItem(position).image_uri.isNotEmpty()){
            SENT_IMG_VIEW_TYPE
        }else{
            RECEIVED_IMG_VIEW_TYPE
        }
    }

    inner class ReceivedImgViewHolder(
        private val binding: ChatItemReceivedImgBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun setData(chat: Chat, context: Context){
            binding.messageTxt.text = chat.message
            binding.image.download(context, chat.image_uri)
        }
    }

    inner class SentImgViewHolder(
        private val binding: ChatItemSentImgBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun setData(chat: Chat, context: Context){
            binding.messageTxt.text = chat.message
            binding.image.download(context, chat.image_uri)
        }
    }

    inner class SentViewHolder(
        private val binding: ChatItemSentBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun setData(chat: Chat){
            binding.messageTxt.text = chat.message
        }
    }

    inner class ReceivedViewHolder(
        private val binding: ChatItemReceivedBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun setData(chat: Chat){
            binding.messageTxt.text = chat.message
        }
    }

    private var onItemClickListener: ((Chat) -> Unit)? = null

    fun setOnItemClickListener(listener: (Chat) -> Unit) {
        onItemClickListener = listener
    }
}