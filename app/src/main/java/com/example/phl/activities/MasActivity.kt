package com.example.phl.activities

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.phl.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlin.math.sqrt

class MasActivity : MyBaseActivity(), SensorEventListener {
    private lateinit var angularVelocityValueTextView: TextView
    private lateinit var angularAccelerationValueTextView: TextView
    private lateinit var graphAngularVelocity: GraphView
    private lateinit var graphAngularAcceleration: GraphView
    private lateinit var graphRadius: GraphView

    private val seriesAngularVelocity = LineGraphSeries<DataPoint>()
    private val seriesAngularAcceleration = LineGraphSeries<DataPoint>()
    private val seriesRadius = LineGraphSeries<DataPoint>()

    private var dataCount = 0

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var accelerometer: Sensor? = null
    private var lastAngularVelocity = DoubleArray(3) { 0.0 }
    private var lastAcceleration = DoubleArray(3) { 0.0 }
    private var velocity = DoubleArray(3) { 0.0 }
    private var lastGyroEventTime: Long = 0
    private var lastAccelEventTime: Long = 0
    private var emaAngularVelocity = DoubleArray(3) { 0.0 }
    private var emaAngularAcceleration = DoubleArray(3) { 0.0 }
    private var emaAcceleration = DoubleArray(3) { 0.0 }
    private var emaVelocity = DoubleArray(3) { 0.0 }

    private val alpha = 0.1 // Smoothing factor for EMA. Adjust as needed.

    private var isTracking = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mas)
        angularVelocityValueTextView = findViewById(R.id.angular_velocity_value)
        angularAccelerationValueTextView = findViewById(R.id.angular_acceleration_value)

        graphAngularVelocity = findViewById(R.id.graph_angular_velocity)
        graphAngularAcceleration = findViewById(R.id.graph_angular_acceleration)
        graphRadius = findViewById(R.id.graph_radius)
        graphAngularVelocity.viewport.isXAxisBoundsManual = true
        graphAngularAcceleration.viewport.isXAxisBoundsManual = true
        graphRadius.viewport.isXAxisBoundsManual = true
//        graphRadius.viewport.isYAxisBoundsManual = true
        graphAngularVelocity.viewport.setMinX(0.0)
        graphAngularVelocity.viewport.setMaxX(100.0)
        graphAngularAcceleration.viewport.setMinX(0.0)
        graphAngularAcceleration.viewport.setMaxX(100.0)
        graphRadius.viewport.setMinX(0.0)
        graphRadius.viewport.setMaxX(100.0)
//        graphRadius.viewport.setMaxY(1.2)
        graphAngularVelocity.addSeries(seriesAngularVelocity)
        graphAngularAcceleration.addSeries(seriesAngularAcceleration)
        graphRadius.addSeries(seriesRadius)

        val startButton = findViewById<TextView>(R.id.start)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        startButton.setOnClickListener {
            if (isTracking) {
                stopTracking()
                startButton.text = "Start"
            } else {
                startTracking()
                startButton.text = "Stop"
            }
        }
    }

    private fun startTracking() {
        isTracking = true
        lastAngularVelocity = DoubleArray(3) { 0.0 }
        lastAcceleration = DoubleArray(3) { 0.0 }
        velocity = DoubleArray(3) { 0.0 }
        lastGyroEventTime = 0
        lastAccelEventTime = 0
        emaAngularVelocity = DoubleArray(3) { 0.0 }
        emaAngularAcceleration = DoubleArray(3) { 0.0 }
        emaAcceleration = DoubleArray(3) { 0.0 }
        emaVelocity = DoubleArray(3) { 0.0 }

        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
    }

    private fun stopTracking() {
        isTracking = false
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = event.timestamp
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                if (lastGyroEventTime != 0L) {
                    val dt = (currentTime - lastGyroEventTime) * 1.0 / 1e9
                    Log.d("dt", dt.toString())
                    val angularVelocity = DoubleArray(3)
                    val angularAcceleration = DoubleArray(3)
                    for (i in 0..2) {
                        angularVelocity[i] = event.values[i].toDouble()
                        angularAcceleration[i] = ((angularVelocity[i] - lastAngularVelocity[i]) / (dt + 1e-100))
                        // Compute EMA of angular velocity and acceleration
                        emaAngularVelocity[i] = alpha * angularVelocity[i] + (1 - alpha) * emaAngularVelocity[i]
                        emaAngularAcceleration[i] = alpha * angularAcceleration[i] + (1 - alpha) * emaAngularAcceleration[i]
                    }
                    Log.d("angularAcceleration",  angularAcceleration[0].toString() + " " + angularAcceleration[1].toString() + " " + angularAcceleration[2].toString())

                    lastAngularVelocity = angularVelocity

//                    val randomBoolean = (0..10).random() == 0
//                    if (randomBoolean) {
//                        angularVelocityValueTextView.text = (sqrt(emaAngularVelocity[0]*emaAngularVelocity[0] + emaAngularVelocity[1]*emaAngularVelocity[1] + emaAngularVelocity[2]*emaAngularVelocity[2]) / Math.PI * 180).toString()
//                        angularAccelerationValueTextView.text = (sqrt(emaAngularAcceleration[0]*emaAngularAcceleration[0] + emaAngularAcceleration[1]*emaAngularAcceleration[1] + emaAngularAcceleration[2]*emaAngularAcceleration[2]) / Math.PI * 180).toString()
//
//                    }
                }
                lastGyroEventTime = currentTime
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                if (lastAccelEventTime != 0L) {
                    val dt = (currentTime - lastAccelEventTime) * 1.0 / 1e9
                    for (i in 0..2) {
                        // Integrate acceleration to get velocity
                        // It is extremely inaccurate!!!!
                        velocity[i] += (event.values[i] + lastAcceleration[i]) / 2.0 * dt

                        // Compute EMA of linear acceleration and velocity
                        emaAcceleration[i] = alpha * event.values[i] + (1 - alpha) * emaAcceleration[i]
                        emaVelocity[i] = alpha * velocity[i] + (1 - alpha) * emaVelocity[i]

                        lastAcceleration[i] = event.values[i].toDouble()
                    }
                    val speed = sqrt(velocity[0]*velocity[0] + velocity[1]*velocity[1] + velocity[2]*velocity[2])

//                    val emaAngularAccelerationMagnitude = sqrt(emaAngularAcceleration[0]*emaAngularAcceleration[0] + emaAngularAcceleration[1]*emaAngularAcceleration[1] + emaAngularAcceleration[2]*emaAngularAcceleration[2])
//                    val emaAccelerationMagnitude = sqrt(emaAcceleration[0]*emaAcceleration[0] + emaAcceleration[1]*emaAcceleration[1] + emaAcceleration[2]*emaAcceleration[2])
//                    val radius = if (emaAngularAccelerationMagnitude >= 5) emaAccelerationMagnitude / emaAngularAccelerationMagnitude else 0.0
//                    Log.d("radius", radius.toString())
                }
                lastAccelEventTime = currentTime
            }
        }
        val emaAngularVelocityMagnitude = sqrt(emaAngularVelocity[0]*emaAngularVelocity[0] + emaAngularVelocity[1]*emaAngularVelocity[1] + emaAngularVelocity[2]*emaAngularVelocity[2])
        val emaAngularAccelerationMagnitude = sqrt(emaAngularAcceleration[0]*emaAngularAcceleration[0] + emaAngularAcceleration[1]*emaAngularAcceleration[1] + emaAngularAcceleration[2]*emaAngularAcceleration[2])
        val emaVelocityMagnitude = sqrt(emaVelocity[0]*emaVelocity[0] + emaVelocity[1]*emaVelocity[1] + emaVelocity[2]*emaVelocity[2])
        val emaAccelerationMagnitude = sqrt(emaAcceleration[0]*emaAcceleration[0] + emaAcceleration[1]*emaAcceleration[1] + emaAcceleration[2]*emaAcceleration[2])
        val radius = if (emaAngularAccelerationMagnitude >= 10) emaAccelerationMagnitude / emaAngularAccelerationMagnitude else 0.0
        seriesAngularVelocity.appendData(DataPoint(dataCount.toDouble(), emaAngularVelocityMagnitude), true, 2000)
        seriesAngularAcceleration.appendData(DataPoint(dataCount.toDouble(), emaAngularAccelerationMagnitude), true, 2000)
        if (radius > 0) seriesRadius.appendData(DataPoint(dataCount.toDouble(), radius), true, 2000)
        dataCount++
        graphAngularVelocity.viewport.setMinX(0.coerceAtLeast(dataCount - 2000).toDouble())
        graphAngularVelocity.viewport.setMaxX(dataCount.toDouble())
        graphAngularAcceleration.viewport.setMinX(0.coerceAtLeast(dataCount - 2000).toDouble())
        graphAngularAcceleration.viewport.setMaxX(dataCount.toDouble())
        graphRadius.viewport.setMinX(0.coerceAtLeast(dataCount - 2000).toDouble())
        graphRadius.viewport.setMaxX(dataCount.toDouble())
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }

    override fun onPause() {
        super.onPause()
        stopTracking()
    }
}