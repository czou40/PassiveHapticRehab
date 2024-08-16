package com.example.phl.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.phl.R
import com.example.phl.services.RemoteControlService
import com.example.phl.utils.QRCodeGenerator

class MainActivity : MyBaseActivity() {
    private var noInternetConnection: TextView? = null
    private var connecting: TextView? = null
    private var qrCodeImageView: ImageView? = null
    private var qrCodeTextView: TextView? = null

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == RemoteControlService.SOCKET_CONNECTED_ACTION) {
                val id = intent.getStringExtra("id")
                if (!id.isNullOrEmpty()) {
                    displayQRCode(id)
                }
            } else if (action == RemoteControlService.SOCKET_ERROR_ACTION) {
                displayNoInternetConnection()
            }
        }
    }

    private fun displayQRCode(id: String) {
        val width = 500
        val height = 500
        val qrCodeBitmap = QRCodeGenerator.generateQRCode(
            RemoteControlService.WEB_SERVER + "?id=" + id,
            width,
            height
        )
        qrCodeImageView!!.setImageBitmap(qrCodeBitmap)
        noInternetConnection!!.visibility = View.GONE
        connecting!!.visibility = View.GONE
        qrCodeImageView!!.visibility = View.VISIBLE
        qrCodeTextView!!.visibility = View.VISIBLE
    }

    private fun displayConnecting() {
        noInternetConnection!!.visibility = View.GONE
        connecting!!.visibility = View.VISIBLE
        qrCodeImageView!!.visibility = View.GONE
        qrCodeTextView!!.visibility = View.GONE
    }

    private fun displayNoInternetConnection() {
        noInternetConnection!!.visibility = View.VISIBLE
        connecting!!.visibility = View.GONE
        qrCodeImageView!!.visibility = View.GONE
        qrCodeTextView!!.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)
        val button4 = findViewById<Button>(R.id.button4)
        val button5 = findViewById<Button>(R.id.button5)
        val button6 = findViewById<Button>(R.id.button6)
        val button7 = findViewById<Button>(R.id.button7)
        noInternetConnection = findViewById(R.id.no_internet)
        connecting = findViewById(R.id.connecting)
        qrCodeImageView = findViewById(R.id.qr_code_image_view)
        qrCodeTextView = findViewById(R.id.qr_code_text_view)
        button1.setOnClickListener { v: View? ->
            // navigate to spasticity diagnosis
            val intent = Intent(this, SpasticityDiagnosisActivity::class.java)
            startActivity(intent)
        }
        button2.setOnClickListener { v: View? ->
            // navigate to tactile sensation
            val intent = Intent(this, TactileSensationActivity::class.java)
            startActivity(intent)
        }
        button3.setOnClickListener { v: View? ->
            // navigate to progress
            val intent = Intent(this, ProgressVisualizationActivity::class.java)
            startActivity(intent)
        }
        button4.setOnClickListener { v: View? ->
            val intent = Intent(this, BallTestActivity::class.java)
            startActivity(intent)
        }
        button5.setOnClickListener { v: View? ->
            val intent = Intent(this, TiltTestActivity::class.java)
            startActivity(intent)
        }
        button6.setOnClickListener { v: View? ->
            if (getVolume() < 0.5) {
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Volume Warning")
                    .setMessage("To make the Modified Ashworth Scale test work properly, please increase the volume of your device to at least 50%.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .create()

                alertDialog.show()
            } else {
                val intent = Intent(this, MasActivity::class.java)
                startActivity(intent)
            }
        }
        button7.setOnClickListener { v: View? ->
            val intent = Intent(this, TestListActivity::class.java)
            startActivity(intent)
        }
        val settingsIcon = findViewById<ImageView>(R.id.settings_icon)
        settingsIcon.setOnClickListener { v: View? ->
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
        if (RemoteControlService.getSocketId() != null) {
            displayQRCode(RemoteControlService.getSocketId())
        } else {
            displayConnecting()
        }
        displayConnecting()
        val intent = Intent(this, RemoteControlService::class.java)
        startService(intent)
        Log.d("MainActivity", "Started service")
        // The following code is for testing Python scripts
//        if (!Python.isStarted()) {
//            Python.start(AndroidPlatform(this))
//        }
//        val py = Python.getInstance()
//        val pyModule = py.getModule("test_module")
//        val pyClass = pyModule.callAttr("TestModule")
//        val result = pyClass.callAttr("get_random_number")
//        val randomNumber = result.toInt()
//        Log.d("MainActivity", "Random number: $randomNumber")
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(RemoteControlService.SOCKET_CONNECTED_ACTION)
        intentFilter.addAction(RemoteControlService.SOCKET_ERROR_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            this.registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // do nothing
    }

    private fun getVolume(): Double {
        // Get an instance of AudioManager
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Check the current volume level
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return currentVolume.toDouble() / maxVolume.toDouble()
    }
}