package com.example.phl.data;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SensorData  implements SensorEventListener  {
//    List<Float> pressures = new LinkedList<>();
    private List<Double> gyroscopeX = new LinkedList<>();
    private List<Double> gyroscopeY = new LinkedList<>();
    private List<Double> gyroscopeZ = new LinkedList<>();
    private List<Double> accelerometerX = new LinkedList<>();
    private List<Double> accelerometerY = new LinkedList<>();
    private List<Double> accelerometerZ = new LinkedList<>();

    private HashMap<String, List<Double>> map = new HashMap<>();

    public static final String[] includedSensors = new String[]{"gyroscopeY", "accelerometerZ"};


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
        }
    }

    public Double[] getDatapoint() {
        Double[] averages = new Double[includedSensors.length];
        for (int i = 0; i < includedSensors.length; i++) {
            String sensor = includedSensors[i];
            int finalI = i;
            Objects.requireNonNull(map.get(sensor)).stream().mapToDouble(Double::doubleValue).average().ifPresent(average -> averages[finalI] = average);
        }
        Double[] standardDeviation = new Double[includedSensors.length];
        for (int i = 0; i < includedSensors.length; i++) {
            String sensor = includedSensors[i];
            int finalI = i;
            Objects.requireNonNull(map.get(sensor)).stream().mapToDouble(Double::doubleValue).map(x -> Math.pow(x - averages[finalI], 2)).average().ifPresent(variance -> standardDeviation[finalI] = Math.sqrt(variance));
        }
        return standardDeviation;
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
