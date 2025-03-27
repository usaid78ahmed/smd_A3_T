package com.amnabatool.assignment_1


import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment_1.R

class FollowersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_10)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Navigate to FollowingActivity
        val followingText = findViewById<TextView>(R.id.followingText)
        followingText.setOnClickListener {
            val intent = Intent(this, FollowingActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Setup RecyclerView
        val followersRecyclerView = findViewById<RecyclerView>(R.id.followersRecyclerView)
        followersRecyclerView.layoutManager = LinearLayoutManager(this)

        val followersList = listOf(
            Follower("Alfredo Lipshutz", R.drawable.user_profile1),
            Follower("Emily James", R.drawable.user_profile2),
            Follower("Lily Thomas", R.drawable.user_profile3),
            Follower("Christopher", R.drawable.user_profile4),
            Follower("Amy Wesley", R.drawable.user_profile5),
            Follower("Laura Ryan", R.drawable.user_profile6),
            Follower("Sandra Dady", R.drawable.user_profile7),
            Follower("Marigold Gomzales", R.drawable.user_profile8),
            Follower("Imojean Swigert", R.drawable.user_profile9)
        )

        val adapter = FollowersAdapter(followersList)
        followersRecyclerView.adapter = adapter
    }
}
