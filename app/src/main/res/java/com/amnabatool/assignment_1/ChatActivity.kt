package com.amnabatool.assignment_1

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.i210646.DMActivity
import com.example.assignment_1.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private var messageEditText: EditText? = null
    private var sendButton: ImageView? = null
    private var messages = mutableListOf<Message>()

    private var isVanishMode = false // Track vanish mode state
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Check vanish mode
        isVanishMode = intent.getBooleanExtra("VANISH_MODE", false)

        // Set the correct layout
        if (isVanishMode) {
            setContentView(R.layout.activity_6)
        } else {
            setContentView(R.layout.activity_5)
            // Navigate to CallActivity
            val callButton = findViewById<ImageView>(R.id.callButton)
            callButton.setOnClickListener {
                val intent = Intent(this, CallActivity::class.java)
                startActivity(intent)
            }

            // Navigate to VideCallActivity
            val videoCallButton = findViewById<ImageView>(R.id.videoCallButton)
            videoCallButton.setOnClickListener {
                val intent = Intent(this, VideoCallActivity::class.java)
                startActivity(intent)
            }
        }

        // Navigate to DMActivity
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, DMActivity::class.java)
            startActivity(intent)
        }


        // Retrieve previous messages from Intent if they exist
        val storedMessages = intent.getParcelableArrayListExtra<Message>("MESSAGES")
        messages = storedMessages ?: mutableListOf()

        // messages won't be empty if passed in
        recyclerView = findViewById(R.id.messageList)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messages, isVanishMode)
        recyclerView.adapter = messageAdapter


        sendButton?.setOnClickListener {
            sendMessage()
        }

        // Set up gesture for double-tap
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                toggleVanishMode()
                return true
            }
        })

        // Scroll to the last message
        if (messages.isNotEmpty()) {
            recyclerView.scrollToPosition(messages.size - 1)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let { gestureDetector.onTouchEvent(it) }
        return super.dispatchTouchEvent(event)
    }

    private fun sendMessage() {
        val messageText = messageEditText?.text.toString().trim() //Safe Access
        if (messageText.isNotEmpty()) {
            val timeStamp = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
            messages.add(Message(messageText, true, timeStamp)) // Sent by user
            messageAdapter.notifyItemInserted(messages.size - 1)
            recyclerView.scrollToPosition(messages.size - 1)
            messageEditText?.text?.clear() //Safe Access

            // Simulate a response
            recyclerView.postDelayed({
                val botReply = "Nice to hear that!"
                val botTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
                messages.add(Message(botReply, false, botTime)) // Received message
                messageAdapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
            }, 1000)
        }
    }

    private fun toggleVanishMode() {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putParcelableArrayListExtra("MESSAGES", ArrayList(messages)) // Preserve chat
            putExtra("VANISH_MODE", !isVanishMode)
        }

        startActivity(intent)
        finish()
    }
}
