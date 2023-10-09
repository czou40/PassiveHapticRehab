package com.example.phl.activities.mas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.phl.R
import kotlin.properties.Delegates

class MasResultFragment : Fragment() {

    private var passiveRangeOfMotion: Float? = null
    private var maximumAngularDeceleration: Float? = null // Can approximate the resistance force when a catch, or  muscle tone, happens
    private var maximumAngularDecelerationAngle : Float? = null // Angle with the initial position of the arm when the catch happens
    private var angularDecelerationToAngularVelocityRatio : Float? = null // Can approximate the magnitude of catch, or muscle tone
    private var maximumAngularVelocity: Float? = null
    private var maximumAngularAcceleration: Float? = null // Maximum  angular velocity should be before the catch happens.
    private var angularAccelerationToAngleSlope: Float? = null // Before the catch, what is the slope of the linear regression of the angular acceleration with respect to the joint angle?
    private var angularDecelerationToAngleSlope: Float? = null // After the catch, what is the slope of the linear regression of the angular deceleration with respect to the joint angle?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the arguments passed to this fragment. They may be null.
        arguments?.let {
            passiveRangeOfMotion = it.getFloat("passiveRangeOfMotion")
            maximumAngularDeceleration = it.getFloat("maximumAngularDeceleration")
            maximumAngularDecelerationAngle = it.getFloat("maximumAngularDecelerationAngle")
            angularDecelerationToAngularVelocityRatio = it.getFloat("angularDecelerationToAngularVelocityRatio")
            maximumAngularVelocity = it.getFloat("maximumAngularVelocity")
            maximumAngularAcceleration = it.getFloat("maximumAngularAcceleration")
            angularAccelerationToAngleSlope = it.getFloat("angularAccelerationToAngleSlope")
            angularDecelerationToAngleSlope = it.getFloat("angularDecelerationToAngleSlope")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mas_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val resultTextView = view.findViewById<android.widget.TextView>(R.id.result)
        resultTextView.text = "Passive Range of Motion: $passiveRangeOfMotion\n" +
                "Maximum Angular Deceleration: $maximumAngularDeceleration\n" +
                "Maximum Angular Deceleration Angle: $maximumAngularDecelerationAngle\n" +
//                "Angular Deceleration to Angular Velocity Ratio: $angularDecelerationToAngularVelocityRatio\n" +
                "Maximum Angular Velocity: $maximumAngularVelocity\n" +
                "Maximum Angular Acceleration: $maximumAngularAcceleration\n" //+
//                "Angular Acceleration to Angle Slope: $angularAccelerationToAngleSlope\n" +
//                "Angular Deceleration to Angle Slope: $angularDecelerationToAngleSlope"

        val exitButton = view.findViewById<View>(R.id.exit_button)
        exitButton.setOnClickListener {
            requireActivity().finish()
        }
    }
}