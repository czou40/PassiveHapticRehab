package com.example.phl.data.spasticity.data_collection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SensorData  implements SensorEventListener  {
//    List<Float> pressures = new ArrayList<>();
    private List<Double> gyroscopeX = new ArrayList<>();
    private List<Double> gyroscopeY = new ArrayList<>();
    private List<Double> gyroscopeZ = new ArrayList<>();

    private List<Double> gyroscopeMagnitude = new ArrayList<>();
    private List<Double> accelerometerX = new ArrayList<>();
    private List<Double> accelerometerY = new ArrayList<>();
    private List<Double> accelerometerZ = new ArrayList<>();

    private List<Double> accelerometerMagnitude = new ArrayList<>();

    private HashMap<String, List<Double>> map = new HashMap<>();

    public static final String[] includedSensors = new String[]{ "accelerometerZ"};


    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor;

    public SensorData(SensorManager sensorManager) {
        map.put("gyroscopeX", gyroscopeX);
        map.put("gyroscopeY", gyroscopeY);
        map.put("gyroscopeZ", gyroscopeZ);
        map.put("accelerometerX", accelerometerX);
        map.put("accelerometerY", accelerometerY);
        map.put("accelerometerZ", accelerometerZ);
        map.put("gyroscopeMagnitude", gyroscopeMagnitude);
        map.put("accelerometerMagnitude", accelerometerMagnitude);
        this.sensorManager = sensorManager;
    }

    public void add(SensorEvent sensorEvent) {
        // Check if the sensor event is from the gyroscope sensor
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Get the gyroscope data
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            // Do something with the gyroscope data
            Log.d("Gyroscope", "x: " + x + ", y: " + y + ", z: " + z);
            gyroscopeX.add((double) x);
            gyroscopeY.add((double) y);
            gyroscopeZ.add((double) z);
            gyroscopeMagnitude.add(Math.sqrt(x * x + y * y + z * z));
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Get the accelerometer data
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            // Do something with the accelerometer data
            Log.d("Accelerometer", "x: " + x + ", y: " + y + ", z: " + z);
            accelerometerX.add((double) x);
            accelerometerY.add((double) y);
            accelerometerZ.add((double) z);
            accelerometerMagnitude.add(Math.sqrt(x * x + y * y + z * z));
        }
    }

    public Double[] getDatapoint() {
//        Double[] averages = new Double[includedSensors.length];
//        for (int i = 0; i < includedSensors.length; i++) {
//            String sensor = includedSensors[i];
//            int finalI = i;
//            Objects.requireNonNull(map.get(sensor)).stream().mapToDouble(Double::doubleValue).average().ifPresent(average -> averages[finalI] = average);
//        }
        Double[][] squaredDifferences = new Double[includedSensors.length][];
        for (int i = 0; i < includedSensors.length; i++) {
            String sensor = includedSensors[i];
            Double[] sensorData = new Double[Objects.requireNonNull(map.get(sensor)).size()];
            sensorData = Objects.requireNonNull(map.get(sensor)).toArray(sensorData);
            squaredDifferences[i] = calculateSmoothedAverage(sensorData, 20);
            for (int j = 0; j < squaredDifferences[i].length; j++) {
                squaredDifferences[i][j] = squaredDifferences[i][j] - sensorData[j];
                squaredDifferences[i][j] = squaredDifferences[i][j] * squaredDifferences[i][j];
            }
        }

        Double[] standardDeviation = new Double[includedSensors.length];
        for (int i = 0; i < includedSensors.length; i++) {
            int finalI = i;
            Arrays.stream(squaredDifferences[i]).mapToDouble(Double::doubleValue).average().ifPresent(average -> standardDeviation[finalI] = Math.sqrt(average));
        }
        return standardDeviation;
    }

    public static Double[] calculateSmoothedAverage(Double[] values, int windowSize) {
        Double[] smoothedAverages = new Double[values.length];
        int n = values.length;

        if (n == 0 || windowSize <= 1) {
            return values.clone();
        }

        if (windowSize > n) {
            windowSize = n;
        }

        int paddingSizeLeft = (windowSize - 1) / 2;
        int paddingSizeRight = windowSize - 1 - paddingSizeLeft;

        double windowSum = 0.0;
        for (int i = 0; i < windowSize; i++) {
            windowSum += values[i];
        }

        for (int i = 0; i <= paddingSizeLeft; i++) {
            smoothedAverages[i] = windowSum / windowSize;
        }

        for (int i = windowSize; i < n; i++) {
            windowSum += values[i] - values[i - windowSize];
            smoothedAverages[i - windowSize + paddingSizeLeft + 1] = windowSum / windowSize;
        }

        for (int i = n - paddingSizeRight; i < n; i++) {
            smoothedAverages[i] = windowSum / windowSize;
        }

        return smoothedAverages;
    }

    public void startCollectingData() {
//        // Get an instance of the SensorManager
//        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

        // Get an instance of the gyroscope sensor
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register the gyroscope sensor listener
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public Double[] stopCollectingData() {
        // Unregister the gyroscope sensor listener
        sensorManager.unregisterListener(this);
        return getDatapoint();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        add(sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
