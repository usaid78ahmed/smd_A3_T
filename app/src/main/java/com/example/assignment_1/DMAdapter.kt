package com.example.assignment_1

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DMAdapter(private val dmList: List<DMUser>) :
    RecyclerView.Adapter<DMAdapter.DMViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DMViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dm, parent, false)
        return DMViewHolder(view)
    }

    override fun onBindViewHolder(holder: DMViewHolder, position: Int) {
        val user = dmList[position]
        holder.name.text = user.name
        holder.profileImage.setImageResource(user.imageRes)

        // Handle Click Event to Open Chat Screen
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("USERNAME", user.name)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = dmList.size

    class DMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.dmName)
        val profileImage: ImageView = itemView.findViewById(R.id.dmProfileImage)
    }
}


data class DMUser(val name:String, val imageRes:Int)