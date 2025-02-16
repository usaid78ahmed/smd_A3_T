package com.example.assignment_1

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_7)

        val endCallButton = findViewById<ImageView>(R.id.endCall)

        endCallButton.setOnClickListener {
            finish()
        }
    }
}
