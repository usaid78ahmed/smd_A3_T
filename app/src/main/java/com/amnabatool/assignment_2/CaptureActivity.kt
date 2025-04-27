package com.amnabatool.assignment_2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.amnabatool.assignment_2.R

class CaptureActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private val GALLERY_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_15)
        val uid = intent.getStringExtra("uid") ?: "No UID"

        val closeButton = findViewById<ImageView>(R.id.closeButton)
        val nextText = findViewById<TextView>(R.id.nextText)
        val gallery = findViewById<ImageView>(R.id.gallery)

        closeButton.setOnClickListener {
            finish()
        }

        gallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        nextText.setOnClickListener {
            val intent = Intent(this, NewPostActivity2::class.java)
            selectedImageUri?.let { uri ->
                intent.putExtra("selectedImageUri", uri.toString())
            }
            intent.putExtra("uid", uid)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                // Persist the permission so the URI can be accessed later
                val takeFlags = data?.flags?.and(Intent.FLAG_GRANT_READ_URI_PERMISSION) ?: 0
                try {
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
