package com.amnabatool.assignment_2

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private var messages = mutableListOf<Message>()

    private lateinit var prefs: SharedPreferences
    private val PREFS_NAME = "chatPrefs"
    private val KEY_VANISH_MODE = "vanish_mode"
    private val KEY_VANISH_START = "vanish_start"
    private var isVanishMode = false
    private var vanishStart: Long? = null

    private lateinit var gestureDetector: GestureDetector

    private val db = FirebaseFirestore.getInstance()
    private val messagesRef = db.collection("messages")

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private const val FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000

        // Request code for image read permission (READ_MEDIA_IMAGES for API 33+/READ_EXTERNAL_STORAGE otherwise)
        private const val REQ_CODE_READ_IMAGES = 200
    }

    // Screenshot observer to detect screenshots
    private lateinit var screenshotObserver: ScreenshotObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        isVanishMode = prefs.getBoolean(KEY_VANISH_MODE, false)
        vanishStart = prefs.getLong(KEY_VANISH_START, 0L).takeIf { it > 0 }
        Log.d("ChatActivity", "onCreate: isVanishMode=$isVanishMode, vanishStart=$vanishStart")

        // Choose layout based on vanish mode
        if (isVanishMode) {
            setContentView(R.layout.activity_6)
        } else {
            setContentView(R.layout.activity_5)
        }

        findViewById<ImageView>(R.id.backButton)?.setOnClickListener {
            startActivity(Intent(this, DMActivity::class.java))
        }

        if (!isVanishMode) {
            findViewById<ImageView>(R.id.callButton)?.setOnClickListener {
                startActivity(Intent(this, CallActivity::class.java))
            }
            findViewById<ImageView>(R.id.videoCallButton)?.setOnClickListener {
                startActivity(Intent(this, VideoCallActivity::class.java))
            }
        }

        val storedMessages = intent.getParcelableArrayListExtra<Message>("MESSAGES")
        messages = storedMessages ?: mutableListOf()

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

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                toggleVanishMode()
                return true
            }
        })

        loadMessagesFromFirestore()

        if (messages.isNotEmpty()) {
            recyclerView.scrollToPosition(messages.size - 1)
        }

        // Request permission and register screenshot observer
        requestReadImagesPermission()
    }

    private fun requestReadImagesPermission() {
        val permissionsToRequest = mutableListOf<String>()

        // For Android 13+, add notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Storage permission based on Android version
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, storagePermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(storagePermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQ_CODE_READ_IMAGES
            )
        } else {
            // All permissions already granted
            registerScreenshotObserver()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CODE_READ_IMAGES) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerScreenshotObserver()
            } else {
                Log.w("ChatActivity", "Read images permission denied. Screenshot detection may not work.")
            }
        }
    }

    private fun registerScreenshotObserver() {
        // Create notification channel early
        NotificationHelper.createNotificationChannel(this)

        val handler = Handler()
        screenshotObserver = ScreenshotObserver(handler, this) {
            // When a screenshot is detected, send a local notification
            Log.d("ChatActivity", "Screenshot detected! Sending notification...")
            NotificationHelper.sendLocalNotification(this)
        }

        // Register for EXTERNAL content
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            screenshotObserver
        )

        // Also register for INTERNAL content (some devices store screenshots here)
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.INTERNAL_CONTENT_URI,
            true,
            screenshotObserver
        )

        Log.d("ChatActivity", "ScreenshotObserver registered for both INTERNAL and EXTERNAL URIs")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::screenshotObserver.isInitialized) {
            contentResolver.unregisterContentObserver(screenshotObserver)
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
                    val vanishStartLocal = vanishStart
                    for (doc in snapshots.documents) {
                        val msg = doc.toObject(Message::class.java)
                        if (msg != null) {
                            val updatedMsg = msg.copy(id = doc.id)
                            if (updatedMsg.isEphemeral && updatedMsg.vanishDeadline != null) {
                                if (vanishStartLocal != null) {
                                    if (updatedMsg.creationTime >= vanishStartLocal && now < updatedMsg.vanishDeadline) {
                                        newList.add(updatedMsg)
                                    } else if (now >= updatedMsg.vanishDeadline) {
                                        doc.reference.delete()
                                    }
                                } else {
                                    if (now < updatedMsg.vanishDeadline) {
                                        newList.add(updatedMsg)
                                    } else {
                                        doc.reference.delete()
                                    }
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
        val text = messageEditText?.text.toString().trim()
        if (text.isNotEmpty()) {
            val timeStamp = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
            val creation = System.currentTimeMillis()
            val ephemeral = isVanishMode
            val vanishTime = if (ephemeral) creation + ONE_DAY_IN_MILLIS else null

            val newMessage = Message(
                text = text,
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

            messagesRef.add(newMessage).addOnSuccessListener { docRef ->
                val index = messages.indexOf(newMessage)
                if (index != -1) {
                    messages[index] = newMessage.copy(id = docRef.id)
                    messageAdapter.notifyItemChanged(index)
                }
            }

            // Simulate a bot reply after a delay
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
                    vanishDeadline = botVanish,
                    type = MessageType.TEXT
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
                    message.id?.let {
                        messagesRef.document(it).update("text", newText)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMessage(position: Int) {
        val msg = messages[position]
        messages.removeAt(position)
        messageAdapter.notifyItemRemoved(position)
        msg.id?.let {
            messagesRef.document(it).delete()
        }
    }

    private fun toggleVanishMode() {
        val now = System.currentTimeMillis()
        if (!isVanishMode) {
            prefs.edit().putBoolean(KEY_VANISH_MODE, true).apply()
            prefs.edit().putLong(KEY_VANISH_START, now).apply()

            val marker = Message(
                text = "You’ve turned on vanish mode. New messages will disappear in 24 hours after everyone has seen them.",
                isSentByUser = false,
                time = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date(now)),
                creationTime = now,
                isEphemeral = true,
                vanishDeadline = now + ONE_DAY_IN_MILLIS,
                type = MessageType.SYSTEM
            )
            messagesRef.add(marker).addOnSuccessListener { docRef ->
                messages.add(marker.copy(id = docRef.id))
                messageAdapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
                restartActivityWithMode(true)
            }
        } else {
            prefs.edit().putBoolean(KEY_VANISH_MODE, false).apply()
            prefs.edit().remove(KEY_VANISH_START).apply()

            val marker = Message(
                text = "You’ve turned OFF vanish mode. Ephemeral messages sent in vanish mode will still vanish after 24 hours.",
                isSentByUser = false,
                time = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date(now)),
                creationTime = now,
                isEphemeral = true,
                vanishDeadline = now + ONE_DAY_IN_MILLIS,
                type = MessageType.SYSTEM
            )
            messagesRef.add(marker).addOnSuccessListener { docRef ->
                messages.add(marker.copy(id = docRef.id))
                messageAdapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
                restartActivityWithMode(false)
            }
        }
    }

    private fun restartActivityWithMode(vanish: Boolean) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putParcelableArrayListExtra("MESSAGES", ArrayList(messages))
            putExtra("VANISH_MODE", vanish)
        }
        startActivity(intent)
        finish()
    }
}