package com.example.phl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.phl.databinding.FragmentFirstBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.LinkedList;
import java.util.List;

public class FirstFragment extends Fragment implements SensorEventListener {

    private FragmentFirstBinding binding;
    private Context mContext;
    GraphView graphView;
    GraphView graphView2;
    Toast toast;
    List<Float> pressures = new LinkedList<>();
    List<Float> gyroscopeX = new LinkedList<>();
    List<Float> gyroscopeY = new LinkedList<>();
    List<Float> gyroscopeZ = new LinkedList<>();

    boolean isVibrating = false;
    LineGraphSeries<DataPoint> screenSensorDataSeries;
    LineGraphSeries<DataPoint> gyroscopeXDataSeries;
    LineGraphSeries<DataPoint> gyroscopeYDataSeries;
    LineGraphSeries<DataPoint> gyroscopeZDataSeries;


    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

//    boolean showGyroscope = false;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        binding.buttonFirst
                .setText(R.string.vibrate);
        graphView =  binding.graph;
        graphView2 = binding.graph2;

        screenSensorDataSeries = new LineGraphSeries<DataPoint>();
        gyroscopeXDataSeries = new LineGraphSeries<DataPoint>();
        gyroscopeYDataSeries = new LineGraphSeries<DataPoint>();
        gyroscopeZDataSeries = new LineGraphSeries<DataPoint>();

        graphView.addSeries(screenSensorDataSeries);

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(100);

        // set manual X bounds
        graphView2.getViewport().setXAxisBoundsManual(true);
        graphView2.addSeries(gyroscopeXDataSeries);
        graphView2.addSeries(gyroscopeYDataSeries);
        graphView2.addSeries(gyroscopeZDataSeries);

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
                if (isVibrating) {
                    return true;
                }
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
                int numFingers = motionEvent.getPointerCount();
                Log.i("MotionEvent", String.valueOf(motionEvent.getPointerCount()));
                pressures.add(pressure);
                screenSensorDataSeries.appendData(new DataPoint(pressures.size(), pressure), false, 10000);
                graphView.getViewport().setMaxX(pressures.size() * 1.01);
                binding.textviewFirst.setText(
                        String.format("Pressure: %.4f\tArea: %.4f\tNum Fingers: %d\n%s",
                                pressure, area, numFingers,
                                numFingers > 1 ? "Touchscreen sensor readings are not accurate for multiple fingers.": ""));

                return true;
            }
        });

        binding.switch1.setVisibility(View.GONE);
//        binding.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                showGyroscope = isChecked;
//            }
//        });

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
                    // Unregister the gyroscope sensor listener
                    sensorManager.unregisterListener(FirstFragment.this);
                } else {
                    FirstFragment.this.isVibrating = true;
                    long[] pattern = {0, 30000};
                    VibrationEffect effect = VibrationEffect.createWaveform(pattern, 1);
                    vibrator.vibrate(effect);
                    binding.buttonFirst.setText(R.string.stop);
                    // Register the gyroscope sensor listener
                    sensorManager.registerListener(FirstFragment.this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
                    DataPoint[] empty = {};
                    gyroscopeXDataSeries.resetData(empty);
                    gyroscopeYDataSeries.resetData(empty);
                    gyroscopeZDataSeries.resetData(empty);
                }
            }
        });

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        // Get an instance of the gyroscope sensor
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Check if the sensor event is from the gyroscope sensor
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Get the gyroscope data
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            // Do something with the gyroscope data
            Log.d("Gyroscope", "x: " + x + ", y: " + y + ", z: " + z);
            gyroscopeX.add(x);
            gyroscopeY.add(y);
            gyroscopeZ.add(z);
            gyroscopeXDataSeries.appendData(new DataPoint(gyroscopeX.size(), x), false, 1000);
            gyroscopeYDataSeries.appendData(new DataPoint(gyroscopeY.size(), y), false, 1000);
            gyroscopeZDataSeries.appendData(new DataPoint(gyroscopeZ.size(), z), false, 1000);
            graphView2.getViewport().setMinX(Math.max(0, gyroscopeX.size() - 1000));
            graphView2.getViewport().setMaxX(gyroscopeX.size());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}