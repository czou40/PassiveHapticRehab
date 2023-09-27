package com.example.phl.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phl.R;
import com.example.phl.services.RemoteControlService;
import com.example.phl.utils.QRCodeGenerator;
import com.example.phl.views.MyButton;

import com.chaquo.python.Python;
import com.chaquo.python.PyObject;
import com.chaquo.python.android.AndroidPlatform;


public class MainActivity extends MyBaseActivity {

    TextView noInternetConnection;
    TextView connecting;
    ImageView qrCodeImageView;

    TextView qrCodeTextView;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(RemoteControlService.SOCKET_CONNECTED_ACTION)) {
                String id = intent.getStringExtra("id");
                if (id != null && !id.isEmpty()) {
                    displayQRCode(id);
                }
            } else if (action.equals(RemoteControlService.SOCKET_ERROR_ACTION)) {
                displayNoInternetConnection();
            }
        }
    };

    private void displayQRCode(String id) {
        int width = 500;
        int height = 500;
        Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(RemoteControlService.WEB_SERVER + "?id=" + id, width, height);
        qrCodeImageView.setImageBitmap(qrCodeBitmap);
        noInternetConnection.setVisibility(View.GONE);
        connecting.setVisibility(View.GONE);
        qrCodeImageView.setVisibility(View.VISIBLE);
        qrCodeTextView.setVisibility(View.VISIBLE);
    }

    private void displayConnecting() {
        noInternetConnection.setVisibility(View.GONE);
        connecting.setVisibility(View.VISIBLE);
        qrCodeImageView.setVisibility(View.GONE);
        qrCodeTextView.setVisibility(View.GONE);
    }

    private void displayNoInternetConnection() {
        noInternetConnection.setVisibility(View.VISIBLE);
        connecting.setVisibility(View.GONE);
        qrCodeImageView.setVisibility(View.GONE);
        qrCodeTextView.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        Button button5= findViewById(R.id.button5);
        Button button6 = findViewById(R.id.button6);
        noInternetConnection = findViewById(R.id.no_internet);
        connecting = findViewById(R.id.connecting);
        qrCodeImageView = findViewById(R.id.qr_code_image_view);
        qrCodeTextView = findViewById(R.id.qr_code_text_view);
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
        button4.setOnClickListener(v -> {
            // navigate to remote control
            Intent intent = new Intent(this, BallTestActivity.class);
            startActivity(intent);
        });
        button5.setOnClickListener(v -> {
            // navigate to remote control
            Intent intent = new Intent(this, TiltTestActivity.class);
            startActivity(intent);
        });
        button6.setOnClickListener(v -> {
            // navigate to remote control
            Intent intent = new Intent(this, MasActivity.class);
            startActivity(intent);
        });
        if (RemoteControlService.getSocketId() != null) {
            displayQRCode(RemoteControlService.getSocketId());
        } else {
            displayConnecting();
        }
        displayConnecting();
        Intent intent = new Intent(this, RemoteControlService.class);
        startService(intent);
        Log.d("MainActivity", "Started service");

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();

        PyObject pyModule = py.getModule("test_module");
        PyObject pyClass = pyModule.callAttr("TestModule");
        PyObject result = pyClass.callAttr("get_random_number");
        int randomNumber = result.toInt();
        Log.d("MainActivity", "Random number: " + randomNumber);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RemoteControlService.SOCKET_CONNECTED_ACTION);
        intentFilter.addAction(RemoteControlService.SOCKET_ERROR_ACTION);
        this.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }
}