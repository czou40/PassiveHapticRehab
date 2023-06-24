package com.example.phl.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phl.services.RemoteControlService;

import java.util.HashMap;
import java.util.Map;

public class MyBaseActivity  extends AppCompatActivity {

    private boolean isReceiverRegistered = false;

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
