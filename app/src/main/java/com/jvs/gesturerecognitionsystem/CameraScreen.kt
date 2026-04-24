package com.jvs.gesturerecognitionsystem

import android.annotation.SuppressLint
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.lifecycle.compose.LocalLifecycleOwner

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen() {

    val context = LocalContext.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    var gestureText by remember { mutableStateOf("Detecting...") }
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(factory = {
            val previewView = PreviewView(it)
            previewView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(it)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analyzer = ImageAnalysis.Builder().build()

                analyzer.setAnalyzer(executor, HandAnalyzer(context) {
                    gestureText = it
                })

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA



                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    analyzer
                )

            }, ContextCompat.getMainExecutor(it))

            previewView
        })

        Text(
            text = "Gesture: $gestureText",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}