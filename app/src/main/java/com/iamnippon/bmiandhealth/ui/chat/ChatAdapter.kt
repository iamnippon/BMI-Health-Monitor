package com.iamnippon.bmiandhealth.ui.chat

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.iamnippon.bmiandhealth.R
import com.iamnippon.bmiandhealth.databinding.ItemChatMessageBinding
import io.noties.markwon.Markwon

class ChatAdapter(
    context: Context,
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val markwon = Markwon.create(context)

    inner class ChatViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {

            val bubbleParams =
                binding.bubbleContainer.layoutParams as FrameLayout.LayoutParams

            val formattedTime = java.text.SimpleDateFormat(
                "hh:mm a",
                java.util.Locale.getDefault()
            ).format(java.util.Date(message.timestamp))

            binding.tvTime.text = formattedTime

            markwon.setMarkdown(binding.tvMessage, message.message)

            binding.tvMessage.setTextColor(
                binding.root.context.getColor(R.color.chat_text_primary)
            )

            if (message.isUser) {
                binding.tvMessage.setBackgroundResource(R.drawable.bg_user_message)
                bubbleParams.gravity = Gravity.END
                binding.tvTime.gravity = Gravity.END
            } else {
                binding.tvMessage.setBackgroundResource(R.drawable.bg_ai_message)
                bubbleParams.gravity = Gravity.START
                binding.tvTime.gravity = Gravity.START
            }

            binding.bubbleContainer.layoutParams = bubbleParams
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

}
