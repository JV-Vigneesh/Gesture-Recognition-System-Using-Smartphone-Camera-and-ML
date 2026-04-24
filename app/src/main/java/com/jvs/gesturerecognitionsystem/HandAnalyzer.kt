package com.jvs.gesturerecognitionsystem

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.*
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.framework.image.BitmapImageBuilder

class HandAnalyzer(
    private val context: Context,
    private val onGestureDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val gestureBuffer = ArrayDeque<String>()

    private fun smoothGesture(newGesture: String): String {
        gestureBuffer.addLast(newGesture)
        if (gestureBuffer.size > 5) gestureBuffer.removeFirst()

        return gestureBuffer.groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: newGesture
    }

    private val classifier = GestureClassifier()
    private var handLandmarker: HandLandmarker

    private var lastTime = 0L

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(1)
            .setResultListener { result, _ ->

                if (result.landmarks().isNotEmpty()) {

                    val landmarks = result.landmarks()[0]

                    val rawGesture = classifier.classify(landmarks)
                    val stableGesture = smoothGesture(rawGesture)

                    val handedness = result.handedness()[0][0].categoryName()

                    onGestureDetected("$stableGesture ($handedness)")
                } else {
                    onGestureDetected("No Hand")
                }
            }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    override fun analyze(image: ImageProxy) {

        val now = System.currentTimeMillis()

        // 🔥 throttle frames (IMPORTANT)
        if (now - lastTime < 120) {
            image.close()
            return
        }
        lastTime = now

        try {
            // ✅ Convert safely using ImageUtils (keep this)
            val bitmap = ImageUtils.imageProxyToBitmap(image)

            val mpImage = BitmapImageBuilder(bitmap).build()

            // ✅ Correct API for LIVE_STREAM
            handLandmarker.detectAsync(mpImage, now)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }
}