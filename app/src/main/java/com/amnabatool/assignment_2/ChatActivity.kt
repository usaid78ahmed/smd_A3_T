package com.amnabatool.assignment_2

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private var messageEditText: EditText? = null
    private var sendButton: ImageView? = null
    private var attachmentButton: ImageView? = null

    // Local list of messages
    private var messages = mutableListOf<Message>()

    private var isVanishMode = false // true for vanish mode, false for normal mode
    private lateinit var gestureDetector: GestureDetector

    // Firestore
    private val db = FirebaseFirestore.getInstance()
    private val messagesRef = db.collection("messages")

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private const val FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve vanish mode flag from intent; normal mode if not specified.
        isVanishMode = intent.getBooleanExtra("VANISH_MODE", false)
        Log.d("ChatActivity", "onCreate: isVanishMode=$isVanishMode")

        // Set layout based on vanish mode
        if (isVanishMode) {
            setContentView(R.layout.activity_6)
        } else {
            setContentView(R.layout.activity_5)
        }

        // Set up back button to navigate to DMActivity
        findViewById<ImageView>(R.id.backButton)?.setOnClickListener {
            startActivity(Intent(this, DMActivity::class.java))
        }

        // For normal mode, set up additional buttons
        if (!isVanishMode) {
            findViewById<ImageView>(R.id.callButton)?.setOnClickListener {
                startActivity(Intent(this, CallActivity::class.java))
            }
            findViewById<ImageView>(R.id.videoCallButton)?.setOnClickListener {
                startActivity(Intent(this, VideoCallActivity::class.java))
            }
        }

        // Retrieve any stored messages from intent extras (if switching modes)
        val storedMessages = intent.getParcelableArrayListExtra<Message>("MESSAGES")
        messages = storedMessages ?: mutableListOf()

        // Set up RecyclerView and adapter
        recyclerView = findViewById(R.id.messageList)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        attachmentButton = findViewById(R.id.attachmentButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messages, isVanishMode) { position ->
            onMessageLongPressed(position)
        }
        recyclerView.adapter = messageAdapter

        sendButton?.setOnClickListener { sendMessage() }
        attachmentButton?.setOnClickListener { pickImageFromGallery() }

        // Setup double-tap gesture for toggling vanish mode
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                toggleVanishMode()
                return true
            }
        })

        // Load messages from Firestore in either mode.
        // In normal mode, we want all persistent messages.
        // In vanish mode, we load messages but filter out ephemeral ones that expired.
        loadMessagesFromFirestore()

        if (messages.isNotEmpty()) {
            recyclerView.scrollToPosition(messages.size - 1)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let { gestureDetector.onTouchEvent(it) }
        return super.dispatchTouchEvent(event)
    }

    private fun loadMessagesFromFirestore() {
        messagesRef.orderBy("creationTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("ChatActivity", "Error loading messages", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val newList = mutableListOf<Message>()
                    val now = System.currentTimeMillis()
                    for (doc in snapshots.documents) {
                        val msg = doc.toObject(Message::class.java)
                        if (msg != null) {
                            val updatedMsg = msg.copy(id = doc.id)
                            // If the message is ephemeral (vanish mode), only add if still valid.
                            if (updatedMsg.isEphemeral && updatedMsg.vanishDeadline != null) {
                                if (now < updatedMsg.vanishDeadline) {
                                    newList.add(updatedMsg)
                                } else {
                                    // Optionally delete expired ephemeral messages
                                    doc.reference.delete()
                                }
                            } else {
                                newList.add(updatedMsg)
                            }
                        }
                    }
                    messages = newList
                    messageAdapter.updateMessages(messages)
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun sendMessage() {
        val messageText = messageEditText?.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val timeStamp = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
            val creation = System.currentTimeMillis()
            // For vanish mode, mark new messages as ephemeral with a vanish deadline 24 hours later.
            val ephemeral = isVanishMode
            val vanishTime = if (ephemeral) creation + ONE_DAY_IN_MILLIS else null

            val newMessage = Message(
                text = messageText,
                isSentByUser = true,
                time = timeStamp,
                creationTime = creation,
                isEphemeral = ephemeral,
                vanishDeadline = vanishTime
            )

            messages.add(newMessage)
            messageAdapter.notifyItemInserted(messages.size - 1)
            recyclerView.scrollToPosition(messages.size - 1)
            messageEditText?.text?.clear()

            // Save to Firestore (both normal and ephemeral messages are saved)
            messagesRef.add(newMessage).addOnSuccessListener { docRef ->
                val index = messages.indexOf(newMessage)
                if (index != -1) {
                    messages[index] = newMessage.copy(id = docRef.id)
                    messageAdapter.notifyItemChanged(index)
                }
            }

            // Simulate bot response after 1 second
            recyclerView.postDelayed({
                val botReply = "Nice to hear that!"
                val botTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
                val botCreation = System.currentTimeMillis()
                val botEphemeral = isVanishMode
                val botVanish = if (botEphemeral) botCreation + ONE_DAY_IN_MILLIS else null

                val botMessage = Message(
                    text = botReply,
                    isSentByUser = false,
                    time = botTime,
                    creationTime = botCreation,
                    isEphemeral = botEphemeral,
                    vanishDeadline = botVanish
                )
                messages.add(botMessage)
                messageAdapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
                messagesRef.add(botMessage).addOnSuccessListener { docRef ->
                    val idx = messages.indexOf(botMessage)
                    if (idx != -1) {
                        messages[idx] = botMessage.copy(id = docRef.id)
                        messageAdapter.notifyItemChanged(idx)
                    }
                }
            }, 1000)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                sendImageMessage(uri)
            }
        }
    }

    private fun sendImageMessage(uri: Uri) {
        val timeStamp = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
        val creation = System.currentTimeMillis()
        val ephemeral = isVanishMode
        val vanishTime = if (ephemeral) creation + ONE_DAY_IN_MILLIS else null

        val newMessage = Message(
            text = null,
            isSentByUser = true,
            time = timeStamp,
            type = MessageType.IMAGE,
            imageUri = uri.toString(),
            creationTime = creation,
            isEphemeral = ephemeral,
            vanishDeadline = vanishTime
        )
        messages.add(newMessage)
        messageAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
        messagesRef.add(newMessage).addOnSuccessListener { docRef ->
            val index = messages.indexOf(newMessage)
            if (index != -1) {
                messages[index] = newMessage.copy(id = docRef.id)
                messageAdapter.notifyItemChanged(index)
            }
        }
    }

    private fun onMessageLongPressed(position: Int) {
        if (position < 0 || position >= messages.size) return
        val message = messages[position]
        val currentTime = System.currentTimeMillis()
        // Allow edit/delete only within 5 minutes for user-sent messages
        if (currentTime - message.creationTime <= FIVE_MINUTES_IN_MILLIS && message.isSentByUser) {
            val options = arrayOf("Edit", "Delete")
            AlertDialog.Builder(this)
                .setTitle("Choose an action")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> editMessage(position)
                        1 -> deleteMessage(position)
                    }
                }
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Action not allowed")
                .setMessage("You can only edit or delete messages within 5 minutes of sending.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun editMessage(position: Int) {
        val message = messages[position]
        val editText = EditText(this)
        editText.setText(message.text)
        AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    val updatedMessage = message.copy(text = newText)
                    messages[position] = updatedMessage
                    messageAdapter.notifyItemChanged(position)
                    if (message.id != null) {
                        messagesRef.document(message.id).update("text", newText)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

       private fun deleteMessage(position: Int) {
        val messageToDelete = messages[position]
        messages.removeAt(position)
        messageAdapter.notifyItemRemoved(position)
        if (messageToDelete.id != null) {
            messagesRef.document(messageToDelete.id).delete()
        }
    }

    private fun toggleVanishMode() {
        // Pass current messages so that normal messages persist
        val intent = Intent(this, ChatActivity::class.java).apply {
            putParcelableArrayListExtra("MESSAGES", ArrayList(messages))
            putExtra("VANISH_MODE", !isVanishMode)
        }
        startActivity(intent)
        finish()
    }
}
