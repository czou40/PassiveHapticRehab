package com.example.phl.activities.spasticity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.phl.R

class TouchscreenTestResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_touchscreen_test_result, container, false)
        val pressure1 = arguments?.getDouble("pressure1")
        val pressure2 = arguments?.getDouble("pressure2")
        val averagePressureRelaxedTextView = view.findViewById<TextView>(R.id.average_pressure_relaxed)
        val averagePressureLiftedTextView = view.findViewById<TextView>(R.id.average_pressure_lifted)
        averagePressureRelaxedTextView.text = pressure1.toString()
        averagePressureLiftedTextView.text = pressure2.toString()
        val continueButton = view.findViewById<TextView>(R.id.continue_button)
        continueButton.setOnClickListener {
            requireActivity().finish()
        }
        return view
    }

}