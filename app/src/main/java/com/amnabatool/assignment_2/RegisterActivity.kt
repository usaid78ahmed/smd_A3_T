package com.amnabatool.assignment_2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amnabatool.assignment_2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val name = findViewById<TextView>(R.id.name)
        val email = findViewById<TextView>(R.id.email)
        val password = findViewById<TextView>(R.id.password)
        val username = findViewById<TextView>(R.id.username)
        val phoneNo = findViewById<TextView>(R.id.phone)

        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginText = findViewById<TextView>(R.id.loginText)

        registerButton.setOnClickListener {
            // Log the email and username (avoid logging the password for security reasons)
            Log.d(TAG, "Register button clicked. Email: ${email.text}, Username: ${username.text}")
            registerUser(
                email.text.toString(),
                password.text.toString(),
                name.text.toString(),
                username.text.toString(),
                phoneNo.text.toString()
            )
        }

        loginText.setOnClickListener {
            Log.d(TAG, "Login text clicked. Navigating to LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser(
        email: String,
        password: String,
        name: String,
        username: String,
        phone: String
    ) {
        Log.d(TAG, "Starting registration process for email: $email")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    if (user != null) {
                        Log.d(TAG, "User registered with UID: ${user.uid}")
                        // Store the rest of the user's data in Firestore using the UID as document ID.
                        saveUserData(user.uid, name, email, username, phone)
                    } else {
                        Log.d(TAG, "User registration succeeded but currentUser is null")
                    }
                } else {
                    Log.e(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserData(uid: String, name: String, email: String, username: String, phone: String) {

        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "username" to username,
            "phone" to phone,
            "imageID" to "default_image",
            "bio" to ""
        )


        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "User data successfully written!")
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                initializeFF(uid)
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error writing user data", e)
                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initializeFF(uid: String) {
        val dummyData = hashMapOf("isDummy" to true)
        db.collection("users").document(uid)
            .collection("followers")
            .document("dummy")
            .set(dummyData)
            .addOnSuccessListener {
                Log.d(TAG, "Followers sub collection initialized")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error initializing followers sub collection", e)
            }


        db.collection("users").document(uid)
            .collection("following")
            .document("dummy")
            .set(dummyData)
            .addOnSuccessListener {
                Log.d(TAG, "Following sub collection initialized")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error initializing following sub collection", e)
            }
    }
}
