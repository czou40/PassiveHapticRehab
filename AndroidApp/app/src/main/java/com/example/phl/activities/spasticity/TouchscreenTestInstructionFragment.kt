package com.example.phl.activities.spasticity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.phl.R


class TouchscreenTestInstructionFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_touchscreen_test_instruction, container, false)
        view.findViewById<Button>(R.id.continue_button).setOnClickListener {
            findNavController().navigate(R.id.action_touchscreenTestInstructionFragment_to_touchscreenTestInstruction2Fragment)
        }
        return view
    }
}