package com.example.phl.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.ui.AppBarConfiguration;

import com.example.phl.data.Dataset;
import com.example.phl.data.SensorData;
import com.example.phl.databinding.ActivityDiagnosisWorkflowBinding;

import java.util.LinkedList;
import java.util.List;

public class DiagnosisWorkflowActivity extends AppCompatActivity{

    private AppBarConfiguration appBarConfiguration;
    private ActivityDiagnosisWorkflowBinding binding;

//    List<Float> pressures = new LinkedList<>();

    private Vibrator vibrator;
    private boolean isVibrationOn = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        binding = ActivityDiagnosisWorkflowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Dataset.initializeInstance(SensorData.includedSensors.length);


//        setSupportActionBar(binding.toolbar);
//
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }

    public void startVibration() {
        isVibrationOn = true;
        long[] pattern = {0, 65535};
        VibrationEffect effect = VibrationEffect.createWaveform(pattern, 1);
        vibrator.vibrate(effect);
    }

    public void startVibration(int seconds) {
        isVibrationOn = true;
        long[] pattern = {0, seconds * 1000};
        VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0);
        vibrator.vibrate(effect);
    }

    public void stopVibration() {
        vibrator.cancel();
        isVibrationOn = false;
    }

    @Override
    public void onBackPressed() {

    }
}