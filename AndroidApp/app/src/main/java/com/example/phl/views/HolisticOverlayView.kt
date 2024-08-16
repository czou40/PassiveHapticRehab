package com.example.phl.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.phl.R
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class HolisticOverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var poseResults: PoseLandmarkerResult? = null
    private var handResults: HandLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        poseResults = null
        handResults = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = Color.RED
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Draw pose landmarks excluding the specific points
        poseResults?.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {
                landmark.forEachIndexed { index, normalizedLandmark ->
                    if (index !in EXCLUDED_POSE_POINTS) {
                        canvas.drawPoint(
                            normalizedLandmark.x() * imageWidth * scaleFactor,
                            normalizedLandmark.y() * imageHeight * scaleFactor,
                            pointPaint
                        )
                    }
                }

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    if (it!!.start() !in EXCLUDED_POSE_POINTS && it.end() !in EXCLUDED_POSE_POINTS) {
                        canvas.drawLine(
                            poseLandmarkerResult.landmarks()[0][it.start()].x() * imageWidth * scaleFactor,
                            poseLandmarkerResult.landmarks()[0][it.start()].y() * imageHeight * scaleFactor,
                            poseLandmarkerResult.landmarks()[0][it.end()].x() * imageWidth * scaleFactor,
                            poseLandmarkerResult.landmarks()[0][it.end()].y() * imageHeight * scaleFactor,
                            linePaint
                        )
                    }
                }
            }
        }

        // Draw hand landmarks
        handResults?.let { handLandmarkerResult ->
            for (landmark in handLandmarkerResult.landmarks()) {
                for (normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }

                HandLandmarker.HAND_CONNECTIONS.forEach {
                    canvas.drawLine(
                        landmark[it!!.start()].x() * imageWidth * scaleFactor,
                        landmark[it.start()].y() * imageHeight * scaleFactor,
                        landmark[it.end()].x() * imageWidth * scaleFactor,
                        landmark[it.end()].y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }
            }

            // Connect pose landmarks 13 or 14 to hand landmark 0
            poseResults?.landmarks()?.getOrElse(0) { null }?.let { poseLandmarks ->
                handLandmarkerResult.handednesses().forEachIndexed { index, handedness ->
                    val poseLandmarkIndex = if (handedness[0].categoryName() == "Right") 14 else 13
                    val handLandmark = handLandmarkerResult.landmarks()[index][0]

                    canvas.drawLine(
                        poseLandmarks[poseLandmarkIndex].x() * imageWidth * scaleFactor,
                        poseLandmarks[poseLandmarkIndex].y() * imageHeight * scaleFactor,
                        handLandmark.x() * imageWidth * scaleFactor,
                        handLandmark.y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }
            }
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult?,
        handLandmarkerResults: HandLandmarkerResult?,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        poseResults = poseLandmarkerResults ?: poseResults
        handResults = handLandmarkerResults ?: handResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8f
        private val EXCLUDED_POSE_POINTS = setOf(16, 18, 20, 22, 15, 17, 19, 21)
    }
}
