package com.amnabatool.assignment_2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.amnabatool.assignment_2.R
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var doneTextView: TextView
    private lateinit var displayNameText: TextView
    private lateinit var profileImageView: ImageView

    private lateinit var db: FirebaseFirestore
    private var selectedImageUri: Uri? = null
    private val IMAGE_PICK_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_12)

        db = FirebaseFirestore.getInstance()
        val uid = intent.getStringExtra("uid") ?: "No UID"

        profileImageView = findViewById(R.id.profileImageView)
        displayNameText = findViewById(R.id.displayName)
        nameEditText = findViewById(R.id.nameEditText)
        usernameEditText = findViewById(R.id.usernameEditText)
        contactEditText = findViewById(R.id.contactEditText)
        bioEditText = findViewById(R.id.bioEditText)
        doneTextView = findViewById(R.id.doneTextView)


        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    val username = document.getString("username")
                    val contact = document.getString("phone")
                    val imageID = document.getString("imageID")
                    val bio = document.getString("bio")

                    nameEditText.hint = name
                    usernameEditText.hint = username
                    contactEditText.hint = contact
                    bioEditText.hint = bio
                    displayNameText.text = name


                    if (!imageID.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageID)
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.default_image)
                    }
                }
            }
            .addOnFailureListener { exception ->

            }


        profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
        }

        doneTextView.setOnClickListener {

            val newName = if (nameEditText.text.toString().isNotEmpty())
                nameEditText.text.toString() else nameEditText.hint.toString()
            val newUsername = if (usernameEditText.text.toString().isNotEmpty())
                usernameEditText.text.toString() else usernameEditText.hint.toString()
            val newContact = if (contactEditText.text.toString().isNotEmpty())
                contactEditText.text.toString() else contactEditText.hint.toString()
            val newBio = if (bioEditText.text.toString().isNotEmpty())
                bioEditText.text.toString() else bioEditText.hint.toString()

            // Create a map of updates
            val updates = hashMapOf<String, Any>(
                "name" to newName,
                "username" to newUsername,
                "phone" to newContact,
                "bio" to newBio
            )


            if (selectedImageUri != null) {
                updates["imageID"] = selectedImageUri.toString()
            }


            updateUserDocument(uid, updates)
        }
    }


    private fun updateUserDocument(uid: String, updates: HashMap<String, Any>) {
        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("uid", uid)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->

            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data

            selectedImageUri?.let { uri ->

                val takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION) ?: 0
                try {
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                Glide.with(this)
                    .load(uri)
                    .into(profileImageView)
            }
        }
    }
}
