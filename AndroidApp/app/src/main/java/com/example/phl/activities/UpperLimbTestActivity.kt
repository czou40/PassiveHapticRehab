package com.example.phl.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.phl.R

class UpperLimbTestActivity : MyBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upper_limb_home)

        val button1 = findViewById<Button>(R.id.doItNowButton)

        button1.setOnClickListener { v: View? ->
            // TODO navigate to upper limb test
        }

    }
}