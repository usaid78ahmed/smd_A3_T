package com.amnabatool.assignment_2

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostsAdapter(
    private val posts: MutableList<Post>,
    private val userProfileImageUrl: String,
    private val userName: String,
    private val currentUserId: String
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userProfileImage: ImageView = itemView.findViewById(R.id.userProfileImage)
        val userNameTextView: TextView = itemView.findViewById(R.id.userName)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val userNameCaption: TextView = itemView.findViewById(R.id.userNameCaption)
        val postCaption: TextView = itemView.findViewById(R.id.postCaption)
        val commentButton: ImageView = itemView.findViewById(R.id.commentIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Set user profile image
        if (userProfileImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(userProfileImageUrl)
                .into(holder.userProfileImage)
        } else {
            holder.userProfileImage.setImageResource(R.drawable.default_image)
        }

        // Set username
        holder.userNameTextView.text = userName
        holder.userNameCaption.text = userName

        // Set post image
        if (post.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .into(holder.postImage)
        }

        // Set caption
        holder.postCaption.text = post.caption

        // Set click listener for comment button
        holder.commentButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, CommentsActivity::class.java).apply {
                putExtra("postId", post.postId)
                putExtra("postUserId", post.userId)
                putExtra("currentUserId", currentUserId)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }
}