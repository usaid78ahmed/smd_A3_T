package com.amnabatool.assignment_2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ContactsActivity : AppCompatActivity() {

    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var inviteRecyclerView: RecyclerView
    private lateinit var contactsAdapter: ContactAdapter
    private lateinit var inviteAdapter: ContactAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserId: String
    private val contactsList = mutableListOf<Contact>()
    private val inviteList = mutableListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_17)

        currentUserId = intent.getStringExtra("uid") ?: ""
        if (currentUserId.isEmpty()) {
            finish()
            return
        }

        firestore = Firebase.firestore

        setupViews()
        loadAllUsers()
        loadFollowRequests()

        // Bottom Navigation Handling
        setupBottomNavigation()
    }

    private fun setupViews() {
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView)
        inviteRecyclerView = findViewById(R.id.inviteRecyclerView)

        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        inviteRecyclerView.layoutManager = LinearLayoutManager(this)

        contactsAdapter = ContactAdapter(
            contactsList,
            isInvite = false,
            isFollowButton = true,
            onActionClick = { contact ->
                sendFollowRequest(contact)
            }
        )

        inviteAdapter = ContactAdapter(
            inviteList,
            isInvite = true,
            isFollowButton = false,
            onActionClick = { contact ->
                acceptFollowRequest(contact)
            }
        )

        val closeButton = findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            finish()
        }

        contactsRecyclerView.adapter = contactsAdapter
        inviteRecyclerView.adapter = inviteAdapter
    }

    private fun loadAllUsers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                contactsList.clear()
                for (document in documents) {
                    val userId = document.id
                    if (userId != currentUserId) {
                        checkFollowStatus(document)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ContactsActivity", "Error getting users: ", exception)
                Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkFollowStatus(document: DocumentSnapshot) {
        val userId = document.id
        val userName = document.getString("name") ?: "Unknown User"
        val email = document.getString("email") ?: ""
        val imageId = document.getLong("imageId")?.toInt() ?: R.drawable.user_profile1

        // Check if already following
        firestore.collection("users").document(currentUserId)
            .collection("followers").document(userId)
            .get()
            .addOnSuccessListener { followerDoc ->
                if (!followerDoc.exists()) {
                    // Check if there's a pending request
                    firestore.collection("users").document(userId)
                        .collection("followRequests").document(currentUserId)
                        .get()
                        .addOnSuccessListener { requestDoc ->
                            if (!requestDoc.exists()) {
                                // Not following and no pending request
                                contactsList.add(Contact(userName, email, imageId, userId))
                                contactsAdapter.notifyDataSetChanged()
                            }
                        }
                }
            }
    }

    private fun loadFollowRequests() {
        firestore.collection("users").document(currentUserId)
            .collection("followRequests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ContactsActivity", "Error getting follow requests", error)
                    return@addSnapshotListener
                }

                inviteList.clear()
                snapshot?.documents?.forEach { document ->
                    val senderId = document.id
                    firestore.collection("users").document(senderId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val userName = userDoc.getString("name") ?: "Unknown User"
                            val email = userDoc.getString("email") ?: ""
                            val imageId = userDoc.getLong("imageId")?.toInt() ?: R.drawable.user_profile1
                            inviteList.add(Contact(userName, email, imageId, senderId))
                            inviteAdapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun sendFollowRequest(contact: Contact) {
        val requestData = hashMapOf(
            "senderId" to currentUserId,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users").document(contact.userId)
            .collection("followRequests").document(currentUserId)
            .set(requestData)
            .addOnSuccessListener {
                Toast.makeText(this, "Follow request sent to ${contact.name}", Toast.LENGTH_SHORT).show()
                // Remove from contacts list
                contactsList.remove(contact)
                contactsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send follow request", Toast.LENGTH_SHORT).show()
                Log.e("ContactsActivity", "Error sending follow request", e)
            }
    }

    private fun acceptFollowRequest(contact: Contact) {
        // Add to current user's followers
        firestore.collection("users").document(currentUserId)
            .collection("followers").document(contact.userId)
            .set(hashMapOf(
                "userId" to contact.userId,
                "name" to contact.name,
                "email" to contact.email,
                "imageId" to contact.imageResId,
                "timestamp" to System.currentTimeMillis()
            ))
            .addOnSuccessListener {
                // Add current user to sender's following
                firestore.collection("users").document(contact.userId)
                    .collection("following").document(currentUserId)
                    .set(hashMapOf(
                        "userId" to currentUserId,
                        "timestamp" to System.currentTimeMillis()
                    ))
                    .addOnSuccessListener {
                        // Remove the follow request
                        firestore.collection("users").document(currentUserId)
                            .collection("followRequests").document(contact.userId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Accepted follow request from ${contact.name}", Toast.LENGTH_SHORT).show()
                                inviteList.remove(contact)
                                inviteAdapter.notifyDataSetChanged()
                            }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to accept follow request", Toast.LENGTH_SHORT).show()
                Log.e("ContactsActivity", "Error accepting follow request", e)
            }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("uid", currentUserId)
                    startActivity(intent)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra("uid", currentUserId)
                    startActivity(intent)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("uid", currentUserId)
                    startActivity(intent)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_add -> {
                    val intent = Intent(this, NewPostActivity::class.java)
                    intent.putExtra("uid", currentUserId)
                    startActivity(intent)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_contacts -> return@setOnItemSelectedListener true
            }
            false
        }

        bottomNavigationView.selectedItemId = R.id.nav_contacts
    }
}