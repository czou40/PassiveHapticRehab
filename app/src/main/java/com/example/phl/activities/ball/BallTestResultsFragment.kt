package com.example.phl.activities.ball

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.phl.R
import kotlin.properties.Delegates

class BallTestResultsFragment : Fragment() {
    private var closeHandTestResult by Delegates.notNull<Double>()
    private var openHandTestResult by Delegates.notNull<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            closeHandTestResult = it.getDouble("closeHandTestResult")
            openHandTestResult = it.getDouble("openHandTestResult")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ball_test_results, container, false)
        val resultTextView = view.findViewById<android.widget.TextView>(R.id.score_value)

        val roundedOpenHandTestResult = kotlin.math.round(openHandTestResult * 100) / 100
        val roundedCloseHandTestResult = kotlin.math.round(closeHandTestResult * 100) / 100
        val score = closeHandTestResult - openHandTestResult
        val roundedScore = kotlin.math.round(score * 100) / 100

        resultTextView.text = "Maximum Extension: $roundedOpenHandTestResult\nMaximum Flexion: $roundedCloseHandTestResult\nScore: $roundedScore"

        val button = view.findViewById<android.widget.Button>(R.id.exit_button)
        button.setOnClickListener {
            activity?.finish()
        }
        return view
    }
}