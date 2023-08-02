/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.phl.activities.ball

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.phl.R
import com.example.phl.data.HandLandmarkerViewModel
import com.example.phl.databinding.FragmentCameraBinding
import com.example.phl.utils.HandLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.sqrt
import android.view.animation.ScaleAnimation
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.math.MathUtils
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.phl.data.AppDatabase
import com.example.phl.data.ball.BallTestRaw
import com.example.phl.data.ball.BallTestResult
import com.example.phl.utils.GestureCalculationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix

class CameraFragment : Fragment(), HandLandmarkerHelper.LandmarkerListener {

    companion object {
        private const val TAG = "Hand Landmarker"
    }

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private val viewModel: HandLandmarkerViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT

    private var isSavingData = false

    private var sessionId: String? = null

    private var data: MutableList<BallTestRaw> = ArrayList()

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            ).navigate(R.id.action_camera_to_permissions)
        }

        // Start the HandLandmarkerHelper again when users come back
        // to the foreground.
        backgroundExecutor.execute {
            if (handLandmarkerHelper.isClose()) {
                handLandmarkerHelper.setupHandLandmarker()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::handLandmarkerHelper.isInitialized) {
            viewModel.setMaxHands(handLandmarkerHelper.maxNumHands)
            viewModel.setMinHandDetectionConfidence(handLandmarkerHelper.minHandDetectionConfidence)
            viewModel.setMinHandTrackingConfidence(handLandmarkerHelper.minHandTrackingConfidence)
            viewModel.setMinHandPresenceConfidence(handLandmarkerHelper.minHandPresenceConfidence)
            viewModel.setDelegate(handLandmarkerHelper.currentDelegate)

            // Close the HandLandmarkerHelper and release resources
            backgroundExecutor.execute { handLandmarkerHelper.clearHandLandmarker() }
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Create the HandLandmarkerHelper that will handle the inference
        backgroundExecutor.execute {
            handLandmarkerHelper = HandLandmarkerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
                minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
                minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
                maxNumHands = viewModel.currentMaxHands,
                currentDelegate = viewModel.currentDelegate,
                handLandmarkerHelperListener = this
            )
        }

        // Find your TextView
        val countdownTextView: TextView = fragmentCameraBinding.countdownText

        // Create a CountdownTimer
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update TextView
                countdownTextView.text = ceil(millisUntilFinished / 1000.0).toInt().toString()

                // Create a scale animation
                val scaleAnimation = ScaleAnimation(
                    1f, 1.2f, 1f, 1.2f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                scaleAnimation.duration = 1000
                // Start the animation
                countdownTextView.startAnimation(scaleAnimation)
            }

            override fun onFinish() {
                // Create a fade out animation
                val fadeOutAnimation = AlphaAnimation(1f, 0f)
                fadeOutAnimation.duration = 500

                // Start the animation
                countdownTextView.startAnimation(fadeOutAnimation)

                // Set visibility to GONE after the animation
                countdownTextView.postDelayed({
                    countdownTextView.visibility = View.GONE
                    startSavingData()
                }, 500)

                // Set visibility to GONE after the animation and start saving data
                countdownTextView.postDelayed({
                    countdownTextView.visibility = View.GONE
                    startSavingData()
                    // Schedule stop saving data for 20 seconds later
                    countdownTextView.postDelayed({ stopSavingData() }, 5_000)
                }, 500)
            }
        }.start()


    }

    private fun startSavingData() {
        sessionId = UUID.randomUUID().toString()
        data = ArrayList()
        isSavingData = true
    }

    private fun stopSavingData() {
        isSavingData = false
        val rawScores = data.map { it.getStrength() }
        val averageScore = rawScores.average()
        val result = BallTestResult(sessionId!!, averageScore)
        val oldSessionId = sessionId
        lifecycleScope.launch(Dispatchers.IO) {
            // save average score
            val db = AppDatabase.getInstance(requireContext())
            db.ballTestResultDao().insert(result)
            val ballTests = db.ballTestRawDao().getBySessionId(oldSessionId!!)
            if (ballTests.isNotEmpty()) {
                // print ballTests.random()
                val ballTest = ballTests.random()
                Log.d(TAG, "thumbAngle1: ${ballTest.thumbAngle1}")
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Saved ${ballTests.size} ball tests",
                    Toast.LENGTH_SHORT
                ).show()
                val bundle = bundleOf("sessionId" to oldSessionId, "averageScore" to averageScore)
                findNavController().navigate(R.id.action_camera_fragment_to_ballTestResultsFragment3, bundle)
            }
        }
        sessionId = null
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        detectHand(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectHand(imageProxy: ImageProxy) {
        handLandmarkerHelper.detectLiveStream(
            imageProxy = imageProxy,
            isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    data class Point3D(val x: Double, val y: Double, val z: Double)

    fun vector(point1: Point3D, point2: Point3D): Point3D {
        return Point3D(point2.x - point1.x, point2.y - point1.y, point2.z - point1.z)
    }

    fun dotProduct(vector1: Point3D, vector2: Point3D): Double {
        return vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z
    }

    fun magnitude(vector: Point3D): Double {
        return sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z)
    }

    fun angle(pointA: Point3D, pointB: Point3D, pointC: Point3D): Double {
        val vectorBA = vector(pointB, pointA)
        val vectorBC = vector(pointB, pointC)

        val dotProduct = dotProduct(vectorBA, vectorBC)

        val magnitudeProduct = magnitude(vectorBA) * magnitude(vectorBC)

        val angleInRad = acos(dotProduct / magnitudeProduct)

        // Convert to degrees
        return Math.toDegrees(angleInRad)
    }

    // Update UI after hand have been detected. Extracts original
    // image height/width to scale and place the landmarks properly through
    // HandLandmarkerOverlayView
    override fun onResults(
        resultBundle: HandLandmarkerHelper.ResultBundle
    ) {
        val result = resultBundle.results.first()
        val hands = result.worldLandmarks()
        if (hands != null && hands.size > 0) {
            val hand = hands[0]
            val points = hand.map { landmark ->
                Point3D(
                    landmark.x().toDouble(),
                    landmark.y().toDouble(),
                    landmark.z().toDouble()
                )
            }
            val pointsArray = (hand.map { landmark ->
                doubleArrayOf(
                        landmark.x().toDouble(),
                        landmark.y().toDouble(),
                        landmark.z().toDouble()
                )
            }).toTypedArray()
            val pointsMatrix: RealMatrix = MatrixUtils.createRealMatrix(pointsArray)
            Log.e(TAG, GestureCalculationUtils.gestureSimilarity(pointsMatrix, rotationInvariant = true).toString())
            // thumb = 1, 2, 3, 4
            val thumbAngle0 = angle(points[0], points[1], points[2])
            val thumbAngle1 = angle(points[1], points[2], points[3])
            val thumbAngle2 = angle(points[2], points[3], points[4])
            // index = 5, 6, 7, 8
            val indexAngle0 = angle(points[0], points[5], points[6])
            val indexAngle1 = angle(points[5], points[6], points[7])
            val indexAngle2 = angle(points[6], points[7], points[8])
            // middle = 9, 10, 11, 12
            val middleAngle0 = angle(points[0], points[9], points[10])
            val middleAngle1 = angle(points[9], points[10], points[11])
            val middleAngle2 = angle(points[10], points[11], points[12])
            // ring = 13, 14, 15, 16
            val ringAngle0 = angle(points[0], points[13], points[14])
            val ringAngle1 = angle(points[13], points[14], points[15])
            val ringAngle2 = angle(points[14], points[15], points[16])
            // pinkie = 17, 18, 19, 20
            val pinkieAngle0 = angle(points[0], points[17], points[18])
            val pinkieAngle1 = angle(points[17], points[18], points[19])
            val pinkieAngle2 = angle(points[18], points[19], points[20])


            if (isSavingData) {
                val ballTestRaw = BallTestRaw(
                    requireNotNull(sessionId),
                    thumbAngle0, thumbAngle1, thumbAngle2,
                    indexAngle0, indexAngle1, indexAngle2,
                    middleAngle0, middleAngle1, middleAngle2,
                    ringAngle0, ringAngle1, ringAngle2,
                    pinkieAngle0, pinkieAngle1, pinkieAngle2,
                )
                val strength = ballTestRaw.getStrength().toInt()
                data.add(ballTestRaw)
                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(requireActivity().applicationContext)
                    db.ballTestRawDao().insert(ballTestRaw)
                }
                activity?.runOnUiThread {
                    fragmentCameraBinding.strengthText.text = ballTestRaw.getDescription()
                }
            }
        }

        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                // Pass necessary information to HandLandmarkerOverlayView for drawing on the canvas
                fragmentCameraBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )

                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            if (errorCode == HandLandmarkerHelper.GPU_ERROR) {
                fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
                    HandLandmarkerHelper.DELEGATE_CPU, false
                )
            }
        }
    }
}
