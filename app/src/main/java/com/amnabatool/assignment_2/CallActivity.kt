package com.amnabatool.assignment_2

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.amnabatool.assignment_2.R

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
