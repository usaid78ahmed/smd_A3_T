package com.amnabatool.assignment_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R
import com.bumptech.glide.Glide

class PostAdapter(private val postList: List<String>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postImage: ImageView = view.findViewById(R.id.postImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_posts, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        try {
            Glide.with(holder.itemView.context)
                .load(postList[position])
                .error(R.drawable.default_image) // fallback image if loading fails
                .into(holder.postImage)
        } catch (e: SecurityException) {
            e.printStackTrace()
            holder.postImage.setImageResource(R.drawable.default_image)
        }
    }


    override fun getItemCount(): Int = postList.size
}
