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

    override fun analyze(image: ImageProxy) {
        recognizerHelper.recognizeLiveStream(image)
    }

    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {

        val result = resultBundle.results.firstOrNull()

        if (result != null && result.gestures().isNotEmpty()) {

            val gesture = result.gestures()[0][0].categoryName()
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