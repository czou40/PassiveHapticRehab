package com.example.phl.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.example.phl.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Find the reset preference using its key
            val resetPreference = findPreference<Preference>("reset_to_default")
            resetPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                resetToDefaultValues()
                true
            }

        }

        private fun resetToDefaultValues() {
            // Reset each preference to its default value
            val accidentalTouchesPreference = findPreference<SwitchPreferenceCompat>("prevent_accidental_touches")
            accidentalTouchesPreference?.isChecked = true

            val holdTimePreference = findPreference<SeekBarPreference>("button_hold_time")
            holdTimePreference?.value = 2000

            // Display a toast or a snackbar for feedback
            Toast.makeText(activity, "Settings reset to default values", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                restartApp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}