package com.example.phl.activities.spasticity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.phl.R;
import com.example.phl.data.Dataset;
import com.example.phl.databinding.FragmentCalibrationBinding;
import com.example.phl.databinding.FragmentCalibrationObjectListBinding;

import java.util.Arrays;
import java.util.List;

public class CalibrationObjectListFragment extends Fragment {

//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    private FragmentCalibrationObjectListBinding binding;

    public CalibrationObjectListFragment() {
        // Required empty public constructor
    }

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment CalibrationObjectListFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static CalibrationObjectListFragment newInstance(String param1, String param2) {
//        CalibrationObjectListFragment fragment = new CalibrationObjectListFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCalibrationObjectListBinding.inflate(inflater, container, false);
        StringBuilder displayText = new StringBuilder();
        List<Double[]> x = Dataset.getInstance().getX();
        List<Double> y = Dataset.getInstance().getY();
        for (int i = 0; i < x.size(); i++) {
            displayText.append(Arrays.toString(x.get(i))).append(" -> ").append(y.get(i)).append("\n");
        }
        binding.textView.setText(displayText.toString());
        binding.textViewNeeded.setText(String.format(getString(R.string.total_data_points_needed), x.get(0).length + 1));
        binding.addNewObjectButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_calibrationObjectListFragment_to_CalibrationInstructionFragment);
        });
        binding.continueButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_calibrationObjectListFragment_to_measurementInstructionFragment);
        });
        binding.continueButton.setEnabled(x.get(0).length + 1 <= x.size());
        return binding.getRoot();
    }
}