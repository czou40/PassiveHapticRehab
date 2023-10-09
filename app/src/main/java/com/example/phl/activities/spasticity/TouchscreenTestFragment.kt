package com.example.phl.activities.spasticity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.phl.R
import com.jjoe64.graphview.series.DataPoint
import java.util.LinkedList

class TouchscreenTestFragment : Fragment(), View.OnTouchListener {

    private lateinit var instructionText: TextView
    private lateinit var pressureText: TextView
    private lateinit var countdownText: TextView
    private lateinit var touchLayout: LinearLayout
    private var stage = Stage.NONE
    private var lastMotionEvent: MotionEvent? = null
    private lateinit var pressures1: ArrayList<Pair<Long, Float>>
    private lateinit var pressures2: ArrayList<Pair<Long, Float>>
    private var countDownTimer: CountDownTimer? = null
    private lateinit var handler: Handler
    private lateinit var pollingRunnable: Runnable
    private var fingerLifted = false
    private var lastMotionEventSaved = false

    companion object {
        enum class Stage {
            NONE,
            PRESS_1, // User presses the screen for 1 second, after which we enter the next stage.
            PRESS_2, // User presses the screen for 10 seconds. Data is recorded.
            LIFT_1, // User has 10 seconds to lift their finger off the screen. Data is not recorded. After 10 seconds, we enter the next stage.
            LIFT_2, // The system measures the pressure on the screen for 10 seconds. Data is recorded.
            DONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_touchscreen_test, container, false)
        pressureText = view.findViewById(R.id.pressure_text)
        countdownText = view.findViewById(R.id.countdown_text)
        instructionText = view.findViewById(R.id.instruction_text)
        touchLayout = view.findViewById(R.id.touch_layout)
        stage = Stage.NONE
        pressures1 = ArrayList()
        pressures2 = ArrayList()
        touchLayout.setOnTouchListener(this)
        return view
    }


    private fun showStage(stage: Stage) {
        when (stage) {
            Stage.NONE -> {
                instructionText.text = "Please press the screen with your finger."
                countdownText.visibility = View.GONE
            }

            Stage.PRESS_1 -> {
                instructionText.text = "Please press the screen with your finger."
                countdownText.visibility = View.GONE
            }

            Stage.PRESS_2 -> {
                instructionText.text = "Please press the screen with your finger."
                countdownText.visibility = View.VISIBLE
            }

            Stage.LIFT_1 -> {
                instructionText.text =
                    "Try your best to lift your finger off the screen. You have 10 seconds to do so before the recording starts."
            }

            Stage.LIFT_2 -> {
                instructionText.text =
                    "Try your best to keep your finger off the screen. The recording will last 10 seconds."
            }

            Stage.DONE -> {
                instructionText.text = "Done!"
            }
        }
    }

    private fun moveToNextStage() {
        Log.d("TouchscreenTest", "Moving to next stage: " + stage.toString())
        when (stage) {
            Stage.NONE -> {
                stage = Stage.PRESS_1
                showStage(stage)
                lastMotionEventSaved = false
                fingerLifted = false
                handler = Handler(Looper.getMainLooper())
                pollingRunnable = object : Runnable {
                    override fun run() {
                        checkForTouchPressure()
                        handler.postDelayed(
                            this,
                            10
                        ) // Schedule the next check after 10 milliseconds
                    }
                }
                handler.post(pollingRunnable)
            }

            Stage.PRESS_1 -> {
                stage = Stage.PRESS_2
                showStage(stage)
                createCountdown()
            }

            Stage.PRESS_2 -> {
                stage = Stage.LIFT_1
                showStage(stage)
                createCountdown()
            }

            Stage.LIFT_1 -> {
                stage = Stage.LIFT_2
                showStage(stage)
                createCountdown()
            }

            Stage.LIFT_2 -> {
                stage = Stage.DONE
                showStage(stage)
                handler.removeCallbacks(pollingRunnable)
                if (isAdded && activity != null && view != null && !isDetached) {
                    findNavController().navigate(R.id.action_touchscreenTestFragment_to_touchscreenTestResultFragment,
                        Bundle().apply {
                            putDouble("pressure1", getAveragePressure(pressures1))
                            putDouble("pressure2", getAveragePressure(pressures2))
                        })
                }
            }
            Stage.DONE -> {
                // Do nothing
            }
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(pollingRunnable)
        countDownTimer?.cancel()
    }

    private fun createCountdown() {

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownText.text = "Countdown: " + (millisUntilFinished / 1000 + 1).toString()
            }

            override fun onFinish() {
                moveToNextStage()
            }
        }
        countDownTimer!!.start()
    }

    private fun checkForTouchPressure() {
        var pressure = 0f
        if (lastMotionEvent == null || (lastMotionEventSaved && fingerLifted)) {
            if (stage == Stage.PRESS_2) {
                pressures1.add(Pair(System.currentTimeMillis(), pressure))
            } else if (stage == Stage.LIFT_2) {
                pressures2.add(Pair(System.currentTimeMillis(), pressure))
            }
        } else {
            val p1 = lastMotionEvent as MotionEvent

            val numFingers: Int = p1.pointerCount
            val historySize: Int = p1.historySize
            for (i in 0 until numFingers) {
                var currentFingerPressureSum = p1.getPressure(i).coerceAtMost(2.0f)
                for (j in 0 until historySize) {
                    currentFingerPressureSum += Math.min(p1.getHistoricalPressure(i, j), 2.0f)
                }
                pressure += currentFingerPressureSum / (historySize + 1)
            }
            pressure /= 4 // Divide by 4 because we have 4 fingers on the screen.

            if (stage == Stage.PRESS_2) {
                pressures1.add(Pair(System.currentTimeMillis(), pressure))
            } else if (stage == Stage.LIFT_2) {
                pressures2.add(Pair(System.currentTimeMillis(), pressure))
            }
            lastMotionEventSaved = true
            fingerLifted = p1.action == MotionEvent.ACTION_UP
        }

        pressureText.text = "Pressure per finger: " + pressure.toString()

    }


    override fun onResume() {
        super.onResume()
        moveToNextStage()
    }

    override fun onTouch(p0: View, p1: MotionEvent): Boolean {
        lastMotionEvent = p1
        lastMotionEventSaved = false

        if (stage == Stage.PRESS_1 && countDownTimer == null) {
            countDownTimer = object : CountDownTimer(1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // Do nothing.
                }

                override fun onFinish() {
                    moveToNextStage()
                }
            }
            countDownTimer!!.start()
        }
        return true
    }

    private fun getAveragePressure(pressures: ArrayList<Pair<Long, Float>>): Double {
        if (pressures.isEmpty()) {
            return 0.0
        }
        var sum = 0.0
        for (pressure in pressures) {
            sum += pressure.second
        }
        return sum / pressures.size
    }

}