package com.example.phl.activities.spasticity;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.phl.R;
import com.example.phl.data.Dataset;
import com.example.phl.databinding.FragmentCalibrationInstructionBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class CalibrationInstructionFragment extends Fragment {
    private  Context mContext;
    private FragmentCalibrationInstructionBinding binding;

    private boolean isFirstCalibration = true;

    public CalibrationInstructionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.isFirstCalibration = Dataset.getInstance().size() == 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalibrationInstructionBinding.inflate(inflater, container, false);
        if (isFirstCalibration) {
            binding.textviewPlaceObject1.setVisibility(View.GONE);
            binding.textviewPlaceObject2.setVisibility(View.GONE);
            binding.textviewDoNotPlaceObject.setVisibility(View.VISIBLE);
            binding.weightInputLayout.setVisibility(View.GONE);
            binding.continueButton.setEnabled(true);
        } else {
            binding.textviewPlaceObject1.setVisibility(View.VISIBLE);
            binding.textviewPlaceObject2.setVisibility(View.VISIBLE);
            binding.textviewDoNotPlaceObject.setVisibility(View.GONE);
            binding.weightInputLayout.setVisibility(View.VISIBLE);
            binding.continueButton.setEnabled(false);
        }
        binding.weightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.continueButton.setEnabled(true);
                } else {
                    binding.continueButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                if (!isFirstCalibration) {
                    bundle.putDouble("weight", binding.weightInput.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(binding.weightInput.getText().toString()));
                } else {
                    bundle.putDouble("weight", 0.0);
                }
                NavHostFragment.findNavController(CalibrationInstructionFragment.this)
                        .navigate(R.id.action_calibrationInstructionFragment_to_CalibrationFragment, bundle);
            }
        });
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