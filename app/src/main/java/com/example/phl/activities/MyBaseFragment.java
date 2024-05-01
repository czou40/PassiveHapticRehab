package com.example.phl.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.example.phl.services.RemoteControlService;

import java.util.HashMap;
import java.util.Map;

public class MyBaseFragment extends Fragment {
    private boolean isReceiverRegistered = false;

    public static final String TAG = "MyBaseFragment";

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
            RemoteControlService.notifyCommand(requireContext(), command);
        }
    }
    private void unregisterAllCommands() {
        for (String command : commandMap.keySet()) {
            RemoteControlService.notifyCommandRemoval(requireContext(), command);
        }
    }

    private void registerReceiverIfNotRegistered() {
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(RemoteControlService.ACTION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(remoteControlReceiver,filter, Context.RECEIVER_EXPORTED);
            } else {
                requireContext().registerReceiver(remoteControlReceiver, filter);
            }
            isReceiverRegistered = true;
        }
        registerAllCommands();
    }

    private void unregisterReceiverIfRegistered() {
        unregisterAllCommands();
        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(remoteControlReceiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiverIfNotRegistered();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiverIfRegistered();
    }
}
