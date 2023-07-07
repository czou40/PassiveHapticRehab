package com.example.phl.activities.tilt

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.phl.R
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass.
 * Use the [TiltTestResultsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TiltTestResultsFragment : Fragment() {
    private var averageScore by Delegates.notNull<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            averageScore = it.getDouble("averageScore")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tilt_test_results, container, false)
        val resultTextView = view.findViewById<android.widget.TextView>(R.id.score_value)
        resultTextView.text = averageScore.toString()
        val button = view.findViewById<android.widget.Button>(R.id.exit_button)
        button.setOnClickListener {
            activity?.finish()
        }
        return view
    }

}