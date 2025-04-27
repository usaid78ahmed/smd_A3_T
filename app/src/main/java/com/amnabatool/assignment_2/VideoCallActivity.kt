package com.amnabatool.assignment_2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc2.*
import io.agora.rtc2.Constants.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration

class VideoCallActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "VideoCallActivity"
        private const val APP_ID = "481cd65d25024fcf97c902ea7a9b8723"  // Verify this ID from your Agora Console!
        private const val CHANNEL_NAME = "testChannel"
        // Use a valid temporary token. If expired, generate a new one.
        private const val TEMP_TOKEN = "007eJxTYHAqmei7MEHRZt+5TtXvzk9NxbLLSmbWm8w/u9y030sgYK8Cg4mFYXKKmWmKkamBkUlacpqlebKlgVFqonmiZZKFuZGx0WGOjIZARoZwpWMsjAwQCOJzM5SkFpc4ZyTm5aXmMDAAAKwMICQ="
        private const val PERMISSION_REQ_ID = 22
    }

    private var rtcEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null
    private lateinit var endCallButton: ImageView

    // Agora event handler
    private val rtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                Log.d(TAG, "Joined channel: $channel, UID: $uid")
                showToast("Joined channel successfully")
            }
        }
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                Log.d(TAG, "Remote user joined: $uid")
                setupRemoteVideo(uid)
            }
        }
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Log.d(TAG, "User offline: $uid, Reason: $reason")
                removeRemoteVideo()
            }
        }
        override fun onError(errorCode: Int) {
            runOnUiThread {
                Log.e(TAG, "Agora Error: $errorCode")
                showToast("Agora Error: $errorCode")
            }
        }
        override fun onConnectionStateChanged(state: Int, reason: Int) {
            runOnUiThread {
                Log.d(TAG, "Connection state changed: $state, Reason: $reason")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_8)

        endCallButton = findViewById(R.id.endCall)
        endCallButton.setOnClickListener {
            leaveChannel()
            finish()
        }

        // Initialize Agora engine after checking permissions
        if (checkPermissions()) {
            setupAgoraEngine()
        }
    }

    private fun checkPermissions(): Boolean {
        val requiredPermissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        return if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQ_ID
            )
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                setupAgoraEngine()
            } else {
                showToast("Permissions not granted")
                finish()
            }
        }
    }

    private fun setupAgoraEngine() {
        try {
            // Use RtcEngineConfig to create the engine
            val config = RtcEngineConfig().apply {
                mContext = applicationContext
                mAppId = APP_ID
                mEventHandler = rtcEngineEventHandler
            }
            rtcEngine = RtcEngine.create(config)
        } catch (e: Exception) {
            Log.e(TAG, "Agora Engine Initialization Error: ${e.message}")
            showToast("Agora initialization failed: ${e.message}")
            return
        }

        rtcEngine?.apply {
            enableVideo()
            enableAudio() // Though audio is enabled by default, we explicitly enable it here.
            setChannelProfile(CHANNEL_PROFILE_COMMUNICATION)
            setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VideoDimensions(640, 360),
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                )
            )
        }

        // Setup local video preview
        setupLocalVideo()

        // Join channel
        joinChannel()
    }

    private fun setupLocalVideo() {
        val container = findViewById<FrameLayout>(R.id.localVideoContainer)
        localSurfaceView = SurfaceView(this)
        container.addView(localSurfaceView)
        rtcEngine?.setupLocalVideo(VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        rtcEngine?.startPreview()
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = findViewById<FrameLayout>(R.id.remoteVideoContainer)
        if (remoteSurfaceView != null) return
        remoteSurfaceView = SurfaceView(this)
        container.addView(remoteSurfaceView)
        rtcEngine?.setupRemoteVideo(VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    private fun removeRemoteVideo() {
        val container = findViewById<FrameLayout>(R.id.remoteVideoContainer)
        container.removeAllViews()
        remoteSurfaceView = null
    }

    private fun joinChannel() {
        rtcEngine?.joinChannel(
            TEMP_TOKEN,
            CHANNEL_NAME,
            null,
            0
        )
    }

    private fun leaveChannel() {
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
    }
}