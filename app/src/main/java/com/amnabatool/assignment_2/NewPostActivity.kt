package com.amnabatool.assignment_2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment_1.R

class NewPostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_14)


        val closeButton = findViewById<ImageView>(R.id.closeButton)
        val nextText = findViewById<TextView>(R.id.nextText)
        val cameraIcon = findViewById<ImageView>(R.id.cameraIcon)
        val recentRecyclerView = findViewById<RecyclerView>(R.id.recentRecyclerView)

        val imageList = listOf(
            R.drawable.post_7,
            R.drawable.post_6,
            R.drawable.post_8,
            R.drawable.post_9,
            R.drawable.post_14,
            R.drawable.post_12,
            R.drawable.post_2,
            R.drawable.post_11,
            R.drawable.post_10,
            R.drawable.post_13,
            R.drawable.post_3,
            R.drawable.post_4
        )

        val adapter = RecentAdapter(imageList)
        recentRecyclerView.adapter = adapter


        closeButton.setOnClickListener {
            finish()
        }

        cameraIcon.setOnClickListener{
            val intent = Intent(this, CaptureActivity::class.java)
            startActivity(intent)
        }

        nextText.setOnClickListener{
            val intent = Intent(this, NewPostActivity2::class.java)
            startActivity(intent)
        }

    }
}
