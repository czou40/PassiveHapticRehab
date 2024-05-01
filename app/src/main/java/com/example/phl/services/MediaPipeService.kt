package com.example.phl.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.phl.R
import java.util.concurrent.Executors
import android.util.Log
import android.view.Surface
import androidx.camera.core.ImageCaptureException
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.view.PreviewView
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ArrayBlockingQueue

class MediaPipeService : LifecycleService() {

    private val executor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var isStreaming = AtomicBoolean(false)
    private var isCameraReady = AtomicBoolean(false)
    private var preview: Preview? = null

    private var cameraFacing = CameraSelector.LENS_FACING_FRONT

    private val binder = LocalBinder()

    // Class used for the client Binder.
    inner class LocalBinder : Binder() {
        fun startStreaming(previewView: PreviewView?=null) {
            if (isCameraReady.get()) {
                if (!isStreaming.get()) {
                    isStreaming.set(true)
                    startCamera(previewView)
                    Toast.makeText(applicationContext, "Streaming started", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        applicationContext, "Streaming already started", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(applicationContext, "Camera not ready", Toast.LENGTH_SHORT).show()
            }
        }

        fun stopStreaming() {
            if (isStreaming.get()) {
                stopCamera()
                isStreaming.set(false)
                Toast.makeText(applicationContext, "Streaming stopped", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Streaming not started", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, getNotification())

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            isCameraReady.set(true)
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MediaPipeService", "Service destroyed")
        stopCamera();  // Ensure camera is stopped
        executor.shutdown();  // Shutdown executor service
    }

    private fun startCamera(previewView: PreviewView?) {
        if (!isCameraReady.get()) {
            Log.e("MediaPipeService", "Camera provider is null")
            return
        }
        val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        val resolutionSelector = ResolutionSelector.Builder().setAspectRatioStrategy(
                AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
            ).build()

        val rotation = previewView?.display?.rotation ?: Surface.ROTATION_0

        preview =
            Preview.Builder().setResolutionSelector(resolutionSelector).setTargetRotation(rotation)
                .build()

        imageAnalyzer = ImageAnalysis.Builder().setTargetRotation(rotation)
            .setResolutionSelector(resolutionSelector)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
            .also { analyzer ->
                analyzer.setAnalyzer(executor) { imageProxy ->
                    // Insert your image analysis code here
                    imageProxy.close()
                }
            }

        try {
            cameraProvider?.unbindAll() // Make sure nothing is bound to the cam provider
            cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            // Attach the viewfinder's surface provider to preview use case
            if (previewView != null) {
                preview?.setSurfaceProvider(previewView.surfaceProvider)
            }
        } catch (e: Exception) {
            // Handle exception
            Log.e("MediaPipeService", "Error starting camera: ${e.message}")
        }
    }

    private fun stopCamera() {
        try {
            cameraProvider?.unbindAll()  // Unbind all use cases from the camera provider
        } catch (e: Exception) {
            Log.e("MediaPipeService", "Error stopping camera: ${e.message}")
        }
    }

    private fun getNotification(): Notification {
        val notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Camera Service")
                .setContentText("Running...").setSmallIcon(R.drawable.ic_baseline_photo_camera_24)
                .setOngoing(true)

        return notificationBuilder.build()
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(CHANNEL_ID, "Camera Service", NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "media_pipe_service_channel"
//        const val ACTION_CAPTURE_PHOTO = "action_capture_photo"
//        const val ACTION_SEND_PHOTO = "action_send_photo"
//        const val ACTION_START_STREAMING = "action_start_streaming"
//        const val ACTION_STOP_STREAMING = "action_stop_streaming"
//        const val ACTION_SEND_STREAM = "action_send_stream"
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

}