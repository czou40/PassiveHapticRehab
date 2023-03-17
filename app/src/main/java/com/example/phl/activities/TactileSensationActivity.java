package com.example.phl.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.phl.R;
import com.skyfishjy.library.RippleBackground;

import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TactileSensationActivity extends AppCompatActivity {

//    private SpeechRecognizer speechRecognizer;

    private final int MAX_AMPLITUDE = 255;

    private final int MIN_AMPLITUDE = 1;

    private final int MIN_INCREMENT = 3;

    private int amplitude = MAX_AMPLITUDE;
    private int increment = MAX_AMPLITUDE / 2;
    private int smallestSensedAmplitude = 0;

    private Button vibrateButton;
    private LinearLayout resultLayout;
    private Button feltVibrationButton;
    private Button didNotFeelVibrationButton;

    private Button notSureButton;

    private RippleBackground ripple;

    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tactile_sensation);

        vibrateButton = findViewById(R.id.button);
        resultLayout = findViewById(R.id.result_layout);
        feltVibrationButton = findViewById(R.id.felt_vibration_button);
        didNotFeelVibrationButton = findViewById(R.id.did_not_feel_vibration_button);
        notSureButton = findViewById(R.id.i_am_not_sure_button);
        textView = findViewById(R.id.text_view);
        ripple = findViewById(R.id.ripple);

        textView.setVisibility(View.VISIBLE);
        ripple.setVisibility(View.GONE);
        resultLayout.setVisibility(View.GONE);
        vibrateButton.setVisibility(View.VISIBLE);

        vibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayVibrationInterface();
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                if (vibrator != null) {
                    vibrator.vibrate(VibrationEffect.createOneShot(65535, amplitude));
                }

                new CountDownTimer(2000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        if (vibrator != null) {
                            vibrator.cancel();
                        }
                        displayResultInterface();
                    }
                }.start();
            }
        });

        feltVibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amplitude == MIN_AMPLITUDE || increment < MIN_INCREMENT) {
                    smallestSensedAmplitude = amplitude;
                    Toast.makeText(TactileSensationActivity.this, "The smallest amplitude you can feel is " + smallestSensedAmplitude, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    amplitude -= increment;
                    increment /= 2;
                }
                resetInterface();
            }
        });

        didNotFeelVibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amplitude == MAX_AMPLITUDE) {
                    Toast.makeText(TactileSensationActivity.this, "The smallest amplitude you can feel is " + amplitude, Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (increment < MIN_INCREMENT) {
                    smallestSensedAmplitude = amplitude + increment;
                    Toast.makeText(TactileSensationActivity.this, "The smallest amplitude you can feel is " + smallestSensedAmplitude, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    amplitude += increment;
                    increment /= 2;
                }
                resetInterface();
            }
        });

        notSureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetInterface();
            }
        });
    }

    private void resetInterface() {
        resultLayout.setVisibility(View.GONE);
        vibrateButton.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        ripple.stopRippleAnimation();
        ripple.setVisibility(View.GONE);
    }

    private void displayVibrationInterface() {
        vibrateButton.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        ripple.setVisibility(View.VISIBLE);
        ripple.startRippleAnimation();
        resultLayout.setVisibility(View.GONE);
    }

    private void displayResultInterface() {
        ripple.stopRippleAnimation();
        ripple.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        vibrateButton.setVisibility(View.GONE);
    }
}