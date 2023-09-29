package com.example.phl.activities

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.phl.R

class MasActivity : MyBaseActivity() {

    companion object {
        const val TAG = "MasActivity"
        const val LONG_PRESS_THRESHOLD = 2000

        interface VolumeKeyListener {
            fun onVolumeKeyShortPress();
            fun onVolumeKeyLongPress();
        }
    }

    private lateinit var navController: NavController

    private var volumeKeyDownTime: Long = 0
    private var numberOfVolumeKeyPressed: Int = 0

    private lateinit var volumeKeyListeners: ArrayList<VolumeKeyListener>

    fun addVolumeKeyListener(listener: VolumeKeyListener) {
        volumeKeyListeners.add(listener)
    }

    fun removeVolumeKeyListener(listener: VolumeKeyListener) {
        volumeKeyListeners.remove(listener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mas)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onStart() {
        super.onStart()
        volumeKeyListeners = ArrayList()
    }

    // If you have a toolbar or other UI elements that interact with navigation,
    // override the onSupportNavigateUp function:
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event?.repeatCount!! > 0) {
                    return true
                }
                numberOfVolumeKeyPressed++
                Log.d("Volume Key", "Number of Volume Key Pressed: $numberOfVolumeKeyPressed")
                volumeKeyDownTime = if (volumeKeyDownTime == 0L) {
                    System.currentTimeMillis()
                } else {
                    volumeKeyDownTime
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                numberOfVolumeKeyPressed--
                Log.d("Volume Key", "Number of Volume Key Pressed: $numberOfVolumeKeyPressed")
                if (numberOfVolumeKeyPressed > 0) {
                    return true
                }
                val volumeKeyUpTime = System.currentTimeMillis()
                val pressDuration = volumeKeyUpTime - volumeKeyDownTime
                Log.d("Volume Key", "Start Time: $volumeKeyDownTime")
                Log.d("Volume Key", "End Time: $volumeKeyUpTime")
                Log.d("Volume Key", "Press Duration: $pressDuration")
                volumeKeyDownTime = 0
                if (pressDuration > LONG_PRESS_THRESHOLD) {
                    handleLongPress()
                } else {
                    handleShortPress()
                }
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun handleShortPress() {
        // Handle short press of volume button
        Log.d(TAG, "Short Press")
        for (listener in volumeKeyListeners) {
            listener.onVolumeKeyShortPress()
        }
    }

    private fun handleLongPress() {
        // Handle long press of volume button
        Log.d(TAG, "Long Press")
        for (listener in volumeKeyListeners) {
            listener.onVolumeKeyLongPress()
        }
    }

}