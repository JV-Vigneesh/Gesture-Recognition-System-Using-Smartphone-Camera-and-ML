package com.jvs.gesturerecognitionsystem

import android.util.Log

class GestureController {

    private var lastActionTime = 0L

    private var lastGesture = ""
    private var stableCount = 0

    fun handleGesture(gesture: String) {

        // 🔁 Stability check
        if (gesture == lastGesture) {
            stableCount++
        } else {
            stableCount = 0
        }

        lastGesture = gesture

        // ⛔ Require 3 consistent frames
        if (stableCount < 3) return

        // ⏱ Cooldown
        val now = System.currentTimeMillis()
        if (now - lastActionTime < 500) return
        lastActionTime = now

        // 🎯 Exact match actions
        when (gesture) {

            "Open_Palm" -> {
                Log.d("GESTURE_ACTION", "Start Action")
            }

            "Closed_Fist" -> {
                Log.d("GESTURE_ACTION", "Stop Action")
            }

            "Victory" -> {
                Log.d("GESTURE_ACTION", "Next Action")
            }

            "Thumb_Up" -> {
                Log.d("GESTURE_ACTION", "Like Action")
            }

            else -> {
                Log.d("GESTURE_ACTION", "Ignored: $gesture")
            }
        }
    }
}