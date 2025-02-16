package com.example.assignment_1

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class NewPostActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_16)

        val closeButton = findViewById<ImageView>(R.id.closeButton)

        closeButton.setOnClickListener {
            finish()
        }

    }
}
