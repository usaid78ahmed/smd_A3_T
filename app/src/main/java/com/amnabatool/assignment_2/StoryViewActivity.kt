package com.amnabatool.assignment_2

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.concurrent.TimeUnit

class StoryViewActivity : AppCompatActivity() {
    private val TAG = "StoryViewActivity"
    private lateinit var storyImageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var timeAgoText: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String
    private val stories = mutableListOf<Map<String, Any>>()
    private var currentStoryIndex = 0
    private var storyDuration = 5000L // 5 seconds per story
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_view)

        uid = intent.getStringExtra("uid") ?: return

        storyImageView = findViewById(R.id.storyFullImage)
        progressBar = findViewById(R.id.storyProgressBar)
        timeAgoText = findViewById(R.id.timeAgoText)

        db = FirebaseFirestore.getInstance()

        loadStories()

        // Setup touch listeners for navigation
        val leftSide = findViewById<View>(R.id.leftTouchArea)
        val rightSide = findViewById<View>(R.id.rightTouchArea)

        leftSide.setOnClickListener {
            if (currentStoryIndex > 0) {
                currentStoryIndex--
                displayCurrentStory()
            } else {
                finish() // Exit if we're at the first story
            }
        }

        rightSide.setOnClickListener {
            if (currentStoryIndex < stories.size - 1) {
                currentStoryIndex++
                displayCurrentStory()
            } else {
                finish() // Exit if we're at the last story
            }
        }
    }

    private fun loadStories() {
        db.collection("users").document(uid)
            .collection("stories")
            .whereGreaterThan("timestamp", System.currentTimeMillis() - 24 * 60 * 60 * 1000)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val story = document.data
                    stories.add(story)
                }

                if (stories.isNotEmpty()) {
                    displayCurrentStory()
                } else {
                    Toast.makeText(this, "No stories to display", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading stories", e)
                Toast.makeText(this, "Error loading stories", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun displayCurrentStory() {
        handler.removeCallbacksAndMessages(null) // Clear any pending callbacks

        if (currentStoryIndex >= 0 && currentStoryIndex < stories.size) {
            val story = stories[currentStoryIndex]

            // Load image from base64
            val imageData = story["imageData"] as String
            try {
                val imageBytes = Base64.decode(imageData, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                storyImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Error decoding image", e)
                storyImageView.setImageResource(R.drawable.default_image)
            }

            // Show how long ago the story was posted
            val timestamp = story["timestamp"] as Long
            timeAgoText.text = getTimeAgo(timestamp)

            // Reset and start progress bar
            progressBar.progress = 0
            progressBar.max = 100

            // Animate progress bar
            val runnable = object : Runnable {
                override fun run() {
                    if (progressBar.progress < 100) {
                        progressBar.progress += 1
                        handler.postDelayed(this, storyDuration / 100)
                    } else {
                        // Move to next story
                        if (currentStoryIndex < stories.size - 1) {
                            currentStoryIndex++
                            displayCurrentStory()
                        } else {
                            finish() // Exit when all stories are shown
                        }
                    }
                }
            }
            handler.post(runnable)
        }
    }

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            else -> "1d ago" // Stories only last 24 hours
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }
}