package com.example.phl.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.ui.AppBarConfiguration;

import com.example.phl.data.spasticity.data_collection.RawDataset;
import com.example.phl.data.spasticity.data_collection.SensorData;
import com.example.phl.databinding.ActivitySpasticityDiagnosisBinding;
import com.example.phl.services.RemoteControlService;

import java.time.LocalDate;

public class SpasticityDiagnosisActivity extends MyBaseActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivitySpasticityDiagnosisBinding binding;

//    List<Float> pressures = new LinkedList<>();

    private Vibrator vibrator;
    private boolean isVibrationOn = false;

    private boolean isOnLegacyWorkflow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        binding = ActivitySpasticityDiagnosisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RawDataset.initializeInstance(SensorData.includedSensors.length);
    }

    public void startVibration() {
        if (!isVibrationOn) {
            isVibrationOn = true;
            long[] pattern = {0, 65535};
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, 1);
            vibrator.vibrate(effect);
        }
    }

    public void startVibration(int seconds) {
        if (!isVibrationOn) {
            isVibrationOn = true;
            long[] pattern = {0, seconds * 1000L};
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0);
            vibrator.vibrate(effect);
        }
    }

    public void stopVibration() {
        if (isVibrationOn) {
            vibrator.cancel();
            isVibrationOn = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopVibration();
    }

    public boolean isOnLegacyWorkflow() {
        return isOnLegacyWorkflow;
    }

    public void setOnLegacyWorkflow(boolean onLegacyWorkflow) {
        isOnLegacyWorkflow = onLegacyWorkflow;
    }
}