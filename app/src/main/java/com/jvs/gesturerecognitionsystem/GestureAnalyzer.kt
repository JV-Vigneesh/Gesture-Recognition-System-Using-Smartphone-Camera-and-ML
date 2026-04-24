package com.jvs.gesturerecognitionsystem

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.jvs.gesturerecognitionsystem.GestureRecognizerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode

class GestureAnalyzer(
    context: Context,
    private val onGestureDetected: (String) -> Unit
) : ImageAnalysis.Analyzer,
    GestureRecognizerHelper.GestureRecognizerListener {

    private val recognizerHelper = GestureRecognizerHelper(
        context = context,
        runningMode = RunningMode.LIVE_STREAM,
        gestureRecognizerListener = this
    )

    private val buffer = ArrayDeque<String>()

    private var lastTime = 0L

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
        recognizerHelper.recognizeLiveStream(image)
    }

    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {

        val result = resultBundle.results.firstOrNull()

        if (result != null && result.gestures().isNotEmpty()) {

            val rawGesture = result.gestures()[0][0].categoryName()
            val gesture = smoothGesture(rawGesture)
            val score = result.gestures()[0][0].score()

            onGestureDetected("$gesture (${String.format("%.2f", score)})")
        } else {
            onGestureDetected("No Gesture")
        }
    }

    override fun onError(error: String, errorCode: Int) {
        onGestureDetected("Error")
    }
}