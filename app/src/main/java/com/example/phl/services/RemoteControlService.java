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
    private static Socket socket;

    public static final String MESSAGE = "message";
    public static final String COMMAND = "command";
    public static final String ID = "id";
    public static final String ACTION = "com.example.phl.services.RemoteControlService";

    public static final String SOCKET_CONNECTED_ACTION = "com.example.phl.services.RemoteControlService.SOCKET_CONNECTED";

    public static final String SOCKET_ERROR_ACTION = "com.example.phl.services.RemoteControlService.SOCKET_ERROR";


    public static final String API_SERVER = "http://143.215.61.59:5000";

    private static boolean isSocketConnected = false;

    public RemoteControlService() {
        try {
            socket = IO.socket(API_SERVER);
            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on(COMMAND, onCommand);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            Log.i("RemoteControlService", "Connecting to server");
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onConnectError = args -> {
        Log.e("RemoteControlService", "Socket error: " + args[0]);
        isSocketConnected = false;
        Intent intent = new Intent(SOCKET_ERROR_ACTION);
        if (args.length > 0 && args[0] instanceof String) {
            intent.putExtra(MESSAGE, (String) args[0]);
        }
        sendBroadcast(intent);
    };

    private Emitter.Listener onConnect = args -> {
        // Handle connection
        Log.i("RemoteControlService", "Connected to server");
        Log.d("RemoteControlService", "Socket ID: " + socket.id());
        isSocketConnected = true;
        Intent intent = new Intent(SOCKET_CONNECTED_ACTION);
        intent.putExtra(ID, socket.id());
        sendBroadcast(intent);
    };

    private Emitter.Listener onDisconnect = args -> {
        // Handle disconnection
        Log.i("RemoteControlService", "Disconnected from server");
        isSocketConnected = false;
    };

    private Emitter.Listener onCommand = args -> {
        String command = (String) args[0];
        Log.d("RemoteControlService", "Received command: " + command);
        broadcastCommand(command);
    };

    private void broadcastCommand(String command) {
        Intent intent = new Intent(ACTION);
        intent.putExtra(COMMAND, command);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.disconnect();
            socket.off(Socket.EVENT_CONNECT, onConnect);
            socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.off(COMMAND, onCommand);
            isSocketConnected = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("RemoteControlService", "onBind");
        return null;
    }

    public static String getSocketId() {
        if (isSocketConnected && socket != null) {
            return socket.id();
        } else {
            return null;
        }
    }
}