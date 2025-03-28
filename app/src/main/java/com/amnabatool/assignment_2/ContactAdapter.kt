package com.amnabatool.assignment_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R

class ContactAdapter(
    private val contactsList: List<Contact>,
    private val isInvite: Boolean, // Flag to determine type
    private val onInviteClick: ((Contact) -> Unit)? = null // Click listener for invites
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val nameTextView: TextView = view.findViewById(R.id.dmName)
        val chatIcon: ImageView? = view.findViewById(R.id.messageIcon) // Only for contacts
        val inviteButton: Button? = view.findViewById(R.id.inviteButton) // Only for invites
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
            holder.inviteButton?.setOnClickListener {
                onInviteClick?.invoke(contact)
            }
        } else {
            holder.chatIcon?.setOnClickListener {
                // Handle chat icon click
            }
        }
    }

    override fun getItemCount() = contactsList.size
}
