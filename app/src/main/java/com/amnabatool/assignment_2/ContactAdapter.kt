package com.amnabatool.assignment_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private val contactsList: List<Contact>,
    private val isInvite: Boolean,
    private val isFollowButton: Boolean = false,
    private val onActionClick: ((Contact) -> Unit)? = null
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val nameTextView: TextView = view.findViewById(R.id.dmName)
        val chatIcon: ImageView? = view.findViewById(R.id.messageIcon)
        val actionButton: Button? = view.findViewById(R.id.inviteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = if (isInvite) {
            LayoutInflater.from(parent.context).inflate(R.layout.item_invite, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.item_follower, parent, false)
        }
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactsList[position]
        holder.profileImage.setImageResource(contact.imageResId)
        holder.nameTextView.text = contact.name

        if (isInvite) {
            // If it's an invite (follow request), button shows "Accept"
            holder.actionButton?.apply {
                visibility = View.VISIBLE
                text = if (isFollowButton) "Follow" else "Accept"
                setOnClickListener {
                    onActionClick?.invoke(contact)
                }
            }
            holder.chatIcon?.visibility = View.GONE
        } else {
            // For users in contacts, show "Follow" button
            if (isFollowButton) {
                holder.actionButton?.apply {
                    visibility = View.VISIBLE
                    text = "Follow"
                    setOnClickListener {
                        onActionClick?.invoke(contact)
                    }
                }
                holder.chatIcon?.visibility = View.GONE
            } else {
                holder.actionButton?.visibility = View.GONE
                holder.chatIcon?.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = contactsList.size
}