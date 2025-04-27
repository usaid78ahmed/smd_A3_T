package com.amnabatool.assignment_2

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.amnabatool.assignment_2.R
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NewPostActivity2 : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_16)

        db = FirebaseFirestore.getInstance()


        val uid = intent.getStringExtra("uid") ?: "No UID"


        val captionEditText = findViewById<EditText>(R.id.captionEditText)
        val shareButton = findViewById<Button>(R.id.shareButton)
        val closeButton = findViewById<ImageView>(R.id.closeButton)
        val cardContainer = findViewById<LinearLayout>(R.id.cardContainer)


        closeButton.setOnClickListener { finish() }


        val selectedImageUriString = intent.getStringExtra("selectedImageUri")
        var selectedImageUri: Uri? = null
        if (!selectedImageUriString.isNullOrEmpty()) {
            selectedImageUri = Uri.parse(selectedImageUriString)


            val cardView = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8.dp, 8.dp, 8.dp, 8.dp)
                }
                radius = 16f
                cardElevation = 4f
            }

            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(150.dp, 220.dp)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }


            Glide.with(this)
                .load(selectedImageUri)
                .into(imageView)

            cardView.addView(imageView)
            cardContainer.addView(cardView)
        }


        shareButton.setOnClickListener {
            val captionText = captionEditText.text.toString()
            if (selectedImageUri == null) {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val postData = hashMapOf(
                "imageID" to selectedImageUri.toString(),
                "caption" to captionText,
                "timestamp" to FieldValue.serverTimestamp()
            )


            db.collection("users").document(uid)
                .collection("posts")
                .add(postData)
                .addOnSuccessListener { documentReference ->

                    val dummyData = hashMapOf("isDummy" to true)
                    documentReference.collection("likes")
                        .document("dummy")
                        .set(dummyData)
                    documentReference.collection("comments")
                        .document("dummy")
                        .set(dummyData)

                    Toast.makeText(this, "Post shared!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error sharing post: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
