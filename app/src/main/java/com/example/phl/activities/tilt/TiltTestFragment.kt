package com.example.phl.activities.tilt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.phl.R
import com.example.phl.activities.MyBaseFragment
import com.example.phl.data.ball.BallTest
import com.example.phl.data.ball.BallTestResult
import com.example.phl.data.tilt.TiltTestResult
import com.example.phl.databinding.FragmentTiltTestBinding
import java.util.UUID
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.sqrt

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class TiltTestFragment : MyBaseFragment(), SensorEventListener {

    private var _binding: FragmentTiltTestBinding? = null

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var tiltTextView: TextView? = null

    private var isSavingData = false

    private var sessionId: String? = null

    private var data: MutableList<Double> = ArrayList()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        registerCommand("Exit") {
            activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTiltTestBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tiltTextView = view.findViewById(R.id.text_tilt)
        val countdownTextView = view.findViewById(R.id.countdown_text) as TextView

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun angleWithGravity(x: Float, y: Float, z: Float): Double {
        val magnitude = sqrt(x * x + y * y + z * z)
        val angleInRadians = acos(z / magnitude)

        // Convert to degrees
        return Math.toDegrees(angleInRadians.toDouble())
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]

            val angle = angleWithGravity(x, y, z)


            val pitch = atan2(x.toDouble(), sqrt(y*y + z*z).toDouble()) * (180.0 / Math.PI)
            val roll = atan2(y.toDouble(), sqrt(x*x + z*z).toDouble()) * (180.0 / Math.PI)

            if (isSavingData) {
                data.add(angle)
            }

            // Update UI with the calculated tilt angles
            activity?.runOnUiThread {
                tiltTextView?.text = "Pitch: %.2f, Roll: %.2f\nAngle with gravity: %.2f".format(pitch, roll, angle)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Do nothing
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun startSavingData() {
        sessionId = UUID.randomUUID().toString()
        data = ArrayList()
        isSavingData = true
    }

    private fun stopSavingData() {
        isSavingData = false
        val averageScore = round(data.average()).toInt()
        val bundle = bundleOf("sessionId" to sessionId, "averageScore" to averageScore)
        findNavController().navigate(R.id.action_tiltTestFragment_to_tiltTestResultsFragment, bundle)
    }
}