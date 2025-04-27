package com.amnabatool.assignment_2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {

    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchBox: EditText
    private lateinit var db: FirebaseFirestore

    private val TAG = "SearchActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_13)

        db = FirebaseFirestore.getInstance()
        val uid = intent.getStringExtra("uid") ?: "No UID"
        val name = intent.getStringExtra("name") ?: "No Name"

        searchBox = findViewById(R.id.searchBox)
        searchRecyclerView = findViewById(R.id.searchRecyclerView)
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        // Initialize with an empty list
        searchAdapter = SearchAdapter(emptyList())
        searchRecyclerView.adapter = searchAdapter

        // Add a TextWatcher to the search box to update results as the user types
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val queryText = s.toString().trim()
                if (queryText.isNotEmpty()) {
                    searchUsers(queryText)
                } else {
                    // Clear results when the search box is empty
                    searchAdapter.updateData(emptyList())
                }
            }
            override fun afterTextChanged(s: Editable?) { }
        })

        // Bottom Navigation Handling
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_search

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    var navIntent = (Intent(this, HomeActivity::class.java))
                    navIntent.putExtra("uid", uid)
                    startActivity(navIntent)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    var profileIntent = (Intent(this, ProfileActivity::class.java))
                    profileIntent.putExtra("uid", uid)
                    startActivity(profileIntent)
                    finish()
                    true
                }
                R.id.nav_contacts -> {
                    var contactsIntent = (Intent(this, ContactsActivity::class.java))
                    contactsIntent.putExtra("uid", uid)
                    startActivity(contactsIntent)
                    finish()
                    true
                }
                R.id.nav_add -> {
                    var newPostIntent = (Intent(this, NewPostActivity::class.java))
                    newPostIntent.putExtra("uid", uid)
                    startActivity(newPostIntent)
                    finish()

                    true
                }
                else -> false
            }
        }
    }

    private fun searchUsers(queryText: String) {
        // Use prefix search on the "username" field.
        // Firestore does not support "contains" queries natively,
        // but you can do prefix queries using startAt/endAt.
        db.collection("users")
            .orderBy("username")
            .startAt(queryText)
            .endAt(queryText + "\uf8ff")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    val username = document.getString("name")
                    if (username != null) {
                        userList.add(username)
                    }
                }
                searchAdapter.updateData(userList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error searching users: ", exception)
            }
    }
}
