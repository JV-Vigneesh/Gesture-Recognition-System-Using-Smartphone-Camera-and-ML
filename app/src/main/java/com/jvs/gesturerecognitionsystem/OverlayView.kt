package com.jvs.gesturerecognitionsystem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.max

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: GestureRecognizerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()

    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        invalidate()
    }

    private fun initPaints() {
        // Change lines to Red
        linePaint.color = Color.RED
        linePaint.strokeWidth = 10f
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.strokeCap = Paint.Cap.ROUND

        // Change points to Yellow
        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = 10f
        pointPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        results?.let { gestureResult ->
            for (landmarks in gestureResult.landmarks()) {

                // 🔹 CALCULATE SCALE AND OFFSETS
                val scale = max(width.toFloat() / imageWidth, height.toFloat() / imageHeight)
                val scaledWidth = imageWidth * scale
                val scaledHeight = imageHeight * scale

                val offsetX = (width - scaledWidth) / 2f
                val offsetY = (height - scaledHeight) / 2f

                // 🔹 DRAW CONNECTIONS
                HandLandmarker.HAND_CONNECTIONS.forEach { connection ->
                    val start = landmarks[connection!!.start()]
                    val end = landmarks[connection.end()]

                    // 🔥 REMOVED (1f - x) because your helper already flips the image!
                    val startX = start.x() * scaledWidth + offsetX
                    val startY = start.y() * scaledHeight + offsetY
                    val endX = end.x() * scaledWidth + offsetX
                    val endY = end.y() * scaledHeight + offsetY

                    canvas.drawLine(startX, startY, endX, endY, linePaint)
                }

                // 🔹 DRAW POINTS
                for (point in landmarks) {
                    // 🔥 REMOVED (1f - x) here as well
                    val x = point.x() * scaledWidth + offsetX
                    val y = point.y() * scaledHeight + offsetY
                    canvas.drawPoint(x, y, pointPaint)
                }
            }
        }
    }

    fun setResults(
        gestureRecognizerResult: GestureRecognizerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.LIVE_STREAM
    ) {
        results = gestureRecognizerResult

        // 🔥 CRITICAL FIX: Dimension Swap
        // This detects if your camera sent landscape dimensions to a portrait screen
        // and swaps them to match the rotated image MediaPipe actually processed.
        if (width < height && imageWidth > imageHeight) {
            this.imageWidth = imageHeight
            this.imageHeight = imageWidth
        } else {
            this.imageWidth = imageWidth
            this.imageHeight = imageHeight
        }

        invalidate()
    }
}