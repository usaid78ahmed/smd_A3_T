package com.amnabatool.assignment_2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class FollowingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_11)
        var db = FirebaseFirestore.getInstance()
        val uid = intent.getStringExtra("uid") ?: "No UID"
        val backButton = findViewById<ImageView>(R.id.backButton)
        val Profilename = findViewById<TextView>(R.id.name)
        backButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
            finish()
        }
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {


                    val name = document.getString("name")
                    Profilename.text = name ?: "No name available"




                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }

        // Navigate to FollowersActivity
        val followersText = findViewById<TextView>(R.id.followersText)
        followersText.setOnClickListener {
            val intent = Intent(this, FollowersActivity::class.java)
            intent.putExtra("uid", intent.getStringExtra("uid"))
            startActivity(intent)
            finish()
        }

        // Setup RecyclerView
        val followingRecyclerView = findViewById<RecyclerView>(R.id.followingRecyclerView)
        followingRecyclerView.layoutManager = LinearLayoutManager(this)
        val followingList = mutableListOf<Follower>()
        db.collection("users").document(uid).collection("followers").document("Followers")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val followersList = document.get("followers") as? List<*>
                    if (followersList != null) {
                        for (follower in followersList) {
                            db.collection("users").document(follower.toString())
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {
                                        val name = document.getString("name")
                                        //val profileImage = document.getString("profileImage")
                                        followingList.add(Follower(name!!, R.drawable.user_profile1))
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    // Handle any errors
                                }
                        }
                    }
                }

            }

//        val followingList = listOf(
//            Follower("Sandra Dady", R.drawable.user_profile10),
//            Follower("Marigold Gomzales", R.drawable.user_profile11),
//            Follower("Imojean Swigert", R.drawable.user_profile12),
//            Follower("Henry School", R.drawable.user_profile13),
//            Follower("Michael Johnson", R.drawable.user_profile14),
//            Follower("Sarah Connor", R.drawable.user_profile9)
//        )

        val adapter = FollowersAdapter(followingList)
        followingRecyclerView.adapter = adapter
    }
}
