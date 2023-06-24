package com.example.phl.activities.tilt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.phl.R
import com.example.phl.activities.MyBaseFragment
import com.example.phl.databinding.FragmentTiltTestBinding
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class TiltTestFragment : MyBaseFragment(), SensorEventListener {

    private var _binding: FragmentTiltTestBinding? = null

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var tiltTextView: TextView? = null


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

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
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
}