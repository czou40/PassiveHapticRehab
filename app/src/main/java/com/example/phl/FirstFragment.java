package com.example.phl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.phl.databinding.FragmentFirstBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.LinkedList;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private Context mContext;
    GraphView graphView;
    Toast toast;
    List<Float> pressures = new LinkedList<>();
    boolean isVibrating = false;
    LineGraphSeries<DataPoint> series;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        binding.buttonFirst
                .setText(R.string.vibrate);
        graphView =  binding.graph;
        series = new LineGraphSeries<DataPoint>();
        graphView.addSeries(series);

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(100);

        return binding.getRoot();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.getRoot().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                    if (toast!=null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(mContext, "You are pressing!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if (motionEvent.getAction()==MotionEvent.ACTION_UP) {
                    if (toast!=null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(mContext, "You released!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                float pressure = motionEvent.getPressure();
                float area = motionEvent.getSize();
                pressures.add(pressure);
                series.appendData(new DataPoint(pressures.size(), pressure), false, 10000);
                graphView.getViewport().setMaxX(pressures.size() * 1.01);
                binding.textviewFirst.setText(String.format("Pressure: %.4f\nArea: %.4f", pressure, area));

                return true;
            }
        });

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                if (FirstFragment.this.isVibrating) {
                    vibrator.cancel();
                    FirstFragment.this.isVibrating = false;
                    binding.buttonFirst.setText(R.string.vibrate);
                } else {
                    FirstFragment.this.isVibrating = true;
                    long[] pattern = {0, 1000};
                    VibrationEffect effect = VibrationEffect.createWaveform(pattern, 1);
                    vibrator.vibrate(effect);
                    binding.buttonFirst.setText(R.string.stop);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}