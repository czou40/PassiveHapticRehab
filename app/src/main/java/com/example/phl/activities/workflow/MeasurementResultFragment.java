package com.example.phl.activities.workflow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.phl.R;
import com.example.phl.databinding.FragmentMeasurementResultBinding;

public class MeasurementResultFragment extends Fragment {

//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    private double result;
    private FragmentMeasurementResultBinding binding;


    public MeasurementResultFragment() {
        // Required empty public constructor
    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment MeasurementResultFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static MeasurementResultFragment newInstance(String param1, String param2) {
//        MeasurementResultFragment fragment = new MeasurementResultFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("result")) {
            result = getArguments().getDouble("result");
        } else {
            throw new IllegalArgumentException("MeasurementResultFragment must be created with a result");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMeasurementResultBinding.inflate(inflater, container, false);
        binding.textviewMeasurementResult.setText(String.valueOf(result));
        binding.buttonMeasurementResult.setOnClickListener(v -> {
            NavHostFragment.findNavController(MeasurementResultFragment.this)
                    .navigate(R.id.action_measurementResultFragment_to_FirstFragment);
        });
        return binding.getRoot();
    }
}