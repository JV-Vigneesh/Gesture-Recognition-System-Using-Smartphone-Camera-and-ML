package com.jvs.gesturerecognitionsystem

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.util.Log
import android.content.Intent
import android.widget.Toast

class GestureController(private val context: Context) {

    private var lastGesture = ""
    private var stableCount = 0
    private var lastActionTime = 0L
    private var flashlightOn = false

    private var lastTriggeredGesture = ""

    fun handleGesture(gesture: String) {

        // 🚫 Ignore useless
        if (gesture == "None" || gesture == "No Gesture") return

        // 🔁 Stability check
        if (gesture == lastGesture) {
            stableCount++
        } else {
            stableCount = 0
        }
        lastGesture = gesture

        if (stableCount < 3) return

        // 🚫 Prevent repeat trigger
        if (gesture == lastTriggeredGesture) return
        lastTriggeredGesture = gesture

        when (gesture) {

            "Open_Palm" -> {
                volumeUp()
                Log.d("GESTURE_ACTION", "Volume Up")
            }

            "Closed_Fist" -> {
                volumeDown()
                Log.d("GESTURE_ACTION", "Volume Down")
            }

            "Victory" -> {
                toggleFlashlight()
                Log.d("GESTURE_ACTION", "Flash Toggle")
            }

            "Thumb_Up" -> {
                vibrate()
                Log.d("GESTURE_ACTION", "Vibrate")
            }

            "Pointing_Up" -> {
                openCamera()
                Log.d("GESTURE_ACTION", "Open Camera")
            }

            "Thumb_Down" -> {
                openDialer()
                Log.d("GESTURE_ACTION", "Open Dialer")
            }
        }
    }

    private fun volumeUp() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    private fun volumeDown() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }

    private fun toggleFlashlight() {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]

        flashlightOn = !flashlightOn
        cameraManager.setTorchMode(cameraId, flashlightOn)
    }

    private fun openCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun openDialer() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun vibrate() {
        val vibrator = context.getSystemService(android.os.Vibrator::class.java)

        vibrator?.vibrate(
            android.os.VibrationEffect.createOneShot(
                200,
                android.os.VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }
}