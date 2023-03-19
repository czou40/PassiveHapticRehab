package com.example.phl.activities;

import static com.example.phl.utils.PermissionManager.RECORD_AUDIO_PERMISSION_REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.example.phl.R;
import com.example.phl.data.sensation.TactileSensationOperations;
import com.example.phl.utils.PermissionManager;
import com.skyfishjy.library.RippleBackground;

import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TactileSensationActivity extends AppCompatActivity {

    private SpeechRecognizer speechRecognizer;

    MediaPlayer mediaPlayer;

    private final int MAX_AMPLITUDE = 255;

    private final int MIN_AMPLITUDE = 1;

    private final int MIN_INTERVAL_SIZE = 4;


    private int amplitude = MIN_AMPLITUDE;

    private int amplitudeLowerBound = MIN_AMPLITUDE;
    private int amplitudeUpperBound = 999999999;

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
                handleStartVibration();
            }
        });

        feltVibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFeltVibration();
            }
        });

        didNotFeelVibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDidNotFeelVibration();
            }
        });

        notSureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNotSure();
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {
                // display error message
                Toast.makeText(TactileSensationActivity.this, "Error: " + i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String match = matches.get(0).toLowerCase();
                    Log.d("Speech", "onResults: " + match);
                    if (match.equals("yes")) {
                        handleFeltVibration();
                    } else if (match.equals("no")) {
                        handleDidNotFeelVibration();
                    } else if (match.contains("not sure")) {
                        handleNotSure();
                    } else {
                        Toast.makeText(TactileSensationActivity.this, "I didn't understand that", Toast.LENGTH_SHORT).show();
                        handlePlayAudio(R.raw.i_dont_understand_it, new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                startSpeechRecognizer();
                            }
                        });
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        handlePlayAudio(R.raw.please_close_your_eyes, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionManager.checkAndRequestRecordAudioPermission(this);
    }

    private void startSpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say yes, no, or not sure");
        speechRecognizer.startListening(intent);
    }

    private void checkBoundsAndHandleNextStep() {
        if (amplitudeUpperBound - amplitudeLowerBound < MIN_INTERVAL_SIZE) {
            finishTestWithScore(amplitudeUpperBound);
        } else {
            handleStartVibration();
        }
    }

    private void handleStartVibration() {
        displayVibrationInterface();
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (vibrator != null) {
            Log.d("Vibration", "handleStartVibration: " + amplitude);
            vibrator.vibrate(VibrationEffect.createOneShot(3000, amplitude));
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

    private void handleFeltVibration() {
        amplitudeUpperBound = amplitude;
        amplitude = (amplitudeLowerBound + amplitudeUpperBound) / 2;
        checkBoundsAndHandleNextStep();
    }

    private void handleDidNotFeelVibration() {
        amplitudeLowerBound = amplitude;
        amplitude = Math.min(Math.min((amplitudeLowerBound + amplitudeUpperBound) / 2, amplitude * 2),MAX_AMPLITUDE);
        checkBoundsAndHandleNextStep();

    }

    private void handleNotSure() {
        checkBoundsAndHandleNextStep();
    }


    private void finishTestWithScore(int score) {
        Toast.makeText(TactileSensationActivity.this, "The smallest amplitude you can feel is " + score, Toast.LENGTH_SHORT).show();
        speechRecognizer.cancel();
        Date date = new Date();
        TactileSensationOperations.insertData(this, date, score);
        handlePlayAudio(R.raw.done, new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                finish();
            }
        });
    }

    private void reset() {
        resultLayout.setVisibility(View.GONE);
        vibrateButton.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        ripple.stopRippleAnimation();
        ripple.setVisibility(View.GONE);
        speechRecognizer.cancel();
        resetAudioPlayerAndSpeechRecognizer();
    }

    private void displayVibrationInterface() {
        vibrateButton.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        ripple.setVisibility(View.VISIBLE);
        ripple.startRippleAnimation();
        resultLayout.setVisibility(View.GONE);
        resetAudioPlayerAndSpeechRecognizer();
    }

    private void handlePlayAudio(int audioId, MediaPlayer.OnCompletionListener onCompletionListener) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, audioId);
//        // Set the volume to its maximum level
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        float volume = (float) maxVolume;
//        mediaPlayer.setVolume(volume, volume);

        mediaPlayer.setOnCompletionListener(onCompletionListener);

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void resetAudioPlayerAndSpeechRecognizer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
//        mediaPlayer = null;
        speechRecognizer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        speechRecognizer.destroy();
    }

    private void displayResultInterface() {
        ripple.stopRippleAnimation();
        ripple.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        vibrateButton.setVisibility(View.GONE);
        resetAudioPlayerAndSpeechRecognizer();
        handlePlayAudio(R.raw.did_you_feel_it, new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                startSpeechRecognizer();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // restart the app
                Intent intent = new Intent(this, TactileSensationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}