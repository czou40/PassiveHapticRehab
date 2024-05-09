package com.example.phl.activities.spasticity

import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.phl.R
import com.example.phl.activities.SpasticityDiagnosisActivity
import com.example.phl.data.AppDatabase
import com.example.phl.data.spasticity.Spasticity
import com.example.phl.data.spasticity.data_collection.RawDataset
import com.example.phl.data.spasticity.data_collection.SensorData
import com.example.phl.databinding.FragmentMeasurementBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

/**
 * A simple [Fragment] subclass.
 */
class MeasurementFragment : Fragment() {
    private var mContext: Context? = null
    private var binding: FragmentMeasurementBinding? = null
    private var isOnLegacyWorkflow = false
    var countDownTimer1: CountDownTimer? = null
    var countDownTimer2: CountDownTimer? = null
    var countDownTimer3: CountDownTimer? = null
    var sensorData: SensorData? = null
    var result = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isOnLegacyWorkflow = (requireActivity() as SpasticityDiagnosisActivity).isOnLegacyWorkflow
        //        if (getArguments() != null && getArguments().containsKey("weight")) {
//            weight = getArguments().getDouble("weight");
//        } else {
//            throw new IllegalArgumentException("Must pass weight");
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer1!!.cancel()
        countDownTimer2!!.cancel()
        countDownTimer3!!.cancel()
        (requireActivity() as SpasticityDiagnosisActivity).stopVibration()
        if (sensorData != null) {
            sensorData!!.stopCollectingData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMeasurementBinding.inflate(inflater, container, false)
        binding!!.continueButton.visibility = View.INVISIBLE
        binding!!.textviewCountingDown.setText(R.string.counting_down)
        binding!!.continueButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putDouble("result", result)
            NavHostFragment.findNavController(this@MeasurementFragment)
                .navigate(R.id.action_measurementFragment_to_measurementResultFragment, bundle)
        }
        countDownTimer1 = object : CountDownTimer(3000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                binding!!.textviewTimer.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                (requireActivity() as SpasticityDiagnosisActivity).startVibration()
                binding!!.textviewTimer.text = ""
                countDownTimer2!!.start()
            }
        }
        countDownTimer2 = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                sensorData!!.startCollectingData()
                countDownTimer3!!.start()
            }
        }
        countDownTimer3 = object : CountDownTimer(20000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                binding!!.textviewTimer.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                sensorData!!.stopCollectingData()
                if (isOnLegacyWorkflow) {
                    result = RawDataset.getInstance().predict(sensorData!!.datapoint)
                } else {
                    RawDataset.getInstance().add(sensorData!!.datapoint, 0.0)
                    result = RawDataset.getInstance().score
                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(context)
                        db.spasticityDao().insert(Spasticity(UUID.randomUUID().toString(), result))
                    }
                }
                (requireActivity() as SpasticityDiagnosisActivity).stopVibration()
                binding!!.textviewCountingDown.setText(R.string.continue_to_next)
                binding!!.continueButton.visibility = View.VISIBLE
                binding!!.textviewTimer.text = ""
            }
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorData =
            SensorData(mContext!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
        binding!!.ripple.startRippleAnimation()
        countDownTimer1!!.start()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}