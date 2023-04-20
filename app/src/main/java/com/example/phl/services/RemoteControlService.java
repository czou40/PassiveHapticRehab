package com.example.phl.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class RemoteControlService extends Service {
    private Socket socket;
    public static final String ACTION = "com.example.phl.services.RemoteControlService";
    public static final String API_SERVER = "http://143.215.61.59:5000";

    public RemoteControlService() {
        try {
            socket = IO.socket(API_SERVER);
            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on("command", onCommand);
            socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                Log.e("RemoteControlService", "Socket error: " + args[0]);
            });
            Log.i("RemoteControlService", "Connecting to server");
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onConnect = args -> {
        // Handle connection
        Log.i("RemoteControlService", "Connected to server");
        Log.d("RemoteControlService", "Socket ID: " + socket.id());
    };

    private Emitter.Listener onDisconnect = args -> {
        // Handle disconnection
        Log.i("RemoteControlService", "Disconnected from server");
    };

    private Emitter.Listener onCommand = args -> {
        String command = (String) args[0];
        Log.d("RemoteControlService", "Received command: " + command);
        broadcastMessage(command);
    };

    private void broadcastMessage(String message) {
        Intent intent = new Intent(ACTION);
        intent.putExtra("command", message);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.disconnect();
            socket.off(Socket.EVENT_CONNECT, onConnect);
            socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.off("command", onCommand);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("RemoteControlService", "onBind");
        return null;
    }
}