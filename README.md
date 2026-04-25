# Gesture-Recognition-System-Using-Smartphone-Camera-and-ML

A real-time Android application built with **Kotlin** and **Jetpack Compose** that utilizes **Google MediaPipe** to recognize hand gestures through the smartphone camera. The app can detect gestures and trigger specific system-level actions or UI updates based on the recognized hand movements.


## Features

* **Real-time Detection**: Processes live camera feed to identify gestures with low latency.
* **MediaPipe Integration**: Uses the `gesture_recognizer.task` model for high-accuracy hand landmark and gesture detection.
* **Jetpack Compose UI**: Features a modern, reactive user interface with a Settings Bottom Sheet for easy configuration.
* **Visual Feedback**: Includes a real-time overlay that draws hand landmarks directly over the camera preview.
* **Gesture Smoothing**: Implements a buffering mechanism to reduce "flicker" and ensure stable gesture recognition.

## Architecture

The project follows a modular structure for handling camera frames and machine learning inference:

* **MainActivity**: Handles runtime camera permissions and initializes the UI.
* **CameraScreen**: The main UI component that manages the CameraX lifecycle and the settings panel.
* **GestureAnalyzer**: An `ImageAnalysis.Analyzer` that captures frames from the camera and prepares them for the model.
* **GestureRecognizerHelper**: Manages the MediaPipe `GestureRecognizer` instance, configuring detection thresholds and hardware delegates (CPU/GPU).
* **OverlayView**: A custom view that renders hand landmarks and connections on top of the video feed.

## Requirements

* **Android Studio**: Hedgehog or newer.
* **Minimum SDK**: API Level 24.
* **Hardware**: A device with a functioning front-facing camera.
* **Model**: Requires `gesture_recognizer.task` located in the `assets` folder.

## Permissions

The app requires the following permissions to be declared in the `AndroidManifest.xml`:
* `android.permission.CAMERA`
* `android.permission.FLASHLIGHT`
* `android.permission.VIBRATE`

## Configuration

You can customize the detection sensitivity in `GestureRecognizerHelper.kt`:
* `minHandDetectionConfidence`: Default is `0.5`.
* `minHandTrackingConfidence`: Default is `0.5`.
* `minHandPresenceConfidence`: Default is `0.5`.

## How to Run

1.  Clone the repository.
2.  Ensure the `gesture_recognizer.task` file is present in `app/src/main/assets/`.
3.  Build the project in Android Studio.
4.  Run the app on a physical Android device.
5.  Grant camera permissions when prompted.

## Working Gestures
| Gesture                  | Action                | What it does                |
| ------------------------ | --------------------- | --------------------------- |
| ✋ `Open_Palm`            | Volume Up             | Increases system volume     |
| ✊ `Closed_Fist`          | Volume Down           | Decreases system volume     |
| ✌️ `Victory`             | Flashlight Toggle     | Turns torch ON/OFF          |
| 👍 `Thumb_Up`            | Vibrate               | Phone vibrates (feedback)   |
| ☝️ `Pointing_Up`         | Open Camera           | Launches camera app         |
| 👎 `Thumb_Down`          | Open Dialer           | Opens phone dial screen     |
| 🤟 `ILoveYou`            | None                   | No action                  |
| 🚫 `None` / `No Gesture` | Ignored               | No action                   |