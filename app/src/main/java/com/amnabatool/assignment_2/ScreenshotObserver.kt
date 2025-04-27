package com.amnabatool.assignment_2

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log

class ScreenshotObserver(
    handler: Handler,
    private val context: Context,
    private val onScreenshotDetected: () -> Unit
) : ContentObserver(handler) {

    companion object {
        private const val TAG = "ScreenshotObserver"
        private val SCREENSHOT_KEYWORDS = listOf(
            "screenshot", "screen_shot", "screen-shot", "screen shot", "capture", "scrn"
        )
        // Debounce threshold in milliseconds (e.g., 5 seconds)
        private const val DEBOUNCE_THRESHOLD = 5000L
        private var lastNotificationTime: Long = 0
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        Log.d(TAG, "onChange triggered with uri: $uri")
        if (uri == null) return

        try {
            val projection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN
            )
            // Query for images taken within the last minute
            val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?"
            val timeThreshold = (System.currentTimeMillis() - 60000).toString()
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
            context.contentResolver.query(uri, projection, selection, arrayOf(timeThreshold), sortOrder)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))?.lowercase() ?: ""
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))?.lowercase() ?: ""
                    Log.d(TAG, "Latest image - Name: $name, Path: $path")
                    val isScreenshot = SCREENSHOT_KEYWORDS.any { keyword ->
                        name.contains(keyword) || path.contains(keyword)
                    }
                    if (isScreenshot) {
                        val now = System.currentTimeMillis()
                        if (now - lastNotificationTime > DEBOUNCE_THRESHOLD) {
                            lastNotificationTime = now
                            Log.d(TAG, "Screenshot detected: $path")
                            onScreenshotDetected.invoke()
                        } else {
                            Log.d(TAG, "Screenshot detected but ignored due to debounce threshold")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Screenshot detection error", e)
        }
    }
}