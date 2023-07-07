package com.example.phl.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.phl.R
import com.example.phl.data.AppDatabase
import com.example.phl.data.sensation.TactileSensation
import com.example.phl.utils.PermissionManager
import com.skyfishjy.library.RippleBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import java.util.UUID

class TactileSensationActivity : MyBaseActivity() {
    private lateinit var speechRecognizer: SpeechRecognizer
    private var mediaPlayer: MediaPlayer? = null
    private val MAX_AMPLITUDE = 255
    private val MIN_AMPLITUDE = 1
    private val MIN_INTERVAL_SIZE = 4
    private var amplitude = MIN_AMPLITUDE
    private var amplitudeLowerBound = MIN_AMPLITUDE
    private var amplitudeUpperBound = 999999999
    private lateinit var vibrateButton: Button
    private lateinit var resultLayout: LinearLayout
    private lateinit var feltVibrationButton: Button
    private lateinit var didNotFeelVibrationButton: Button
    private lateinit var notSureButton: Button
    private lateinit var ripple: RippleBackground
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tactile_sensation)
        vibrateButton = findViewById(R.id.button)
        resultLayout = findViewById(R.id.result_layout)
        feltVibrationButton = findViewById(R.id.felt_vibration_button)
        didNotFeelVibrationButton = findViewById(R.id.did_not_feel_vibration_button)
        notSureButton = findViewById(R.id.i_am_not_sure_button)
        textView = findViewById(R.id.text_view)
        ripple = findViewById(R.id.ripple)
        textView.visibility = View.VISIBLE
        ripple.visibility = View.GONE
        resultLayout.visibility = View.GONE
        vibrateButton.visibility = View.VISIBLE
        vibrateButton.setOnClickListener { handleStartVibration() }
        feltVibrationButton.setOnClickListener { handleFeltVibration() }
        didNotFeelVibrationButton.setOnClickListener { handleDidNotFeelVibration() }
        notSureButton.setOnClickListener { handleNotSure() }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(i: Int) {
                // display error message
                Toast.makeText(this@TactileSensationActivity, "Error: $i", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResults(results: Bundle) {
                val matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val match = matches[0].lowercase(Locale.getDefault())
                    Log.d("Speech", "onResults: $match")
                    if (match == "yes") {
                        handleFeltVibration()
                    } else if (match == "no") {
                        handleDidNotFeelVibration()
                    } else if (match.contains("not sure")) {
                        handleNotSure()
                    } else {
                        Toast.makeText(
                            this@TactileSensationActivity,
                            "I didn't understand that",
                            Toast.LENGTH_SHORT
                        ).show()
                        handlePlayAudio(R.raw.i_dont_understand_it) { startSpeechRecognizer() }
                    }
                }
            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
        })
        handlePlayAudio(R.raw.please_close_your_eyes, null)
    }

    override fun onStart() {
        super.onStart()
        PermissionManager.checkAndRequestRecordAudioPermission(this)
    }

    private fun startSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say yes, no, or not sure")
        speechRecognizer.startListening(intent)
    }

    private fun checkBoundsAndHandleNextStep() {
        if (amplitudeUpperBound - amplitudeLowerBound < MIN_INTERVAL_SIZE) {
            finishTestWithScore(amplitudeUpperBound)
        } else {
            handleStartVibration()
        }
    }

    private fun handleStartVibration() {
        displayVibrationInterface()
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        Log.d("Vibration", "handleStartVibration: $amplitude")
        vibrator.vibrate(VibrationEffect.createOneShot(3000, amplitude))
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                vibrator.cancel()
                displayResultInterface()
            }
        }.start()
    }

    private fun handleFeltVibration() {
        amplitudeUpperBound = amplitude
        amplitude = (amplitudeLowerBound + amplitudeUpperBound) / 2
        checkBoundsAndHandleNextStep()
    }

    private fun handleDidNotFeelVibration() {
        amplitudeLowerBound = amplitude
        amplitude = ((amplitudeLowerBound + amplitudeUpperBound) / 2).coerceAtMost(amplitude * 2)
            .coerceAtMost(MAX_AMPLITUDE)
        checkBoundsAndHandleNextStep()
    }

    private fun handleNotSure() {
        checkBoundsAndHandleNextStep()
    }

    private fun finishTestWithScore(score: Int) {
        Toast.makeText(
            this@TactileSensationActivity,
            "The smallest amplitude you can feel is $score",
            Toast.LENGTH_SHORT
        ).show()
        speechRecognizer.cancel()
        val date = Date()
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@TactileSensationActivity)
            db.tactileSensationDao().insert(TactileSensation(UUID.randomUUID().toString(), score.toDouble()))
        }
        handlePlayAudio(R.raw.done) { finish() }
    }

    private fun reset() {
        resultLayout.visibility = View.GONE
        vibrateButton.visibility = View.VISIBLE
        textView.visibility = View.VISIBLE
        ripple.stopRippleAnimation()
        ripple.visibility = View.GONE
        speechRecognizer.cancel()
        resetAudioPlayerAndSpeechRecognizer()
    }

    private fun displayVibrationInterface() {
        vibrateButton.visibility = View.GONE
        textView.visibility = View.GONE
        ripple.visibility = View.VISIBLE
        ripple.startRippleAnimation()
        resultLayout.visibility = View.GONE
        resetAudioPlayerAndSpeechRecognizer()
    }

    private fun handlePlayAudio(
        audioId: Int,
        onCompletionListener: MediaPlayer.OnCompletionListener?
    ) {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
        mediaPlayer = MediaPlayer.create(this, audioId)
        //        // Set the volume to its maximum level
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        float volume = (float) maxVolume;
//        mediaPlayer.setVolume(volume, volume);
        mediaPlayer!!.setOnCompletionListener(onCompletionListener)
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
        }
    }

    private fun resetAudioPlayerAndSpeechRecognizer() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
        speechRecognizer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
        speechRecognizer.destroy()
    }

    private fun displayResultInterface() {
        ripple.stopRippleAnimation()
        ripple.visibility = View.GONE
        resultLayout.visibility = View.VISIBLE
        textView.visibility = View.GONE
        vibrateButton.visibility = View.GONE
        resetAudioPlayerAndSpeechRecognizer()
        handlePlayAudio(R.raw.did_you_feel_it) { startSpeechRecognizer() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // restart the app
                val intent = Intent(this, TactileSensationActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onBackPressed() {
        // do nothing
    }
}