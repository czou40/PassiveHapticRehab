package com.example.phl.services

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.icu.text.ListFormatter.Width
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Toast
import androidx.camera.camera2.internal.Camera2CameraInfoImpl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.phl.R
import com.example.phl.utils.HandLandmarkerHelper
import com.example.phl.utils.PoseLandmarkerHelper
import com.example.phl.views.HandLandmarkerOverlayView
import com.example.phl.views.HolisticOverlayView
import com.example.phl.views.PoseLandmarkerOverlayView
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.io.ByteArrayOutputStream
import java.net.DatagramSocket
import java.net.DatagramPacket
import java.net.InetAddress

class MediaPipeService : LifecycleService(), HandLandmarkerHelper.LandmarkerListener, PoseLandmarkerHelper.LandmarkerListener {

    private val executor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var isStreaming = AtomicBoolean(false)
    private var isCameraReady = AtomicBoolean(false)
    private var startStreamingWhenReady = false
    private var preview: Preview? = null

    private var cameraFacing = CameraSelector.LENS_FACING_FRONT

    private val binder = LocalBinder()

    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper

    private var configuration: Configuration = Configuration()

    private var overlayView: HolisticOverlayView? = null

    private lateinit var activityContext: Activity

    // Class used for the client Binder.
    inner class LocalBinder : Binder() {

//        fun setConfiguration(config: Configuration) {
//            configuration = config
//        }
//        fun getConfiguration(): Configuration = configuration

        private var previewView: PreviewView? = null

        private var width: Int = 352
        private var height: Int = 288

        fun setStartStreamingWhenReady(start: Boolean,previewView: PreviewView? = null, width: Int? = null, height: Int? = null, overlayView: HolisticOverlayView? = null) {
            startStreamingWhenReady = start
            this.previewView = previewView
            this.width = width ?: this.width
            this.height = height ?: this.height
            this@MediaPipeService.overlayView = overlayView
        }

        fun startStreaming(previewView: PreviewView? = null, width: Int? = null, height: Int? = null, overlayView: HolisticOverlayView? = null) {
            this.previewView = previewView ?: this.previewView
            this@MediaPipeService.overlayView = overlayView ?: this@MediaPipeService.overlayView
            this.width = width ?: this.width
            this.height = height ?: this.height
            if (isCameraReady.get()) {
                if (!isStreaming.get()) {
                    isStreaming.set(true)
                    startCamera(this.previewView, this.width, this.height)
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

        fun setActivityContext(activity: Activity) {
            activityContext = activity
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
            if (startStreamingWhenReady) {
                binder.startStreaming()
            }
        }, ContextCompat.getMainExecutor(this))

        // Create the HandLandmarkerHelper that will handle the inference
        executor.execute {
            handLandmarkerHelper = HandLandmarkerHelper(
                context = applicationContext,
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = configuration.minHandDetectionConfidence,
                minHandTrackingConfidence = configuration.minHandTrackingConfidence,
                minHandPresenceConfidence = configuration.minHandPresenceConfidence,
                maxNumHands = configuration.maxHands,
                currentDelegate = configuration.delegate,
                handLandmarkerHelperListener = this
            )

            poseLandmarkerHelper = PoseLandmarkerHelper(
                context = applicationContext,
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = configuration.minPoseDetectionConfidence,
                minPoseTrackingConfidence = configuration.minPoseTrackingConfidence,
                minPosePresenceConfidence = configuration.minPosePresenceConfidence,
                currentModel = configuration.poseModel,
                currentDelegate = configuration.delegate,
                poseLandmarkerHelperListener = this
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MediaPipeService", "Service destroyed")
        stopCamera();  // Ensure camera is stopped
        executor.shutdown();  // Shutdown executor service
    }

    private fun startCamera(previewView: PreviewView?, width: Int=352, height: Int=288) {
        if (!isCameraReady.get()) {
            Log.e("MediaPipeService", "Camera provider is null")
            return
        }
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraFacing).
        build()
        val screenSize = Size(width, height)
        val resolutionSelector = ResolutionSelector.Builder(
        ).setAspectRatioStrategy(
            AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
        ).build()

        val previewResolutionSelector =  ResolutionSelector.Builder(
        ).setAspectRatioStrategy(
            AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
        ).setResolutionStrategy(
            ResolutionStrategy(screenSize,
            ResolutionStrategy.FALLBACK_RULE_NONE)
        ).build()

        val rotation = Surface.ROTATION_270// previewView?.display?.rotation ?: Surface.ROTATION_0

        preview =
            Preview.Builder()
                .setResolutionSelector(previewResolutionSelector)
                .setTargetRotation(rotation)
                .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetRotation(rotation)
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
            .also { analyzer ->
                analyzer.setAnalyzer(executor) { imageProxy ->

                    val bitmapBuffer = convertToBitmap(imageProxy)

                    val mpImage = convertToMPImage(bitmapBuffer)
                    val frameTime = SystemClock.uptimeMillis()
                    detectHand(mpImage, frameTime)
                    detectPose(mpImage, frameTime)
                    // Sending images causes memory leak even if I explicitly recycle the bitmap. I don't know why.
                    // But since we use the image preview view on top of the Unity view, we don't need to send images to Unity. Commenting out.
                    // sendImage(bitmapBuffer)
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

    private fun detectHand(mpImage: MPImage, frameTime: Long) {
//        handLandmarkerHelper.detectLiveStream(
//            imageProxy = imageProxy,
//            isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
//        )
        handLandmarkerHelper.detectAsync(mpImage, frameTime)
    }

    private fun detectPose(mpImage: MPImage, frameTime: Long) {
        if(this::poseLandmarkerHelper.isInitialized) {
//            poseLandmarkerHelper.detectLiveStream(
//                imageProxy = imageProxy,
//                isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
//            )
            poseLandmarkerHelper.detectAsync(mpImage, frameTime)
        }
    }

    private fun convertToBitmap(imageProxy: ImageProxy): Bitmap {
        val isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
        // Copy out RGB bits from the frame to a bitmap buffer
        val bitmapBuffer =
            Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )


        imageProxy.use {
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
        }
        imageProxy.close()

        val matrix = Matrix().apply {
            // Rotate the frame received from the camera to be in the same direction as it'll be shown
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            // flip image if user use front camera
            if (isFrontCamera) {
                postScale(
                    -1f,
                    1f,
                    imageProxy.width.toFloat(),
                    imageProxy.height.toFloat()
                )
            }
        }

        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
            matrix, true
        )

        return rotatedBitmap
    }

    private fun convertToMPImage(bitmapBuffer: Bitmap): MPImage {
        // Convert the input Bitmap object to an MPImage object to run inference
        val mpImage = BitmapImageBuilder(bitmapBuffer).build()
        return mpImage
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
        const val DEFAULT_NUM_HANDS = 2
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
        val delegate: Int = DELEGATE_GPU,
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


    override fun onHandLandmarkerError(error: String, errorCode: Int) {
        Log.e("MediaPipeService", "Error: $error")
    }

    override fun onHandLandmarkerResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
        val result = resultBundle.results.first()
        val handDetected = result.worldLandmarks() != null && result.worldLandmarks().size > 0

        if (handDetected) {
            val worldLandmarks = result.worldLandmarks()
            val handednesses = result.handednesses()
            val data = ArrayList<String>()
            for (i in worldLandmarks.indices) {
                val landmarks = worldLandmarks[i]
                val handedness = handednesses[i][0]
                val visibilities = ArrayList<Float>()
                for (j in landmarks.indices) {
                    val landmark = landmarks[j]
                    val dataForJoint = handedness.categoryName() + "|" + j + "|" + landmark.x() + "|" + landmark.y() + "|" + landmark.z()
                    data.add(dataForJoint)
                    val landmarkVisibility: Float = landmark.visibility().orElse(1.0f)
                    visibilities.add(landmarkVisibility)
                }
                data.add("Visibility" + handedness.categoryName() + "|" + visibilities.joinToString("|"))
            }
            val dataString = data.joinToString("\n")
            sendData(dataString)
        }

        activityContext.runOnUiThread {
            updateOverlayView(null, resultBundle)
        }
    }

    override fun onPoseLandmarkerError(error: String, errorCode: Int) {
        Log.e("MediaPipeService", "Error: $error")
    }

    override fun onPoseLandmarkerResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        val result = resultBundle.results.first()
        val poseDetected = result.worldLandmarks() != null && result.worldLandmarks().size > 0
        if (poseDetected) {
            val worldLandmarks = result.worldLandmarks()[0]
            val data = ArrayList<String>()
            val visibilities = ArrayList<Float>()
            for (j in worldLandmarks.indices) {
                val landmark = worldLandmarks[j]
                val dataForJoint =
                    "Pose|" + j + "|" + landmark.x() + "|" + landmark.y() + "|" + landmark.z()
                val landmarkVisibility: Float = landmark.visibility().orElse(1.0f)
                visibilities.add(landmarkVisibility)
                data.add(dataForJoint)
            }
            data.add("VisibilityPose|" + visibilities.joinToString("|"))
            val dataString = data.joinToString("\n")
            sendData(dataString)
        }

        activityContext.runOnUiThread {
            updateOverlayView(resultBundle, null)
        }
    }

    private fun updateOverlayView(poseResultBundle: PoseLandmarkerHelper.ResultBundle?, handResultBundle: HandLandmarkerHelper.ResultBundle?) {
        // Pass necessary information to HandLandmarkerOverlayView for drawing on the canvas
        assert(poseResultBundle != null || handResultBundle != null) { "Both pose and hand results are null" }
        val imageHeight = poseResultBundle?.inputImageHeight ?: handResultBundle?.inputImageHeight ?: 0
        val imageWidth = poseResultBundle?.inputImageWidth ?: handResultBundle?.inputImageWidth ?: 0
        overlayView?.setResults(
            poseResultBundle?.results?.first(),
            handResultBundle?.results?.first(),
            imageHeight,
            imageWidth,
            RunningMode.LIVE_STREAM
        )
    }

    private fun sendImage(bitmapBuffer: Bitmap) {
        val isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
        val serverAddress = "127.0.0.1"
        val serverPort = 7778

        var stream: ByteArrayOutputStream? = null

        try {
            // Compress the bitmap to JPEG and convert to byte array
            stream = ByteArrayOutputStream()
            bitmapBuffer.compress(Bitmap.CompressFormat.JPEG, 50, stream) // Compress quality is 50
            val imageBytes = stream.toByteArray()

            // Print the size of the compressed image data
            println("Size of image data: ${imageBytes.size / 1024} KB")

            // Send the image data via UDP
            DatagramSocket().use { socket ->
                val packet = DatagramPacket(imageBytes, imageBytes.size, InetAddress.getByName(serverAddress), serverPort)
                try {
                    socket.send(packet)
                } catch (e: Exception) {
                    Log.e("MediaPipeService", "Error sending image data: ${e.message}")
                    e.printStackTrace()
                }
            }
        } finally {
            // Ensure the ByteArrayOutputStream is closed
            stream?.close()
            // Optionally recycle the bitmap if it's no longer needed
            bitmapBuffer.recycle()
        }
    }

    private fun sendData(data: String) {
        val serverAddress = "127.0.0.1"
        val serverPort = 7777

        try {
            // Create a UDP socket
            val socket = DatagramSocket()

            // Get the IP address of the server
            val serverIp = InetAddress.getByName(serverAddress)

            // Convert the message to bytes
            val sendData = data.toByteArray()

            // Create a UDP packet with the data, server IP, and port
            val sendPacket = DatagramPacket(sendData, sendData.size, serverIp, serverPort)

            // Send the packet
            socket.send(sendPacket)

            // Close the socket
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}