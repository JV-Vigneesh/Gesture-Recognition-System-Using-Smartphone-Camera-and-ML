package com.jvs.gesturerecognitionsystem

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult

class GestureAnalyzer(
    context: Context,
    private val onResult: (String, Float, GestureRecognizerResult?) -> Unit
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

            val gestureData = result.gestures()[0][0]

            val rawGesture = gestureData.categoryName()
            val score = gestureData.score()

            // if (score < 0.5f) {
              //  onResult("Detecting...", result)
               // return
           // }

            val gesture = smoothGesture(rawGesture)
            Log.d("GESTURE_DEBUG", "$rawGesture -> $score")

            onResult(gesture, score, result)


        } else {
            onResult("No Gesture", 0f, null)
        }
    }

    override fun onError(error: String, errorCode: Int) {
        onResult("Error", 0f, null)
    }
}