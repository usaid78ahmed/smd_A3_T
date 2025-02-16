package com.example.assignment_1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(
    private val messages: List<Message>,
    private val isVanishMode: Boolean
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
        val messageTime: TextView = view.findViewById(R.id.messageTime)
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val messageContainer: RelativeLayout? =
            view.findViewById<TextView>(R.id.messageText).parent as? RelativeLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text
        holder.messageTime.text = message.time

        val params = holder.messageText.layoutParams as RelativeLayout.LayoutParams
        val timeParams = holder.messageTime.layoutParams as RelativeLayout.LayoutParams

        if (message.isSentByUser) {
            // Sent message (Align to Right)
            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            params.removeRule(RelativeLayout.ALIGN_PARENT_START)
            holder.profileImage.visibility = View.GONE

            timeParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            timeParams.removeRule(RelativeLayout.ALIGN_PARENT_START)

            if (isVanishMode) {
                // vanish mode
                holder.messageText.setBackgroundResource(R.drawable.button_background)
                holder.messageText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                )
                holder.messageTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                )
            } else {
                // Normal mode for sent messages
                holder.messageText.setBackgroundResource(R.drawable.button_background1)
                holder.messageText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.black)
                )
                holder.messageTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
                )
            }
        } else {
            // Received message (Align to Left)
            params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
            params.removeRule(RelativeLayout.ALIGN_PARENT_END)
            holder.profileImage.visibility = View.VISIBLE

            timeParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
            timeParams.removeRule(RelativeLayout.ALIGN_PARENT_END)

            if (isVanishMode) {
                // vanish mode
                holder.messageText.setBackgroundResource(R.drawable.button_background)
                holder.messageText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                )
                holder.messageTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                )
            } else {
                // normal mode
                holder.messageText.setBackgroundResource(R.drawable.button_background1)
                holder.messageText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.black)
                )
                holder.messageTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
                )
            }
        }

        // Reassign updated layout params
        holder.messageText.layoutParams = params
        holder.messageTime.layoutParams = timeParams
    }

    override fun getItemCount(): Int = messages.size
}
