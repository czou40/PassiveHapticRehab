package com.example.phl.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.phl.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        // button 1: navigate to spacitity diagnosis
        // button 2: navigate to tactile sensation
        button1.setOnClickListener(v -> {
            // navigate to spasticity diagnosis
            Intent intent = new Intent(this, SpasticityDiagnosisActivity.class);
            startActivity(intent);
        });
        button2.setOnClickListener(v -> {
            // navigate to tactile sensation
            Intent intent = new Intent(this, TactileSensationActivity.class);
            startActivity(intent);
        });
    }
}