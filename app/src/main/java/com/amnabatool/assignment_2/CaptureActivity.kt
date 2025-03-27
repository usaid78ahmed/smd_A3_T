package com.amnabatool.assignment_2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment_1.R


class CaptureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_15)


        val closeButton = findViewById<ImageView>(R.id.closeButton)
        val nextText = findViewById<TextView>(R.id.nextText)


        closeButton.setOnClickListener {
            finish()
        }

        nextText.setOnClickListener{
            val intent = Intent(this, NewPostActivity2::class.java)
            startActivity(intent)
        }

    }
}
