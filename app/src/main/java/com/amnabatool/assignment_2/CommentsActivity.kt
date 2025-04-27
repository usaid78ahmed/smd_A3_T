package com.amnabatool.assignment_2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentsActivity : AppCompatActivity() {
    private val TAG = "CommentsActivity"
    private lateinit var db: FirebaseFirestore
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var commentInput: EditText
    private lateinit var postCommentButton: Button

    private lateinit var postId: String
    private lateinit var postUserId: String
    private lateinit var currentUserId: String
    private lateinit var currentUserName: String

    private val commentsList = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comments_activity)

        db = FirebaseFirestore.getInstance()

        // Get data from intent
        postId = intent.getStringExtra("postId") ?: ""
        postUserId = intent.getStringExtra("postUserId") ?: ""
        currentUserId = intent.getStringExtra("currentUserId") ?: ""

        // Initialize views
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentInput = findViewById(R.id.commentInput)
        postCommentButton = findViewById(R.id.postCommentButton)

        // Setup RecyclerView
        commentsAdapter = CommentsAdapter(commentsList)
        commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CommentsActivity)
            adapter = commentsAdapter
        }

        // Load current user info
        loadCurrentUserInfo()

        // Load comments
        loadComments()

        // Setup post button click listener
        postCommentButton.setOnClickListener {
            val commentText = commentInput.text.toString().trim()
            if (commentText.isNotEmpty()) {
                postComment(commentText)
            } else {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCurrentUserInfo() {
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    currentUserName = document.getString("name") ?: "Anonymous"
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading user info", e)
                currentUserName = "Anonymous"
            }
    }

    private fun loadComments() {
        db.collection("users").document(postUserId)
            .collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val comments = mutableListOf<Comment>()
                for (document in documents) {
                    val comment = Comment(
                        commentId = document.id,
                        userId = document.getString("userId") ?: "",
                        userName = document.getString("userName") ?: "Anonymous",
                        commentText = document.getString("commentText") ?: "",
                        timestamp = document.getLong("timestamp") ?: 0
                    )
                    comments.add(comment)
                }
                commentsAdapter.updateComments(comments)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading comments", e)
                Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
    }

    private fun postComment(commentText: String) {
        val comment = hashMapOf(
            "userId" to currentUserId,
            "userName" to currentUserName,
            "commentText" to commentText,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(postUserId)
            .collection("posts").document(postId)
            .collection("comments")
            .add(comment)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Comment added with ID: ${documentReference.id}")

                // Add comment to the adapter
                val newComment = Comment(
                    commentId = documentReference.id,
                    userId = currentUserId,
                    userName = currentUserName,
                    commentText = commentText,
                    timestamp = System.currentTimeMillis()
                )
                commentsAdapter.addComment(newComment)

                // Clear input field
                commentInput.text.clear()

                // Optional: Scroll to top to show the new comment
                commentsRecyclerView.smoothScrollToPosition(0)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding comment", e)
                Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show()
            }
    }
}