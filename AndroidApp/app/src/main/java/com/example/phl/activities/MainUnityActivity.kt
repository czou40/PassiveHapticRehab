package com.example.phl.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.phl.services.MediaPipeService
import com.example.phl.utils.PermissionManager
import com.unity3d.player.IUnityPlayerSupport
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerActivity


class MainUnityActivity : UnityPlayerActivity() {
    enum class Scene {
        GAME_1,
        GAME_2
    }

    private var serviceBinder: MediaPipeService.LocalBinder? = null
    private var isBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MediaPipeService.LocalBinder
            this@MainUnityActivity.serviceBinder = binder
            isBound = true
            binder.setStartStreamingWhenReady(true)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            this@MainUnityActivity.serviceBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup activity layout
//        addControlsToUnityFrame()
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
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        setIntent(intent)
    }

    fun handleIntent(intent: Intent?) {
        if (intent == null || intent.extras == null) return

        if (intent.extras!!.containsKey("doQuit")) {
            if (mUnityPlayer != null) {
                if (isBound) {
                    unbindService(connection)
                    isBound = false
                }
                finish()
            }
        }

        if (intent.extras!!.containsKey("loadScene")) {
            val scene = Scene.valueOf(intent.extras!!.getString("loadScene")!!)
            when (scene) {
                Scene.GAME_1 -> {
                    UnityPlayer.UnitySendMessage(
                        "Control", "ReceiveCommand", "load Game1"
                    )
                }
                Scene.GAME_2 -> {
                    UnityPlayer.UnitySendMessage(
                        "Control", "ReceiveCommand", "load Game2"
                    )
                }
            }
        }
    }

    override fun onUnityPlayerQuitted() {
        super.onUnityPlayerQuitted()
        Toast.makeText(this, "UnityPlayer quitted", Toast.LENGTH_SHORT).show()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        finish()
    }

    override fun onUnityPlayerUnloaded() {
        super.onUnityPlayerUnloaded()
        Toast.makeText(this, "UnityPlayer unloaded", Toast.LENGTH_SHORT).show()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        finish()
    }

    @Suppress("unused")
    fun receiveCommand(command: String) {
        Log.d("MainUnityActivity", "Received command: $command")
        Toast.makeText(this, "Received command: $command", Toast.LENGTH_SHORT).show()
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
