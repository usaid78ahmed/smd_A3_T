package com.amnabatool.assignment_2

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class StoriesAdapter(
    private var stories: List<Story>,
    private val currentUserId: String,
    private val userProfileImageUrl: String,
    private val onOwnStoryClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_OWN_STORY = 0
        private const val VIEW_TYPE_FRIEND_STORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_OWN_STORY else VIEW_TYPE_FRIEND_STORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_OWN_STORY) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_own_story, parent, false)
            OwnStoryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_story, parent, false)
            StoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_OWN_STORY) {
            (holder as OwnStoryViewHolder).bind()
        } else {
            val story = stories[position - 1] // Adjust for own story at position 0
            (holder as StoryViewHolder).bind(story)
        }
    }

    override fun getItemCount(): Int = stories.size + 1 // +1 for own story at the beginning

    inner class OwnStoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
        private val addIcon: View = itemView.findViewById(R.id.addIcon)
        private val storyIndicator: View = itemView.findViewById(R.id.storyIndicator)

        fun bind() {
            // Load the user's profile image
            Glide.with(itemView.context)
                .load(userProfileImageUrl)
                .placeholder(R.drawable.user_profile0)
                .into(profileImage)

            // Check if user has active stories
            checkAndDisplayStoryIndicator()

            itemView.setOnClickListener { onOwnStoryClick() }
        }

        private fun checkAndDisplayStoryIndicator() {
            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                .collection("stories")
                .whereGreaterThan("timestamp", System.currentTimeMillis() - 24 * 60 * 60 * 1000)
                .get()
                .addOnSuccessListener { documents ->
                    storyIndicator.visibility = if (!documents.isEmpty) View.VISIBLE else View.GONE
                }
        }
    }

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.storyProfileImage)
        private val username: TextView = itemView.findViewById(R.id.storyUsername)

        fun bind(story: Story) {
            Glide.with(itemView.context)
                .load(story.profileImageUrl)
                .placeholder(R.drawable.user_profile0)
                .into(profileImage)

            username.text = story.username

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, StoryViewActivity::class.java).apply {
                    putExtra("uid", story.userId)
                    putExtra("username", story.username)
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    fun updateStories(newStories: List<Story>) {
        stories = newStories
        notifyDataSetChanged()
    }
}