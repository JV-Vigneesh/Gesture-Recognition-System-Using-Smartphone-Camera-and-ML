package com.jvs.gesturerecognitionsystem

import android.annotation.SuppressLint
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.*
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
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    var gestureText by remember { mutableStateOf("Detecting...") }
    var gestureEnabled by remember { mutableStateOf(true) }
    var overlayEnabled by remember { mutableStateOf(true) }

    var currentResult by remember { mutableStateOf<GestureRecognizerResult?>(null) }
    val controller = remember { GestureController(context) }

    // 🔥 Bottom Sheet
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    var imageWidth by remember { mutableStateOf(1) }
    var imageHeight by remember { mutableStateOf(1) }


    Box(modifier = Modifier.fillMaxSize()) {

        // ================= CAMERA =================
        AndroidView(factory = { ctx ->

            val previewView = PreviewView(ctx)
            previewView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({

                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analyzer = ImageAnalysis.Builder()
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analyzer.setAnalyzer(executor, GestureAnalyzer(ctx) { gesture, score, result, imgHeight, imgWidth, rotation ->

                    currentResult = result

                    // 🔥 NO rotation handling here
                    imageWidth = imgWidth
                    imageHeight = imgHeight

                    gestureText = if (gesture == "None" || gesture == "No Gesture") {
                        "Detecting..."
                    } else {
                        "$gesture (${String.format(java.util.Locale.US, "%.2f", score)})"
                    }

                    if (gestureEnabled &&
                        gesture != "None" &&
                        gesture != "No Gesture"
                    ) {
                        controller.handleGesture(gesture)
                    }
                })

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    analyzer
                )

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        })

        // ================= OVERLAY =================
        AndroidView(
            factory = { ctx -> OverlayView(ctx, null) },
            modifier = Modifier.fillMaxSize(),
            update = { overlay ->

                if (overlayEnabled &&
                    currentResult != null &&
                    imageWidth > 0 &&
                    imageHeight > 0
                ) {
                    overlay.setResults(
                        currentResult!!,
                        imageHeight,
                        imageWidth,
                        RunningMode.LIVE_STREAM
                    )
                } else {
                    overlay.clear()
                }
            }
        )

        // ================= BOTTOM PANEL =================
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = "Gesture",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = gestureText,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        showSheet = true
                        scope.launch { sheetState.show() }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }
    }

    // ================= SETTINGS BOTTOM SHEET =================
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Gesture")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = gestureEnabled,
                        onCheckedChange = { gestureEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Show Overlay")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = overlayEnabled,
                        onCheckedChange = { overlayEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}