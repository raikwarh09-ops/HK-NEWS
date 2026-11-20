package com.example.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    isFrontCamera: Boolean = true,
    isTorchEnabled: Boolean = false,
    isRecording: Boolean = false,
    onRecordingFinished: (Uri) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var camera by remember { mutableStateOf<Camera?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var currentOutputFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(isTorchEnabled, camera) {
        try {
            camera?.cameraControl?.enableTorch(isTorchEnabled && !isFrontCamera)
        } catch (_: Exception) {}
    }

    // Handle recording start and stop
    LaunchedEffect(isRecording) {
        if (isRecording) {
            val capture = videoCapture
            if (capture != null) {
                try {
                    val file = File(context.cacheDir, "hk_live_cam_${System.currentTimeMillis()}.mp4")
                    currentOutputFile = file
                    val outputOptions = FileOutputOptions.Builder(file).build()

                    val recording = capture.output
                        .prepareRecording(context, outputOptions)
                        .apply {
                            val hasAudio = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasAudio) {
                                withAudioEnabled()
                            }
                        }
                        .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                            when (recordEvent) {
                                is VideoRecordEvent.Finalize -> {
                                    if (!recordEvent.hasError() && file.exists() && file.length() > 0) {
                                        onRecordingFinished(Uri.fromFile(file))
                                    } else {
                                        if (file.exists() && file.length() > 0) {
                                            onRecordingFinished(Uri.fromFile(file))
                                        }
                                    }
                                }
                            }
                        }
                    activeRecording = recording
                } catch (e: Exception) {
                    onError("रिकॉर्डिंग शुरू करने में त्रुटि: ${e.localizedMessage}")
                }
            }
        } else {
            activeRecording?.stop()
            activeRecording = null
        }
    }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(isFrontCamera, previewViewRef) {
        val view = previewViewRef ?: return@LaunchedEffect
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = view.surfaceProvider
                }

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()
                val vCapture = VideoCapture.withOutput(recorder)
                videoCapture = vCapture

                val cameraSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    vCapture
                )
            } catch (e: Exception) {
                onError("Camera initialization error: ${e.localizedMessage}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewViewRef = this
                }
            }
        )
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                activeRecording?.stop()
                activeRecording = null
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                cameraProvider.unbindAll()
            } catch (_: Exception) {}
        }
    }
}
