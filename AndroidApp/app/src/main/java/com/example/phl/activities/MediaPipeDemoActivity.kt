package com.example.phl.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.example.phl.R
import com.example.phl.services.MediaPipeService
import com.example.phl.utils.PermissionManager

class MediaPipeDemoActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private var serviceBinder: MediaPipeService.LocalBinder? = null
    private var isBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MediaPipeService.LocalBinder
            this@MediaPipeDemoActivity.serviceBinder = binder
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            this@MediaPipeDemoActivity.serviceBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediapipe_demo)
        previewView = findViewById(R.id.previewView)
        val startStreamingButton = findViewById<Button>(R.id.startStreamingButton)
        val stopStreamingButton = findViewById<Button>(R.id.stopStreamingButton)
        val showUnityButton = findViewById<Button>(R.id.showUnityButton)

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

        showUnityButton.setOnClickListener {
            val intent = Intent(this, MainUnityActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val hasPermission = PermissionManager.checkAndRequestCameraPermission(this)
        if (!hasPermission) {
            return
        }
        Intent(this, MediaPipeService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
            Log.d("MediaPipeDemoActivity", "Service unbound")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // restart the app
                val intent = Intent(this, MediaPipeDemoActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}