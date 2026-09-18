package com.example.phl.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phl.R

import com.example.phl.activities.ui.theme.PHLTheme
import com.example.phl.utils.UnityAPI

class TestListActivity : ComponentActivity() {

    private enum class SetupState { CHECKING, CONNECT_WIFI, CHECKING_INTERNET, NO_INTERNET, SUCCESS }
    private enum class SetupDot { INACTIVE, ACTIVE, DONE }

    companion object {
        private const val SETUP_PREFS_NAME = "phl_setup"
        private const val SETUP_KEY_WIFI_COMPLETE = "wifi_setup_complete"

        // How long to wait for the system to validate internet access
        // before asking the user to double-check their Wi-Fi password.
        private const val SETUP_INTERNET_TIMEOUT_MS = 20_000L

        // Fallback poll interval in case a network callback is missed.
        private const val SETUP_POLL_INTERVAL_MS = 2_000L

        // Returning users that are already connected see the success screen
        // for a moment and then go straight to the game list.
        private const val SETUP_SUCCESS_AUTO_DISMISS_MS = 1_500L

        private val SETUP_PANELS = intArrayOf(
            R.id.layout_checking,
            R.id.layout_connect_wifi,
            R.id.layout_internet_check,
            R.id.layout_no_internet,
            R.id.layout_success
        )
    }

    private lateinit var setupConnectivityManager: ConnectivityManager
    private lateinit var setupPrefs: SharedPreferences
    private var setupFirstRun = true
    private var setupState = SetupState.CHECKING
    private var setupInternetCheckStartedAt = 0L
    private var setupNetworkCallbackRegistered = false
    private var setupDismissed = false
    private var setupSplashView: View? = null
    private var setupBackCallback: OnBackPressedCallback? = null

    private val setupHandler = Handler(Looper.getMainLooper())

    private val setupPollRunnable = object : Runnable {
        override fun run() {
            evaluateSetupConnectivity()
            setupHandler.postDelayed(this, SETUP_POLL_INTERVAL_MS)
        }
    }

    private val setupNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            evaluateSetupConnectivity()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            evaluateSetupConnectivity()
        }

        override fun onLost(network: Network) {
            evaluateSetupConnectivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewUpperLimbTestsScreen()
        }
        setupConnectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        setupPrefs = getSharedPreferences(SETUP_PREFS_NAME, Context.MODE_PRIVATE)
        setupFirstRun = !setupPrefs.getBoolean(SETUP_KEY_WIFI_COMPLETE, false)
        // Wi-Fi setup splash: skipped only when the device is connected to
        // Wi-Fi with a validated internet connection. Otherwise the splash
        // explains what's wrong (no Wi-Fi, or Wi-Fi without internet).
        if (hasValidatedInternet()) {
            setupDismissed = true
        } else {
            showSetupSplash()
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check in case the user changed something in the system Wi-Fi settings.
        evaluateSetupConnectivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        setupHandler.removeCallbacks(setupPollRunnable)
        if (setupNetworkCallbackRegistered) {
            try {
                setupConnectivityManager.unregisterNetworkCallback(setupNetworkCallback)
            } catch (e: Exception) {
                Log.w("TestListActivity", "Could not unregister network callback", e)
            }
            setupNetworkCallbackRegistered = false
        }
    }

    // ------------------------------------------------------------------
    // Wi-Fi setup splash (shown on top of the game list before it's usable)
    // ------------------------------------------------------------------

    private fun showSetupSplash() {
        // Inflate the splash on top of the Compose content. Both are children
        // of the window's content FrameLayout; the splash is added last, so
        // it draws over the game list.
        val contentFrame = findViewById<ViewGroup>(android.R.id.content)
        val splash = layoutInflater.inflate(R.layout.view_setup_splash, contentFrame, false)
        splash.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        contentFrame.addView(splash)
        setupSplashView = splash

        splash.findViewById<View>(R.id.button_connect_wifi).setOnClickListener { openWifiSettings() }
        splash.findViewById<View>(R.id.button_try_again).setOnClickListener { openWifiSettings() }
        splash.findViewById<View>(R.id.button_continue).setOnClickListener { dismissSetupSplash() }
        splash.findViewById<View>(R.id.button_skip_setup).setOnClickListener {
            // Skip without persisting: the next app launch prompts again.
            dismissSetupSplash(persist = false)
        }

        // While the splash is up, back must not let the user leave the setup
        // flow (same rule as the old MainActivity behavior).
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Intentionally empty: keep the user in the Wi-Fi setup flow.
            }
        }
        setupBackCallback = backCallback
        onBackPressedDispatcher.addCallback(this, backCallback)

        registerSetupNetworkCallback()
        updateSetupState(SetupState.CHECKING)
        setupHandler.postDelayed(setupPollRunnable, SETUP_POLL_INTERVAL_MS)
        evaluateSetupConnectivity()
    }

    private fun registerSetupNetworkCallback() {
        if (setupNetworkCallbackRegistered) return
        try {
            setupConnectivityManager.registerDefaultNetworkCallback(setupNetworkCallback)
            setupNetworkCallbackRegistered = true
        } catch (e: SecurityException) {
            Log.w("TestListActivity", "Could not register network callback, relying on polling", e)
        }
    }

    /**
     * True when a Wi-Fi network is connected AND the system has validated
     * that it actually reaches the internet (not just associated with an
     * access point).
     */
    private fun hasValidatedInternet(): Boolean {
        for (network in setupConnectivityManager.allNetworks) {
            val capabilities =
                setupConnectivityManager.getNetworkCapabilities(network) ?: continue
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                return true
            }
        }
        return false
    }

    private fun evaluateSetupConnectivity() {
        if (setupDismissed) return
        var wifiConnected = false
        var internetValidated = false
        for (network in setupConnectivityManager.allNetworks) {
            val capabilities =
                setupConnectivityManager.getNetworkCapabilities(network) ?: continue
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                wifiConnected = true
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    internetValidated = true
                    break
                }
            }
        }
        var newState = when {
            internetValidated -> SetupState.SUCCESS
            wifiConnected -> SetupState.CHECKING_INTERNET
            else -> SetupState.CONNECT_WIFI
        }
        if (newState == SetupState.CHECKING_INTERNET && setupState == SetupState.CHECKING_INTERNET) {
            // Still waiting: check whether we should ask the user to retry.
            if (System.currentTimeMillis() - setupInternetCheckStartedAt > SETUP_INTERNET_TIMEOUT_MS) {
                newState = SetupState.NO_INTERNET
            }
        }
        updateSetupState(newState)
    }

    private fun updateSetupState(newState: SetupState) {
        if (setupDismissed) return
        if (newState == SetupState.CHECKING_INTERNET && setupState != SetupState.CHECKING_INTERNET) {
            setupInternetCheckStartedAt = System.currentTimeMillis()
        }
        if (newState == SetupState.NO_INTERNET && setupState != SetupState.NO_INTERNET) {
            setupInternetCheckStartedAt = 0L
        }
        setupState = newState

        when (newState) {
            SetupState.CHECKING -> {
                showSetupPanel(R.id.layout_checking)
                setSetupStepLabel(null)
                setSetupDots(SetupDot.ACTIVE, SetupDot.INACTIVE, SetupDot.INACTIVE)
            }
            SetupState.CONNECT_WIFI -> {
                showSetupPanel(R.id.layout_connect_wifi)
                setSetupStepLabel(getString(R.string.setup_step_of_total, 1))
                setSetupDots(SetupDot.ACTIVE, SetupDot.INACTIVE, SetupDot.INACTIVE)
            }
            SetupState.CHECKING_INTERNET -> {
                showSetupPanel(R.id.layout_internet_check)
                setSetupStepLabel(getString(R.string.setup_step_of_total, 2))
                setSetupDots(SetupDot.DONE, SetupDot.ACTIVE, SetupDot.INACTIVE)
            }
            SetupState.NO_INTERNET -> {
                showSetupPanel(R.id.layout_no_internet)
                setSetupStepLabel(getString(R.string.setup_step_of_total, 2))
                setSetupDots(SetupDot.DONE, SetupDot.ACTIVE, SetupDot.INACTIVE)
            }
            SetupState.SUCCESS -> {
                showSetupPanel(R.id.layout_success)
                setSetupStepLabel(null)
                setSetupDots(SetupDot.DONE, SetupDot.DONE, SetupDot.DONE)
                if (!setupFirstRun) {
                    // Returning user that is already connected: keep it quick.
                    setupHandler.postDelayed({ dismissSetupSplash() }, SETUP_SUCCESS_AUTO_DISMISS_MS)
                }
            }
        }
    }

    private fun showSetupPanel(panelId: Int) {
        for (id in SETUP_PANELS) {
            findViewById<View>(id).visibility = if (id == panelId) View.VISIBLE else View.GONE
        }
    }

    private fun setSetupStepLabel(label: String?) {
        findViewById<TextView>(R.id.setup_step_label).text = label ?: ""
    }

    private fun setSetupDots(step1: SetupDot, step2: SetupDot, step3: SetupDot) {
        setSetupDot(R.id.dot_step_1, step1)
        setSetupDot(R.id.dot_step_2, step2)
        setSetupDot(R.id.dot_step_3, step3)
    }

    private fun setSetupDot(viewId: Int, dot: SetupDot) {
        val drawable = when (dot) {
            SetupDot.INACTIVE -> R.drawable.dot_setup_inactive
            SetupDot.ACTIVE -> R.drawable.dot_setup_active
            SetupDot.DONE -> R.drawable.dot_setup_done
        }
        findViewById<View>(viewId).setBackgroundResource(drawable)
    }

    private fun openWifiSettings() {
        try {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        } catch (e: Exception) {
            // Fall back to the general settings screen.
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (e2: Exception) {
                Log.e("TestListActivity", "Could not open Wi-Fi settings", e2)
            }
        }
    }

    /**
     * Closes the splash. When [persist] is true (setup completed or the user
     * tapped Continue), the setup-complete flag is saved so the next app
     * launch goes straight to the game list. When false (the user tapped
     * "Skip for now"), the flag is left untouched, so the next launch prompts
     * again.
     */
    private fun dismissSetupSplash(persist: Boolean = true) {
        if (setupDismissed) return
        setupDismissed = true
        if (persist) {
            setupPrefs.edit().putBoolean(SETUP_KEY_WIFI_COMPLETE, true).apply()
        }
        setupHandler.removeCallbacks(setupPollRunnable)
        setupBackCallback?.isEnabled = false
        val splash = setupSplashView ?: return
        splash.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction { splash.visibility = View.GONE }
            .start()
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current // Get the local context for the Intent

    Column(modifier = modifier) {
        Text(text = "Hello, please choose a game to load!")
        Button(onClick = {
            val intent = Intent(context, MainUnityActivity::class.java)
            intent.putExtra("loadScene", "GAME_1")
            context.startActivity(intent)
        }) {
            Text(text = "Load Game 1")
        }
        Button(onClick = {
            val intent = Intent(context, MainUnityActivity::class.java)
            intent.putExtra("loadScene", "GAME_2") // Corrected to GAME_2
            context.startActivity(intent)
        }) {
            Text(text = "Load Game 2")
        }
        Button(onClick = {
            val intent = Intent(context, MainUnityActivity::class.java)
            intent.putExtra("loadScene", "GAME_4")
            context.startActivity(intent)
        }) {
            Text(text = "Load Game 4")
        }
    }
}

@Composable
fun UpperLimbTestsScreen(
    tests: List<Test>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF20A086)) // Set background color
            .padding(16.dp)
    ) {
        Text(
            text = "Upper Limb Tests",
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            content = {
                items(tests) { test ->
                    Column(
                        modifier = Modifier
                            .padding(12.dp) // Add padding between items
                    ) {
                        TestCard(test)
                    }
                }
            },
        )

        Spacer(modifier = Modifier.weight(1f))

    }
}

@Composable
fun TestCard(test: Test) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp),
        onClick = {
            Log.d("UnityAPI", "Tapped ${test.scene.name}")
            if (test.scene != UnityAPI.Scene.None) {
                UnityAPI.launchUnityActivity(context, test.scene)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
        ) {
            Image(
                painter = painterResource(id = test.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(125.dp)
                    .width(150.dp)
            )
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = test.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                )
                Text(
                    text = test.description,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

data class Test(val title: String, val description: String, val imageRes: Int, val scene:UnityAPI.Scene)

@Preview(name = "10-inch Tablet Landscape", widthDp = 1200, heightDp = 750)
@Composable
fun PreviewUpperLimbTestsScreen() {
    val tests = listOf(
        Test("Shoulder Extension & Flexion", "Evaluate the range of motion of your shoulder", R.drawable.shoulder_extension_icon, UnityAPI.Scene.GAME_1),
        Test("Shoulder Rotation", "Evaluate the internal and external rotation ability of your shoulder", R.drawable.shoulder_rotation_icon, UnityAPI.Scene.GAME_2),
        Test("Elbow Extension & Flexion", "Evaluate the range of motion of your elbow", R.drawable.elbow_extension_icon, UnityAPI.Scene.None),
        Test("Wrist Up & Down", "Evaluate the range of motion of your wrist", R.drawable.wrist_up_icon_compressed, UnityAPI.Scene.GAME_4),
        Test("Finger-to-Nose", "Evaluate the overall coordination of your upper-limb", R.drawable.finger_to_nose_icon_compressed, UnityAPI.Scene.None),
        Test("Finger Extension & Flexion", "Evaluate the maximum range of motion of your fingers", R.drawable.finger_extension_icon, UnityAPI.Scene.GAME_3_FingerFlexion),
        Test("Finger Tapping - Coordination", "Evaluate the overall dexterity and coordination of your fingers", R.drawable.finger_tapping_1_icon, UnityAPI.Scene.None),
        Test("Finger Tapping - Speed", "Evaluate the dexterity and movements of your fingers", R.drawable.finger_tapping_2_icon, UnityAPI.Scene.GAME_5),
    )


    UpperLimbTestsScreen(tests)
}
