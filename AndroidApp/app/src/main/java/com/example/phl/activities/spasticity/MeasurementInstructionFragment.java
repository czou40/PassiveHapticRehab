package com.example.phl.activities.spasticity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.phl.R;
import com.example.phl.activities.SpasticityDiagnosisActivity;
import com.example.phl.databinding.FragmentMeasurementInstructionBinding;

public class MeasurementInstructionFragment extends Fragment {

//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    private boolean isOnLegacyWorkflow;


    private FragmentMeasurementInstructionBinding binding;
    public MeasurementInstructionFragment() {
        // Required empty public constructor
    }

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment MeasurementInstructionFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static MeasurementInstructionFragment newInstance(String param1, String param2) {
//        MeasurementInstructionFragment fragment = new MeasurementInstructionFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.isOnLegacyWorkflow = ((SpasticityDiagnosisActivity) requireActivity()).isOnLegacyWorkflow();
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMeasurementInstructionBinding.inflate(inflater, container, false);

        if(this.isOnLegacyWorkflow) {
            binding.legacy.setVisibility(View.VISIBLE);
            binding.newWorkflow.setVisibility(View.GONE);
        } else {
            binding.legacy.setVisibility(View.GONE);
            binding.newWorkflow.setVisibility(View.VISIBLE);
        }

        binding.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(MeasurementInstructionFragment.this)
                        .navigate(R.id.action_measurementInstructionFragment_to_measurementFragment);
            }
        });
        return binding.getRoot();
    }
}