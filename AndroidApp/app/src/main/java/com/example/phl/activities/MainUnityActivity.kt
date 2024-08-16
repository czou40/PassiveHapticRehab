package com.example.phl.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.ComposeView
import com.example.phl.activities.unity.ShoulderExtensionFlexionOverlay
import com.example.phl.services.MediaPipeService
import com.example.phl.utils.PermissionManager
import com.example.phl.utils.UnityAPI
import com.unity3d.player.IUnityPlayerSupport
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerGameActivity
import com.example.phl.utils.UnityAPI.Scene
import com.example.phl.views.HandLandmarkerOverlayView
import com.example.phl.views.HolisticOverlayView
import com.example.phl.views.PoseLandmarkerOverlayView

class MainUnityActivity : UnityPlayerGameActivity() {


    private var serviceBinder: MediaPipeService.LocalBinder? = null
    private var isBound: Boolean = false
    private var previewView: PreviewView? = null
    private var overlayView: HolisticOverlayView? = null

    private val cameraImageHeight = 480
    private val cameraImageWidth = 640

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MediaPipeService.LocalBinder
            this@MainUnityActivity.serviceBinder = binder
            isBound = true
            binder.setActivityContext(this@MainUnityActivity)
            binder.setStartStreamingWhenReady(true, previewView, cameraImageWidth, cameraImageHeight, overlayView)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            this@MainUnityActivity.serviceBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Setup activity layout
//        addControlsToUnityFrame()
        addCameraPreviewToUnityFrame()
        addComposeViewToUnityFrame()

    }

    override fun onResume() {
        super.onResume()
        val intent = intent
        handleIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        val hasPermission = PermissionManager.checkAndRequestCameraPermission(this)
        if (!hasPermission) {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            finish()
        }
        Intent(this, MediaPipeService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            if (isBound) {
                unbindService(connection)
                isBound = false
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        setIntent(intent)
    }

    fun doQuit() {
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        finish()
    }

    fun handleIntent(intent: Intent?) {
        if (intent == null || intent.extras == null) return

        if (intent.extras!!.containsKey("doQuit")) {
            doQuit()
        }

        if (intent.extras!!.containsKey("loadScene")) {
            val scene = Scene.valueOf(intent.extras!!.getString("loadScene")!!)
            UnityAPI.loadStartScene(scene)
        }
    }

    override fun onUnityPlayerQuitted() {
        super.onUnityPlayerQuitted()
        Log.i("MainUnityActivity", "UnityPlayer quitted")
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        finish()
    }

    override fun onUnityPlayerUnloaded() {
        super.onUnityPlayerUnloaded()
        Log.i("MainUnityActivity", "UnityPlayer unloaded")
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        finish()
    }

    @Suppress("unused")
    fun receiveCommand(command: String) {
        Log.d("MainUnityActivity", "Received command: $command")
        UnityAPI.processCommand(applicationContext, command, object : UnityAPI.CommandCallback {
            override fun onSuccess(message: String, operation: UnityAPI.CallBackOperation) {
                Log.d("UnityAPI", "Command processed successfully: $message")
                when (operation) {
                    UnityAPI.CallBackOperation.LOAD_PROGRESS_VISUALIZATION -> {
                        val intent = Intent(
                            this@MainUnityActivity,
                            ProgressVisualizationActivity::class.java
                        )
                        startActivity(intent)
                        doQuit()
                    }

                    UnityAPI.CallBackOperation.QUIT -> {
                        doQuit()
                    }
                    UnityAPI.CallBackOperation.NONE -> {}
                }
            }

            override fun onFailure(error: String, operation: UnityAPI.CallBackOperation) {
                Log.e("UnityAPI", "Command processing failed: $error")
                when (operation) {
                    UnityAPI.CallBackOperation.LOAD_PROGRESS_VISUALIZATION -> {
                        val intent = Intent(
                            this@MainUnityActivity,
                            ProgressVisualizationActivity::class.java
                        )
                        startActivity(intent)
                        doQuit()
                    }

                    UnityAPI.CallBackOperation.QUIT -> {
                        doQuit()
                    }
                    UnityAPI.CallBackOperation.NONE -> {}
                }
            }
        })
        runOnUiThread {
            Toast.makeText(this, "Received command: $command", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun addCameraPreviewToUnityFrame() {
        val unityPlayer = (UnityPlayer.currentActivity as IUnityPlayerSupport).unityPlayerConnection
        val layout = unityPlayer.frameLayout
        previewView = PreviewView(unityPlayer.context).apply {
            layoutParams = FrameLayout.LayoutParams(cameraImageWidth, cameraImageHeight, Gravity.BOTTOM or Gravity.RIGHT)
            scaleType = PreviewView.ScaleType.FIT_START
        }

        overlayView = HolisticOverlayView(unityPlayer.context, null).apply {
            layoutParams = FrameLayout.LayoutParams(cameraImageWidth, cameraImageHeight, Gravity.BOTTOM or Gravity.RIGHT)
        }

        layout.addView(previewView)
        layout.addView(overlayView)
    }

    private fun addComposeViewToUnityFrame() {
        val unityPlayer = (UnityPlayer.currentActivity as IUnityPlayerSupport).unityPlayerConnection
        val layout = unityPlayer.frameLayout

        val composeView = ComposeView(unityPlayer.context).apply {
            setContent {
                ShoulderExtensionFlexionOverlay()
            }
        }
        layout.addView(composeView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
    }


//    private fun addControlsToUnityFrame() {
//        val unityPlayer = (UnityPlayer.currentActivity as IUnityPlayerSupport).unityPlayerConnection
//        val layout = unityPlayer.frameLayout
//        val startStreamingButton = Button(this)
//        startStreamingButton.text = "Start Camera"
//        startStreamingButton.x = 10f
//        startStreamingButton.y = 500f
//        startStreamingButton.setOnClickListener { serviceBinder?.startStreaming() }
//        layout.addView(startStreamingButton, 300, 200)
//
//        val sendMsgButton = Button(this)
//        sendMsgButton.text = "Send Msg"
//        sendMsgButton.x = 320f
//        sendMsgButton.y = 500f
//        sendMsgButton.setOnClickListener {
//            UnityPlayer.UnitySendMessage(
//                "Control", "ReceiveCommand", "load Game1"
//            )
//        }
//        layout.addView(sendMsgButton, 300, 200)
//
//        val unloadButton = Button(this)
//        unloadButton.text = "Unload UnityPlayer"
//        unloadButton.x = 630f
//        unloadButton.y = 500f
//
//        unloadButton.setOnClickListener { unityPlayer.unload() }
//        layout.addView(unloadButton, 350, 200)
//
//        val finishButton = Button(this)
//        finishButton.text = "Activity Finish"
//        finishButton.x = 630f
//        finishButton.y = 800f
//
//        finishButton.setOnClickListener { finish() }
//        layout.addView(finishButton, 300, 200)
//    }
}
