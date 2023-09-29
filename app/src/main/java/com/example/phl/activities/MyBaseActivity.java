package com.example.phl.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phl.services.RemoteControlService;

import java.util.HashMap;
import java.util.Map;

public class MyBaseActivity  extends AppCompatActivity {

    private boolean isReceiverRegistered = false;

    private boolean displayOnBackPressedWarning = true;

    public static final String TAG = "MyBaseActivity";

    private Map<String, RemoteControlService.CommandHandler> commandMap = new HashMap<>();

    BroadcastReceiver remoteControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra(RemoteControlService.COMMAND);
            if (command != null) {
                command = command.trim();
                RemoteControlService.CommandHandler commandHandler = commandMap.get(command);
                if (commandHandler != null) {
                    abortBroadcast();
                    commandHandler.handle();
                }
            }
        }
    };

    public void registerCommand(String command, RemoteControlService.CommandHandler commandHandler) {
        command = command.trim();
        commandMap.put(command, commandHandler);
//        RemoteControlService.notifyCommand(this, command);
    }

    private void registerAllCommands() {
        for (String command : commandMap.keySet()) {
            RemoteControlService.notifyCommand(this, command);
        }
    }
    private void unregisterAllCommands() {
        for (String command : commandMap.keySet()) {
            RemoteControlService.notifyCommandRemoval(this, command);
        }
    }

    private void registerReceiverIfNotRegistered() {
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(RemoteControlService.ACTION);
            this.registerReceiver(remoteControlReceiver, filter);
            isReceiverRegistered = true;
        }
        registerAllCommands();
    }

    private void unregisterReceiverIfRegistered() {
        unregisterAllCommands();
        if (isReceiverRegistered) {
            this.unregisterReceiver(remoteControlReceiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        registerCommand("Exit Activity", new RemoteControlService.CommandHandler() {
            @Override
            public void handle() {
                finish();
            }
        });

        registerCommand("Go Back", new RemoteControlService.CommandHandler() {
            @Override
            public void handle() {
                displayOnBackPressedWarning = false;
                onBackPressed();
                displayOnBackPressedWarning = true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!displayOnBackPressedWarning) {
            super.onBackPressed();
            return;
        }
        // display a warning dialog, then navigate to main activity
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to go back?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // navigate to main activity
                MyBaseActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        registerReceiverIfNotRegistered();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiverIfRegistered();
    }



    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

}
