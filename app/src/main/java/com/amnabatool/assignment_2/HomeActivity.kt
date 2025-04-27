package com.amnabatool.assignment_2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeActivity : AppCompatActivity() {
    private val TAG = "HomeActivity"
    private lateinit var db: FirebaseFirestore
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var storiesRecyclerView: RecyclerView
    private lateinit var storiesAdapter: StoriesAdapter
    private val postsList = mutableListOf<Post>()
    private val storiesList = mutableListOf<Story>()
    private var userName: String = ""
    private var userProfileImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()

        setContentView(R.layout.activity_3)

        val uid = intent.getStringExtra("uid") ?: "No UID"
        Log.d(TAG, "Received UID: $uid")

        // Initialize RecyclerViews
        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsAdapter = PostsAdapter(postsList, userProfileImageUrl, userName, uid)
        postsRecyclerView.adapter = postsAdapter

        storiesRecyclerView = findViewById(R.id.storiesRecyclerView)
        storiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        storiesAdapter = StoriesAdapter(storiesList, uid, userProfileImageUrl) {
            // Handle own story click
            handleOwnStoryClick(uid)
        }
        storiesRecyclerView.adapter = storiesAdapter

        // First get user info
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val profileImageID = document.getString("imageID").toString()
                    userProfileImageUrl = profileImageID
                    userName = document.getString("username") ?: "Unknown User"

                    // Update adapters with user info
                    postsAdapter = PostsAdapter(postsList, userProfileImageUrl, userName, uid)
                    postsRecyclerView.adapter = postsAdapter

                    storiesAdapter = StoriesAdapter(storiesList, uid, userProfileImageUrl) {
                        handleOwnStoryClick(uid)
                    }
                    storiesRecyclerView.adapter = storiesAdapter

                    // Load user's posts
                    loadUserPosts(uid)

                    // Load stories from followers
                    loadStoriesFromFollowers(uid)

                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        setupNavigation(uid)
    }

    private fun setupNavigation(uid: String) {
        // Navigate to DMActivity
        val dmicon = findViewById<ImageView>(R.id.dmicon)
        dmicon.setOnClickListener {
            val intent = Intent(this, DMActivity::class.java)
            startActivity(intent)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> {
                    val navIntent = Intent(this, SearchActivity::class.java)
                    navIntent.putExtra("uid", uid)
                    startActivity(navIntent)
                    true
                }
                R.id.nav_profile -> {
                    val profileIntent = Intent(this, ProfileActivity::class.java)
                    profileIntent.putExtra("uid", uid)
                    startActivity(profileIntent)
                    true
                }
                R.id.nav_contacts -> {
                    val contactsIntent = Intent(this, ContactsActivity::class.java)
                    contactsIntent.putExtra("uid", uid)
                    startActivity(contactsIntent)
                    true
                }
                R.id.nav_add -> {
                    val newPostIntent = Intent(this, NewPostActivity::class.java)
                    newPostIntent.putExtra("uid", uid)
                    startActivity(newPostIntent)
                    true
                }
                else -> false
            }
        }
    }

    private fun handleOwnStoryClick(uid: String) {
        // Check if user has active stories
        db.collection("users").document(uid)
            .collection("stories")
            .whereGreaterThan("timestamp", System.currentTimeMillis() - 24 * 60 * 60 * 1000) // 24 hours ago
            .get()
            .addOnSuccessListener { documents ->
                val intent: Intent
                if (documents.isEmpty) {
                    // No active stories, go to upload
                    intent = Intent(this, StoryUploadActivity::class.java)
                } else {
                    // Has active stories, view them
                    intent = Intent(this, StoryViewActivity::class.java)
                }
                intent.putExtra("uid", uid)
                startActivity(intent)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error checking stories", exception)
                // In case of error, default to upload
                val intent = Intent(this, StoryUploadActivity::class.java)
                intent.putExtra("uid", uid)
                startActivity(intent)
            }
    }

    private fun loadStoriesFromFollowers(uid: String) {
        // First, get the user's followers
        db.collection("followers")
            .document(uid)
            .collection("userFollowers")
            .get()
            .addOnSuccessListener { followersDocuments ->
                val followers = mutableListOf<String>()
                for (document in followersDocuments) {
                    followers.add(document.id)
                }

                // For each follower, check if they have active stories
                for (followerId in followers) {
                    loadStoriesForUser(followerId)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error loading followers", exception)
            }
    }

    private fun loadStoriesForUser(userId: String) {
        val twentyFourHoursAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000

        // First get user info
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val username = userDocument.getString("username") ?: "Unknown"
                    val profileImageUrl = userDocument.getString("imageID") ?: ""

                    // Then check if they have active stories
                    db.collection("users").document(userId)
                        .collection("stories")
                        .whereGreaterThan("timestamp", twentyFourHoursAgo)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1) // Just get the latest story for each user
                        .get()
                        .addOnSuccessListener { storyDocuments ->
                            if (!storyDocuments.isEmpty) {
                                val latestStory = storyDocuments.documents[0]
                                val story = Story(
                                    userId = userId,
                                    username = username,
                                    profileImageUrl = profileImageUrl,
                                    storyImageUrl = latestStory.getString("imageUrl") ?: "",
                                    timestamp = latestStory.getLong("timestamp") ?: 0,
                                    hasViewed = false // You might want to track this in the database
                                )
                                storiesList.add(story)

                                // Sort stories by timestamp (newest first)
                                storiesList.sortByDescending { it.timestamp }
                                storiesAdapter.updateStories(storiesList)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error loading stories for user $userId", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error loading user info for $userId", exception)
            }
    }

    private fun loadUserPosts(uid: String) {
        db.collection("users").document(uid)
            .collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val posts = mutableListOf<Post>()
                for (document in documents) {
                    val post = Post(
                        postId = document.id,
                        caption = document.getString("caption") ?: "",
                        imageUrl = document.getString("imageID") ?: "",
                        userId = uid,
                        timestamp = document.getTimestamp("timestamp")?.toDate()?.time ?: 0
                    )
                    posts.add(post)
                }
                postsAdapter.updatePosts(posts)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error loading posts", exception)
            }
    }
}