package com.amnabatool.assignment_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment_1.R

class FollowersAdapter(
    private val followersList: List<Follower>
) : RecyclerView.Adapter<FollowersAdapter.FollowerViewHolder>() {

    class FollowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val nameText: TextView = itemView.findViewById(R.id.dmName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_follower, parent, false)
        return FollowerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        val follower = followersList[position]
        holder.nameText.text = follower.name
        holder.profileImage.setImageResource(follower.profileImageResId)
    }

    override fun getItemCount(): Int {
        return followersList.size
    }
}
