package com.example.phl.activities.workflow;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.phl.R;
import com.example.phl.activities.DiagnosisWorkflowActivity;
import com.example.phl.data.Dataset;
import com.example.phl.data.SensorData;
import com.example.phl.databinding.FragmentCalibrationBinding;
import com.example.phl.databinding.FragmentMeasurementBinding;
import com.example.phl.databinding.FragmentSecondBinding;
import com.example.phl.utils.UnitConverter;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeasurementFragment extends Fragment {
    private  Context mContext;
    private FragmentMeasurementBinding binding;

    CountDownTimer countDownTimer1;
    CountDownTimer countDownTimer2;
    CountDownTimer countDownTimer3;

    SensorData sensorData;

    double result;

    public MeasurementFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null && getArguments().containsKey("weight")) {
//            weight = getArguments().getDouble("weight");
//        } else {
//            throw new IllegalArgumentException("Must pass weight");
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMeasurementBinding.inflate(inflater, container, false);
        binding.continueButton.setVisibility(View.INVISIBLE);
        binding.textviewCountingDown.setText(R.string.counting_down);
        binding.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putDouble("result", result);
                NavHostFragment.findNavController(MeasurementFragment.this)
                        .navigate(R.id.action_measurementFragment_to_measurementResultFragment, bundle);
            }
        });

        countDownTimer1 = new CountDownTimer(3000, 100) {
            public void onTick(long millisUntilFinished) {
                binding.textviewTimer.setText(
                        String.valueOf(millisUntilFinished / 1000)
                );
            }
            public void onFinish() {
                ((DiagnosisWorkflowActivity) requireActivity()).startVibration();
                binding.textviewTimer.setText("");
                countDownTimer2.start();
            }
        };

        countDownTimer2 = new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                sensorData.startCollectingData();
                countDownTimer3.start();
            }
        };

        countDownTimer3 = new CountDownTimer(20000, 100) {
            public void onTick(long millisUntilFinished) {
                binding.textviewTimer.setText(
                        String.valueOf(millisUntilFinished / 1000)
                );
            }

            public void onFinish() {
                sensorData.stopCollectingData();
                result = Dataset.getInstance().predict(sensorData.getDatapoint());
                ((DiagnosisWorkflowActivity) requireActivity()).stopVibration();
                binding.textviewCountingDown.setText(R.string.continue_to_next);
                binding.continueButton.setVisibility(View.VISIBLE);
                binding.textviewTimer.setText("");
            }
        };
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sensorData = new SensorData((SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE));
        binding.ripple.startRippleAnimation();
        countDownTimer1.start();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}