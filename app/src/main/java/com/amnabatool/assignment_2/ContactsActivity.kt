package com.amnabatool.assignment_2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment_1.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ContactsActivity : AppCompatActivity() {

    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var inviteRecyclerView: RecyclerView
    private lateinit var contactsAdapter: ContactAdapter
    private lateinit var inviteAdapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_17)

        contactsRecyclerView = findViewById(R.id.contactsRecyclerView)
        inviteRecyclerView = findViewById(R.id.inviteRecyclerView)

        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        inviteRecyclerView.layoutManager = LinearLayoutManager(this)

        val contactsList = listOf(
            Contact("Henry School", R.drawable.user_profile1),
            Contact("Emily James", R.drawable.user_profile8),
            Contact("Lily Thomas", R.drawable.user_profile11)
        )

        val inviteList = listOf(
            Contact("Amy Wesley", R.drawable.user_profile13),
            Contact("Laura Ryan", R.drawable.user_profile14),
            Contact("Christopher", R.drawable.user_profile12)
        )

        contactsAdapter = ContactAdapter(contactsList, isInvite = false)
        inviteAdapter = ContactAdapter(inviteList, isInvite = true) { contact ->

        }

        val closeButton = findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener{
            finish()
        }

        contactsRecyclerView.adapter = contactsAdapter
        inviteRecyclerView.adapter = inviteAdapter

        // Bottom Navigation Handling
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, NewPostActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.nav_contacts -> return@setOnItemSelectedListener true // Stay on current screen
            }
            false
        }

        bottomNavigationView.selectedItemId = R.id.nav_contacts
    }
}
