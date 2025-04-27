package com.amnabatool.assignment_2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.*

class StoryUploadActivity : AppCompatActivity() {
    private val TAG = "StoryUploadActivity"
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private lateinit var storyImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_upload)

        val uid = intent.getStringExtra("uid") ?: return

        storyImageView = findViewById(R.id.storyImageView)
        val selectButton = findViewById<Button>(R.id.selectImageButton)
        val uploadButton = findViewById<Button>(R.id.uploadStoryButton)

        selectButton.setOnClickListener {
            openGallery()
        }

        uploadButton.setOnClickListener {
            if (selectedImageUri != null) {
                uploadStory(uid)
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            storyImageView.setImageURI(selectedImageUri)
        }
    }

    private fun uploadStory(uid: String) {
        if (selectedImageUri == null) return

        try {
            // Convert image to base64 string
            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Compress and resize the image to reduce size
            val resizedBitmap = resizeBitmap(bitmap, 800) // Resize to max 800px width
            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            // Save the encoded image to databases
            saveStoryToDatabase(uid, base64Image)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to process image", e)
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth = maxWidth
        val newHeight = (newWidth / ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
    }

    private fun saveStoryToDatabase(uid: String, base64Image: String) {
        val db = FirebaseFirestore.getInstance()
        val realtimeDb = FirebaseDatabase.getInstance().reference

        val timestamp = System.currentTimeMillis()
        val storyId = UUID.randomUUID().toString()

        // Create story object with base64 image directly embedded
        val story = hashMapOf(
            "imageData" to base64Image,
            "timestamp" to timestamp,
            "expiresAt" to timestamp + (24 * 60 * 60 * 1000) // 24 hours later
        )

        // Save to Firestore
        db.collection("users").document(uid)
            .collection("stories")
            .document(storyId)
            .set(story)
            .addOnSuccessListener {
                // Also save to Realtime Database for faster access
                realtimeDb.child("stories").child(uid).child(storyId).setValue(story)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Story uploaded successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error saving to Realtime Database", e)
                        Toast.makeText(this, "Story saved but may not appear immediately", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving story", e)
                Toast.makeText(this, "Failed to save story", Toast.LENGTH_SHORT).show()
            }
    }
}
