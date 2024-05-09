package com.example.phl.activities.tilt

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.phl.R

class TiltTestInstructionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tilt_test_instruction, container, false)
        val nextButton = view.findViewById<android.widget.Button>(R.id.next_button)
        nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_tiltTestInstructionFragment_to_tiltTestFragment)
        }
        return view
    }
}