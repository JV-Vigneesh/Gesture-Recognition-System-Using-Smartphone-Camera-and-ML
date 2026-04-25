package com.jvs.gesturerecognitionsystem

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult

class GestureAnalyzer(
    context: Context,
    private val onResult: (String, Float, GestureRecognizerResult?, Int, Int, Int) -> Unit
) : ImageAnalysis.Analyzer,
    GestureRecognizerHelper.GestureRecognizerListener {

    private val recognizerHelper = GestureRecognizerHelper(
        context = context,
        runningMode = RunningMode.LIVE_STREAM,
        gestureRecognizerListener = this
    )

    private val buffer = ArrayDeque<String>()
    private var lastTime = 0L

    // 🔥 store last frame info
    private var lastRotation = 0
    private var lastWidth = 0
    private var lastHeight = 0

    private fun smoothGesture(newGesture: String): String {
        buffer.addLast(newGesture)
        if (buffer.size > 5) buffer.removeFirst()

        return buffer.groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: newGesture
    }

    override fun analyze(image: ImageProxy) {
        val now = System.currentTimeMillis()

        if (now - lastTime < 120) {
            image.close()
            return
        }

        lastTime = now

        // 🔥 capture real frame info
        lastRotation = image.imageInfo.rotationDegrees

        lastWidth = image.width
        lastHeight = image.height

        recognizerHelper.recognizeLiveStream(image)
    }

    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {

        val result = resultBundle.results.firstOrNull()

        if (result != null && result.gestures().isNotEmpty()) {

            val gestureData = result.gestures()[0][0]

            val rawGesture = gestureData.categoryName()
            val score = gestureData.score()

            val gesture = smoothGesture(rawGesture)

            Log.d("GESTURE_DEBUG", "$rawGesture -> $score")

            onResult(
                gesture,
                score,
                result,
                lastHeight,
                lastWidth,
                0 // rotation no longer needed
            )

        } else {
            onResult("No Gesture", 0f, null, 0, 0, lastRotation)
        }
    }

    override fun onError(error: String, errorCode: Int) {
        onResult("Error", 0f, null, 0, 0, lastRotation)
    }
}