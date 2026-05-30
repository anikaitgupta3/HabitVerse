package com.anikaitgupta.habitverse.ui

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anikaitgupta.habitverse.databinding.ItemChatMessageBinding
import com.anikaitgupta.habitverse.domain.ChatMessage
import com.google.android.material.color.MaterialColors
import com.google.firebase.crashlytics.FirebaseCrashlytics

class TipsAdapter(private val showToast:()->Unit,private val onSpeakClick: (String) -> Unit) : ListAdapter<ChatMessage, TipsAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.text
            binding.tvRole.text = message.role

            val isUser = message.role == "user"
            
            // Set Alignment
            val layoutParams = binding.messageCard.layoutParams as LinearLayout.LayoutParams
            val roleParams = binding.tvRole.layoutParams as LinearLayout.LayoutParams
            
            if (isUser) {
                layoutParams.gravity = Gravity.END
                roleParams.gravity = Gravity.END
                
                val primaryColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorPrimaryFixed)
                val containerColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSecondaryContainer)
                
                binding.tvRole.setTextColor(primaryColor)
                binding.messageCard.setCardBackgroundColor(containerColor)
                binding.messageCard.strokeColor = primaryColor
            } else {
                layoutParams.gravity = Gravity.START
                roleParams.gravity = Gravity.START
                
                val secondaryColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSecondary)
                val surfaceColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurface)
                val outlineColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOutlineVariant)

                binding.tvRole.setTextColor(secondaryColor)
                binding.messageCard.setCardBackgroundColor(surfaceColor)
                binding.messageCard.strokeColor = outlineColor
            }
            
            binding.messageCard.layoutParams = layoutParams
            binding.tvRole.layoutParams = roleParams
            binding.speakAndPauseButton.setOnClickListener {
                onSpeakClick(message.text)
            }
            binding.flagButton.setOnClickListener {
                val flaggedText = message.text
                FirebaseCrashlytics.getInstance().recordException(
                    Exception("User Flagged AI Response: $flaggedText")
                )
                showToast()
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.text == newItem.text && oldItem.role == newItem.role
        }
    }
}