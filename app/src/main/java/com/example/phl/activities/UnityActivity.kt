package com.example.phl.activities

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.phl.R
import com.example.phl.services.MediaPipeService
import java.io.File
import java.nio.ByteBuffer

class UnityActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private var serviceBinder: MediaPipeService.LocalBinder? = null
    private var isBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MediaPipeService.LocalBinder
            this@UnityActivity.serviceBinder = binder
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            this@UnityActivity.serviceBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unity)
        previewView = findViewById(R.id.previewView)
        val startStreamingButton = findViewById<Button>(R.id.startStreamingButton)
        val stopStreamingButton = findViewById<Button>(R.id.stopStreamingButton)

        startStreamingButton.setOnClickListener {
            if (isBound) {
                serviceBinder?.startStreaming(previewView)
            }
        }

        stopStreamingButton.setOnClickListener {
            if (isBound) {
                serviceBinder?.stopStreaming()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MediaPipeService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}