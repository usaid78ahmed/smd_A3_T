package com.amnabatool.assignment_2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import io.agora.rtc2.*

class CallActivity : AppCompatActivity() {

    companion object {
        private const val APP_ID = "481cd65d25024fcf97c902ea7a9b8723"
        private const val CHANNEL_NAME = "testChannel"
        // Ensure that the token here is valid. (It may be a temporary token)
        private const val TEMP_TOKEN = "007eJxTYHAqmei7MEHRZt+5TtXvzk9NxbLLSmbWm8w/u9y030sgYK8Cg4mFYXKKmWmKkamBkUlacpqlebKlgVFqonmiZZKFuZGx0WGOjIZARoZwpWMsjAwQCOJzM5SkFpc4ZyTm5aXmMDAAAKwMICQ="
        private const val PERMISSION_REQ_ID = 33
    }

    private var rtcEngine: RtcEngine? = null
    private lateinit var endCallButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_7)

        endCallButton = findViewById(R.id.endCall)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID)) {
            initializeAndJoinChannel()
        }

        endCallButton.setOnClickListener {
            leaveChannel()
            finish()
        }
    }

    private fun initializeAndJoinChannel() {
        try {
            rtcEngine = RtcEngine.create(this, APP_ID, rtcEngineEventHandler)
        } catch (e: Exception) {
            Log.e("CallActivity", "RtcEngine create error: ${e.message}")
            return
        }

        rtcEngine?.enableAudio()
        rtcEngine?.joinChannel(TEMP_TOKEN, CHANNEL_NAME, null, 0)
    }

    private val rtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                Log.d("CallActivity", "Join channel success on $channel, uid: $uid")
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                Log.d("CallActivity", "Remote user joined: $uid")
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Log.d("CallActivity", "Remote user offline: $uid")
            }
        }

        override fun onError(err: Int) {
            runOnUiThread {
                Log.e("CallActivity", "Error occurred: $err")
            }
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        return if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeAndJoinChannel()
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Microphone permission is required for voice calls",
                    Snackbar.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun leaveChannel() {
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
    }
}