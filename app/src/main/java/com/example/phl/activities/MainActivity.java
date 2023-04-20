package com.example.phl.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.example.phl.R;
import com.example.phl.services.RemoteControlService;
import com.example.phl.utils.QRCodeGenerator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        ImageView qrCodeImageView = findViewById(R.id.qr_code_image_view);
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
        button3.setOnClickListener(v -> {
            // navigate to progress
            Intent intent = new Intent(this, ProgressActivity.class);
            startActivity(intent);
        });
        Intent intent = new Intent(this, RemoteControlService.class);
        startService(intent);
        String text = "This is a sample QR Code";
        int width = 500;
        int height = 500;
        Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(text, width, height);
        qrCodeImageView.setImageBitmap(qrCodeBitmap);
    }
}