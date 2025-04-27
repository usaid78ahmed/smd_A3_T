package com.amnabatool.assignment_2


import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R
import com.google.firebase.firestore.FirebaseFirestore

class FollowersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_10)
        val Profilename = findViewById<TextView>(R.id.name)
        var db = FirebaseFirestore.getInstance()
        val uid = intent.getStringExtra("uid") ?: "No UID"
        val followList = mutableListOf<Follower>()

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
                                        followList.add(Follower(name!!, R.drawable.user_profile1))
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    // Handle any errors
                                }
                        }
                }
            }

            }


        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
            finish()
        }

        // Navigate to FollowingActivity
        val followingText = findViewById<TextView>(R.id.followingText)
        followingText.setOnClickListener {
            val intent = Intent(this, FollowingActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
            finish()
        }

        // Setup RecyclerView
        val followersRecyclerView = findViewById<RecyclerView>(R.id.followersRecyclerView)
        followersRecyclerView.layoutManager = LinearLayoutManager(this)

//        val followersList = listOf(
//            Follower("Alfredo Lipshutz", R.drawable.user_profile1),
//            Follower("Emily James", R.drawable.user_profile2),
//            Follower("Lily Thomas", R.drawable.user_profile3),
//            Follower("Christopher", R.drawable.user_profile4),
//            Follower("Amy Wesley", R.drawable.user_profile5),
//            Follower("Laura Ryan", R.drawable.user_profile6),
//            Follower("Sandra Dady", R.drawable.user_profile7),
//            Follower("Marigold Gomzales", R.drawable.user_profile8),
//            Follower("Imojean Swigert", R.drawable.user_profile9)
//        )

        val adapter = FollowersAdapter(followList)
        followersRecyclerView.adapter = adapter
    }
}
