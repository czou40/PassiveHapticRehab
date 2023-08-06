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
import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
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
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.phl.data.AppDatabase
import com.example.phl.data.ball.BallTestRaw
import com.example.phl.data.ball.BallTestResult
import com.google.mediapipe.tasks.components.containers.Category
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix

class CameraFragment : Fragment(), HandLandmarkerHelper.LandmarkerListener {

    companion object {
        private const val TAG = "Hand Landmarker"
        private const val OPEN_HAND_TEST_TIME = 10000L
        private const val CLOSE_HAND_TEST_TIME = 10000L
        private const val PREPARE_TIME = 3000L
        private const val SHOW_FINAL_COUNTDOWN_TIME = 3000L
        private const val MINIMUM_DATA_COUNT = 30
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


    //    private var data: MutableList<BallTestRaw> = ArrayList()
    private var currentTestData = ArrayList<BallTestRaw>()

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    private lateinit var testType: String
    private lateinit var sessionId: String

    private var warningToast: Toast? = null

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            findNavController().navigate(R.id.permissions_fragment)
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

        // "testType" must be passed from the previous fragment and must be either "OpenHand" or "CloseHand"
        testType = requireArguments().getString("testType")!!
        assert(testType == "OpenHand" || testType == "CloseHand")

        sessionId = requireArguments().getString("sessionId")!!

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
            // Make sure that all permissions are still present, since the
            // user could have removed them while the app was in paused state.
            if (!PermissionsFragment.hasPermissions(requireContext())) {
                findNavController().navigate(R.id.permissions_fragment)
            } else {
                setUpCamera()
            }
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

        // Schedule stop saving data for 20 seconds later
        val duration = if (testType == "OpenHand") OPEN_HAND_TEST_TIME else CLOSE_HAND_TEST_TIME
        startCountDown(PREPARE_TIME, true) {
            startSavingData()
            startCountDown(duration - SHOW_FINAL_COUNTDOWN_TIME, false) {
                startCountDown(SHOW_FINAL_COUNTDOWN_TIME, true, "Data Collection Ending") {
                    stopSavingData()
                    navigateToNextStep()
                }
            }
        }
    }


    private fun startCountDown(
        milliseconds: Long,
        visible: Boolean,
        message: String? = null,
        callback: () -> Unit
    ) {
        // Find your TextView
        val countdownTextView: TextView = fragmentCameraBinding.countdownText
        if (visible) {
            if (countdownTextView.visibility == View.GONE) {
                countdownTextView.visibility = View.VISIBLE
                // Create a fade in animation
                val fadeInAnimation = AlphaAnimation(0f, 1f)
                fadeInAnimation.duration = 500

                // Start the animation
                countdownTextView.startAnimation(fadeInAnimation)
            }
        } else {
            countdownTextView.visibility = View.GONE
        }
        // Create a CountdownTimer
        object : CountDownTimer(milliseconds, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                if (!visible) {
                    return
                }
                // Update TextView
                if (message != null) {
                    countdownTextView.text =
                        message.trim() + "\n" + ceil(millisUntilFinished / 1000.0).toInt()
                            .toString()
                } else {
                    countdownTextView.text = ceil(millisUntilFinished / 1000.0).toInt().toString()
                }
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
                if (visible) {
                    // Create a fade out animation
                    val fadeOutAnimation = AlphaAnimation(1f, 0f)
                    fadeOutAnimation.duration = 500

                    // Start the animation
                    countdownTextView.startAnimation(fadeOutAnimation)

                    // Set visibility to GONE after the animation and start saving data
                    countdownTextView.postDelayed({
                        countdownTextView.visibility = View.GONE
                        callback()
                    }, 500)
                } else {
                    callback()
                }
            }
        }.start()
    }

    private fun startSavingData() {
        currentTestData.clear()
        isSavingData = true
    }

    private fun stopSavingData() {
        isSavingData = false
    }

    private fun navigateToNextStep() {
        if (currentTestData.size < MINIMUM_DATA_COUNT) {
            // Alert Dialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Warning")
            builder.setMessage("Not enough data collected. Please make sure your hand is facing the camera and having the correct gesture.")
            builder.setPositiveButton("OK") { _, _ ->
                findNavController().popBackStack()
            }
            builder.setCancelable(false)
            builder.show()
        } else {
            // Save the data to the database
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(requireActivity().applicationContext)
                db.ballTestRawDao().insertAll(currentTestData)
            }
            val rawScores = currentTestData.map { it.getStrength() }
            val averageScore = rawScores.average()
            val bundle = bundleOf("sessionId" to sessionId)
            if (testType == "CloseHand") {
                bundle.putDouble("closeHandTestResult", averageScore)
                findNavController().navigate(
                    R.id.action_camera_fragment_to_openHandInstructionFragment,
                    bundle
                )
            } else if (testType == "OpenHand") {
                bundle.putDouble(
                    "closeHandTestResult",
                    arguments?.getDouble("closeHandTestResult")!!
                )
                bundle.putDouble("openHandTestResult", averageScore)
                findNavController().navigate(
                    R.id.action_camera_fragment_to_ballTestResultsFragment3,
                    bundle
                )
            }
        }
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

    // Update UI after hand have been detected. Extracts original
    // image height/width to scale and place the landmarks properly through
    // HandLandmarkerOverlayView
    override fun onResults(
        resultBundle: HandLandmarkerHelper.ResultBundle
    ) {
        val result = resultBundle.results.first()
        val handDetected = result.worldLandmarks() != null && result.worldLandmarks().size > 0

        if (handDetected) {
            val worldLandmarks = result.worldLandmarks()[0]
            val handedness = if (result.handednesses()[0][0]!!.categoryName()!! == "Left") {
                BallTestRaw.Companion.Handedness.LEFT
            } else {
                BallTestRaw.Companion.Handedness.RIGHT
            }
            val currentTask = if (testType == "CloseHand") {
                BallTestRaw.Companion.CurrentTask.CLOSE_HAND
            } else if (testType == "OpenHand" ){
                BallTestRaw.Companion.CurrentTask.OPEN_HAND
            } else {
                BallTestRaw.Companion.CurrentTask.NONE
            }
            val landmarks = result.landmarks()[0]
//            val points = hand.map { landmark ->
//                Point3D(
//                    landmark.x().toDouble(),
//                    landmark.y().toDouble(),
//                    landmark.z().toDouble()
//                )
//            }

            val worldLandmarksArray = (worldLandmarks.map { landmark ->
                doubleArrayOf(
                    landmark.x().toDouble(),
                    landmark.y().toDouble(),
                    landmark.z().toDouble()
                )
            }).toTypedArray()

            val landmarksArray = (landmarks.map { landmark ->
                doubleArrayOf(
                    landmark.x().toDouble() * resultBundle.inputImageWidth,
                    landmark.y().toDouble() * resultBundle.inputImageHeight
                )
            }).toTypedArray()

            val ballTestRaw =
                BallTestRaw(sessionId, worldLandmarksArray, landmarksArray, handedness, currentTask)

            viewLifecycleOwner.lifecycleScope.launch {
                fragmentCameraBinding.strengthText.text = ballTestRaw.getDescription()
            }

            if (isSavingData) {
                val (isValid, reason) = ballTestRaw.isValidData()
                if (!isValid) {
                    activity?.runOnUiThread {
                        warningToast?.cancel()
                        warningToast = FancyToast.makeText(
                            requireContext(),
                            reason,
                            FancyToast.LENGTH_SHORT,
                            FancyToast.WARNING,
                            false
                        )
                        warningToast?.setGravity(Gravity.TOP, 0, 0);
                        warningToast?.show()
                    }
                } else {
                    warningToast?.cancel()
                    currentTestData.add(ballTestRaw)
                }
            }
        } else {
            if (isSavingData) {
                activity?.runOnUiThread {
                    warningToast?.cancel()
                    warningToast = FancyToast.makeText(
                        requireContext(),
                        "No hand detected!",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,
                        false
                    )
                    warningToast?.setGravity(Gravity.TOP, 0, 0);
                    warningToast?.show()
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
