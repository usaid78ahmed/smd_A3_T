package com.amnabatool.assignment_2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R

class FollowingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_11)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Navigate to FollowersActivity
        val followersText = findViewById<TextView>(R.id.followersText)
        followersText.setOnClickListener {
            val intent = Intent(this, FollowersActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Setup RecyclerView
        val followingRecyclerView = findViewById<RecyclerView>(R.id.followingRecyclerView)
        followingRecyclerView.layoutManager = LinearLayoutManager(this)

        val followingList = listOf(
            Follower("Sandra Dady", R.drawable.user_profile10),
            Follower("Marigold Gomzales", R.drawable.user_profile11),
            Follower("Imojean Swigert", R.drawable.user_profile12),
            Follower("Henry School", R.drawable.user_profile13),
            Follower("Michael Johnson", R.drawable.user_profile14),
            Follower("Sarah Connor", R.drawable.user_profile9)
        )

        val adapter = FollowersAdapter(followingList)
        followingRecyclerView.adapter = adapter
    }
}
