package com.jvs.gesturerecognitionsystem

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

class GestureClassifier {

    fun classify(landmarks: List<NormalizedLandmark>): String {

        if (landmarks.size < 21) return "No Hand"

        val thumb = isThumbUp(landmarks)
        val index = isFingerUp(landmarks, 8, 6)
        val middle = isFingerUp(landmarks, 12, 10)
        val ring = isFingerUp(landmarks, 16, 14)
        val pinky = isFingerUp(landmarks, 20, 18)

        val count = listOf(thumb, index, middle, ring, pinky).count { it }

        return when {
            count == 0 -> "Fist"
            count == 5 -> "Open Hand"
            index && !middle && !ring && !pinky -> "Pointing"
            thumb && !index && !middle && !ring && !pinky -> "Thumbs Up"
            index && middle && !ring && !pinky -> "Peace"
            else -> "Unknown"
        }
    }

    private fun isFingerUp(
        landmarks: List<NormalizedLandmark>,
        tip: Int,
        pip: Int
    ): Boolean {
        return landmarks[tip].y() < landmarks[pip].y()
    }

    private fun isThumbUp(landmarks: List<NormalizedLandmark>): Boolean {
        return landmarks[4].x() > landmarks[3].x()
    }
}