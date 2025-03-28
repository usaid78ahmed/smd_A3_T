package com.amnabatool.assignment_2

import android.net.Uri
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R

class MessageAdapter(
    private var messages: MutableList<Message>,
    private val isVanishMode: Boolean,
    private val onMessageLongPressed: (position: Int) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
        val messageTime: TextView = view.findViewById(R.id.messageTime)
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val messageImage: ImageView = view.findViewById(R.id.messageImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        // Show text for TEXT messages, image for IMAGE messages
        if (message.type == MessageType.TEXT) {
            holder.messageText.visibility = View.VISIBLE
            holder.messageImage.visibility = View.GONE
            holder.messageText.text = message.text
        } else if (message.type == MessageType.IMAGE) {
            holder.messageText.visibility = View.GONE
            holder.messageImage.visibility = View.VISIBLE
            holder.messageImage.setImageURI(Uri.parse(message.imageUri))
        } else {
            // Handle POST or other types if needed
        }

        holder.messageTime.text = message.time

        // Alignment: sent messages align right, received align left.
        val params = holder.messageText.layoutParams as RelativeLayout.LayoutParams
        val timeParams = holder.messageTime.layoutParams as RelativeLayout.LayoutParams
        val imageParams = holder.messageImage.layoutParams as RelativeLayout.LayoutParams

        if (message.isSentByUser) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            params.removeRule(RelativeLayout.ALIGN_PARENT_START)
            timeParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            timeParams.removeRule(RelativeLayout.ALIGN_PARENT_START)
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            imageParams.removeRule(RelativeLayout.ALIGN_PARENT_START)
            holder.profileImage.visibility = View.GONE

            if (isVanishMode) {
                holder.messageText.setBackgroundResource(R.drawable.button_background)
                holder.messageText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                )
                holder.messageTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                )
            } else {
                holder.messageText.setBackgroundResource(R.drawable.button_background1)
                holder.messageText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.black)
                )
                holder.messageTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
                )
            }
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
            params.removeRule(RelativeLayout.ALIGN_PARENT_END)
            timeParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
            timeParams.removeRule(RelativeLayout.ALIGN_PARENT_END)
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
            imageParams.removeRule(RelativeLayout.ALIGN_PARENT_END)
            holder.profileImage.visibility = View.VISIBLE

            if (isVanishMode) {
                holder.messageText.setBackgroundResource(R.drawable.button_background)
                holder.messageText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                )
                holder.messageTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                )
            } else {
                holder.messageText.setBackgroundResource(R.drawable.button_background1)
                holder.messageText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.black)
                )
                holder.messageTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
                )
            }
        }

        holder.messageText.layoutParams = params
        holder.messageTime.layoutParams = timeParams
        holder.messageImage.layoutParams = imageParams

        // Long press for edit/delete
        holder.itemView.setOnLongClickListener {
            onMessageLongPressed(position)
            true
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newList: MutableList<Message>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }
}
