package com.example.phl.activities.spasticity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.phl.R;
import com.example.phl.data.spasticity.Dataset;
import com.example.phl.data.spasticity.SensorData;
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
    GraphView graphView3;
    Toast toast;
    List<Float> pressures = new LinkedList<>();
    List<Float> gyroscopeX = new LinkedList<>();
    List<Float> gyroscopeY = new LinkedList<>();
    List<Float> gyroscopeZ = new LinkedList<>();
    List<Float> accelerometerX = new LinkedList<>();
    List<Float> accelerometerY = new LinkedList<>();
    List<Float> accelerometerZ = new LinkedList<>();

    boolean isVibrating = false;
    LineGraphSeries<DataPoint> screenSensorDataSeries;
    LineGraphSeries<DataPoint> gyroscopeXDataSeries;
    LineGraphSeries<DataPoint> gyroscopeYDataSeries;
    LineGraphSeries<DataPoint> gyroscopeZDataSeries;
    LineGraphSeries<DataPoint> accelerometerXDataSeries;
    LineGraphSeries<DataPoint> accelerometerYDataSeries;
    LineGraphSeries<DataPoint> accelerometerZDataSeries;


    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor;

//    boolean showGyroscope = false;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        binding.buttonFirst
                .setText(R.string.vibrate);
        graphView =  binding.graph;
        graphView2 = binding.graph2;
        graphView3 = binding.graph3;

        screenSensorDataSeries = new LineGraphSeries<>();
        gyroscopeXDataSeries = new LineGraphSeries<>();
        gyroscopeYDataSeries = new LineGraphSeries<>();
        gyroscopeZDataSeries = new LineGraphSeries<>();
        accelerometerXDataSeries = new LineGraphSeries<>();
        accelerometerYDataSeries = new LineGraphSeries<>();
        accelerometerZDataSeries = new LineGraphSeries<>();
        gyroscopeXDataSeries.setColor(Color.RED);
        gyroscopeYDataSeries.setColor(Color.GREEN);
        gyroscopeZDataSeries.setColor(Color.BLUE);
        accelerometerXDataSeries.setColor(Color.RED);
        accelerometerYDataSeries.setColor(Color.GREEN);
        accelerometerZDataSeries.setColor(Color.BLUE);


        graphView.addSeries(screenSensorDataSeries);

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(100);

        // set manual X bounds
        graphView2.getViewport().setXAxisBoundsManual(true);
//        graphView2.addSeries(gyroscopeXDataSeries);
//        graphView2.addSeries(gyroscopeYDataSeries);
        graphView2.addSeries(gyroscopeZDataSeries);

        graphView3.getViewport().setXAxisBoundsManual(true);
//        graphView3.addSeries(accelerometerXDataSeries);
//        graphView3.addSeries(accelerometerYDataSeries);
        graphView3.addSeries(accelerometerZDataSeries);


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

        binding.buttonWorkflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_calibrationInstructionFragment);
        }});

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
//                    FileWriter.writeToCSV("data.csv", Arrays.asList("Gyroscope X", "Gyroscope Y", "Gyroscope Z", "Accelerometer X", "Accelerometer Y", "Accelerometer Z"), Arrays.asList(gyroscopeX, gyroscopeY, gyroscopeZ, accelerometerX, accelerometerY, accelerometerZ));
//                    Toast.makeText(mContext, "Data saved to data.csv", Toast.LENGTH_SHORT).show();
                } else {
                    FirstFragment.this.isVibrating = true;
                    long[] pattern = {0, 30000};
                    VibrationEffect effect = VibrationEffect.createWaveform(pattern, 1);
//                    VibrationEffect effect = VibrationEffect.createOneShot(65535, 255);
                    vibrator.vibrate(effect);
                    binding.buttonFirst.setText(R.string.stop);
                    // Register the gyroscope sensor listener
                    sensorManager.registerListener(FirstFragment.this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
                    sensorManager.registerListener(FirstFragment.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

                    DataPoint[] empty = {};
                    gyroscopeXDataSeries.resetData(empty);
                    gyroscopeYDataSeries.resetData(empty);
                    gyroscopeZDataSeries.resetData(empty);

                    accelerometerXDataSeries.resetData(empty);
                    accelerometerYDataSeries.resetData(empty);
                    accelerometerZDataSeries.resetData(empty);
                }
            }
        });

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        // Get an instance of the gyroscope sensor
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
            gyroscopeXDataSeries.appendData(new DataPoint(gyroscopeX.size(), x), false, 500);
            gyroscopeYDataSeries.appendData(new DataPoint(gyroscopeY.size(), y), false, 500);
            gyroscopeZDataSeries.appendData(new DataPoint(gyroscopeZ.size(), z), false, 500);
            graphView2.getViewport().setMinX(Math.max(0, gyroscopeX.size() - 500));
            graphView2.getViewport().setMaxX(gyroscopeX.size());
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Get the accelerometer data
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            // Do something with the gyroscope data
            Log.d("Accelerometer", "x: " + x + ", y: " + y + ", z: " + z);
            accelerometerX.add(x);
            accelerometerY.add(y);
            accelerometerZ.add(z);
            accelerometerXDataSeries.appendData(new DataPoint(accelerometerX.size(), x), false, 500);
            accelerometerYDataSeries.appendData(new DataPoint(accelerometerY.size(), y), false, 500);
            accelerometerZDataSeries.appendData(new DataPoint(accelerometerZ.size(), z), false, 500);
            graphView3.getViewport().setMinX(Math.max(0, accelerometerX.size() - 500));
            graphView3.getViewport().setMaxX(accelerometerX.size());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Dataset.reset(SensorData.includedSensors.length);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}