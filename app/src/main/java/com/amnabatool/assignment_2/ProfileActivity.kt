package com.amnabatool.assignment_2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_9)
        val db = FirebaseFirestore.getInstance()

        val uid = intent.getStringExtra("uid") ?: "No UID"

        val profileImage = findViewById<ImageView>(R.id.profileImage)
        val nameText = findViewById<TextView>(R.id.profileName)
        val bioText = findViewById<TextView>(R.id.profileBio)
        val followersCount = findViewById<TextView>(R.id.followersCount)
        val followingCount = findViewById<TextView>(R.id.followingCount)
        val postsCount = findViewById<TextView>(R.id.postsCount)


        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val profileImageID = document.getString("imageID")
                    val imageID = document.getString("imageID")

                    val name = document.getString("name")
                    nameText.text = name ?: "No name available"
                    val bio = document.getString("bio")
                    bioText.text = bio ?: "No bio available"

                    if (!imageID.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageID)
                            .into(profileImage)
                    } else {
                        profileImage.setImageResource(R.drawable.default_image)
                    }

                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }


        db.collection("users").document(uid)
            .collection("followers")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot.documents.filter { it.id != "dummy" }.size
                followersCount.text = count.toString()
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }

        db.collection("users").document(uid)
            .collection("following")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot.documents.filter { it.id != "dummy" }.size
                followingCount.text = count.toString()
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }


        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)


        loadPosts(uid, db, postsCount)


        val editProfileButton = findViewById<ImageView>(R.id.editProfileButton)
        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        val followersText = findViewById<TextView>(R.id.followersText)
        followersText.setOnClickListener {
            val intent = Intent(this, FollowersActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        val followingText = findViewById<TextView>(R.id.followingText)
        followingText.setOnClickListener {
            val intent = Intent(this, FollowingActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val navIntent = Intent(this, HomeActivity::class.java)
                    navIntent.putExtra("uid", uid)
                    startActivity(navIntent)
                    finish()
                    true
                }
                R.id.nav_search -> {
                    val navIntent = Intent(this, SearchActivity::class.java)
                    navIntent.putExtra("uid", uid)
                    startActivity(navIntent)
                    finish()
                    true
                }
                R.id.nav_contacts -> {
                    val contactsIntent = Intent(this, ContactsActivity::class.java)
                    contactsIntent.putExtra("uid", uid)
                    startActivity(contactsIntent)
                    finish()
                    true
                }
                R.id.nav_add -> {
                    val newPostIntent = Intent(this, NewPostActivity::class.java)
                    newPostIntent.putExtra("uid", uid)
                    startActivity(newPostIntent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // Function to load posts from Firestore
    private fun loadPosts(uid: String, db: FirebaseFirestore, postsCount: TextView) {
        db.collection("users").document(uid)
            .collection("posts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val postsList = mutableListOf<String>()
                if (querySnapshot.isEmpty) {
                    val dummyData = hashMapOf("isDummy" to true)
                    db.collection("users").document(uid)
                        .collection("posts")
                        .document("dummy")
                        .set(dummyData)
                        .addOnSuccessListener {
                            postsCount.text = "0"
                            postAdapter = PostAdapter(postsList)
                            postsRecyclerView.adapter = postAdapter
                        }
                        .addOnFailureListener { e ->
                            // Handle error if needed
                        }
                } else {
                    querySnapshot.documents.filter { it.id != "dummy" }.forEach { document ->
                        val imageID = document.getString("imageID")
                        if (!imageID.isNullOrEmpty()) {
                            // Directly add the URI string
                            postsList.add(imageID)
                        }
                    }
                    postsCount.text = postsList.size.toString()
                    postAdapter = PostAdapter(postsList)
                    postsRecyclerView.adapter = postAdapter
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors (e.g., show a message or log the error)
            }
    }

}
