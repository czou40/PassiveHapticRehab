package com.example.phl.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.phl.R
import com.example.phl.utils.LandmarkerHelper
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import com.google.mediapipe.tasks.vision.core.RunningMode

class MediaPipeService : LifecycleService(), LandmarkerHelper.LandmarkerListener {

    private val executor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var isStreaming = AtomicBoolean(false)
    private var isCameraReady = AtomicBoolean(false)
    private var preview: Preview? = null

    private var cameraFacing = CameraSelector.LENS_FACING_FRONT

    private val binder = LocalBinder()

    private lateinit var landmarkerHelper: LandmarkerHelper

    private var configuration: Configuration = Configuration()

    // Class used for the client Binder.
    inner class LocalBinder : Binder() {

//        fun setConfiguration(config: Configuration) {
//            configuration = config
//        }
//        fun getConfiguration(): Configuration = configuration

        fun startStreaming(previewView: PreviewView? = null) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                1,
                getNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            )
        } else {
            startForeground(1, getNotification())
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            isCameraReady.set(true)
        }, ContextCompat.getMainExecutor(this))

        // Create the LandmarkerHelper that will handle the inference
        executor.execute {
            landmarkerHelper = LandmarkerHelper(
                context = applicationContext,
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = configuration.minHandDetectionConfidence,
                minHandTrackingConfidence = configuration.minHandTrackingConfidence,
                minHandPresenceConfidence = configuration.minHandPresenceConfidence,
                maxNumHands = configuration.maxHands,
                currentDelegate = configuration.delegate,
                handLandmarkerHelperListener = this
            )
        }
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
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
            .also { analyzer ->
                analyzer.setAnalyzer(executor) { imageProxy ->
                    // Insert your image analysis code here
                    detectHand(imageProxy)
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

    private fun detectHand(imageProxy: ImageProxy) {
        landmarkerHelper.detectLiveStream(
            imageProxy = imageProxy,
            isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
        )
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
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.6F
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.6F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.6F
        const val DEFAULT_NUM_HANDS = 1
        const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_POSES = 1
        const val MODEL_POSE_LANDMARKER_FULL = 0
        const val MODEL_POSE_LANDMARKER_LITE = 1
        const val MODEL_POSE_LANDMARKER_HEAVY = 2
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    data class Configuration(
        val delegate: Int = DELEGATE_CPU,
        val minHandDetectionConfidence: Float = DEFAULT_HAND_DETECTION_CONFIDENCE,
        val minHandTrackingConfidence: Float = DEFAULT_HAND_TRACKING_CONFIDENCE,
        val minHandPresenceConfidence: Float = DEFAULT_HAND_PRESENCE_CONFIDENCE,
        val maxHands: Int = DEFAULT_NUM_HANDS,
        val poseModel: Int = MODEL_POSE_LANDMARKER_FULL,
        val minPoseDetectionConfidence: Float = DEFAULT_POSE_DETECTION_CONFIDENCE,
        val minPoseTrackingConfidence: Float = DEFAULT_POSE_TRACKING_CONFIDENCE,
        val minPosePresenceConfidence: Float = DEFAULT_POSE_PRESENCE_CONFIDENCE,
        val maxPoses: Int = DEFAULT_NUM_POSES
    )


    override fun onError(error: String, errorCode: Int) {
        Log.e("MediaPipeService", "Error: $error")
    }

    override fun onResults(resultBundle: LandmarkerHelper.ResultBundle) {
        Log.d("MediaPipeService", "Results: ${resultBundle.results.first().worldLandmarks()}")
    }


}