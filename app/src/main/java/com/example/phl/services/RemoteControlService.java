package com.example.phl.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class RemoteControlService extends Service {
    private static Socket socket;

    public static final String MESSAGE = "message";
    public static final String COMMAND = "client/command";

    public static final String CLIENT_UP = "client";

    public static final String ADD = "client/add";
    public static final String REMOVE = "client/remove";

    public static final String NOTIFY_ACTION = "com.example.phl.services.RemoteControlService.NOTIFY";

    public static final String ID = "id";
    
    public static final String ACTION = "com.example.phl.services.RemoteControlService";

    public static final String SOCKET_CONNECTED_ACTION = "com.example.phl.services.RemoteControlService.SOCKET_CONNECTED";

    public static final String SOCKET_ERROR_ACTION = "com.example.phl.services.RemoteControlService.SOCKET_ERROR";

    public static final String API_SERVER = "https://phl.api.czou.me";

    public static final String WEB_SERVER = "https://phl.czou.me";

    private static boolean isSocketConnected = false;

    private static Set<String> addCommandBuffer = new HashSet<>();

    private static Set<String> removeCommandBuffer = new HashSet<>();

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra(ADD);
            String remove = intent.getStringExtra(REMOVE);
            Log.d("RemoteControlService", "Found command: " + command);
            if (command != null) {
                if (isSocketConnected) {
                    socket.emit(ADD, command, (Ack) args -> {
                        addCommandBuffer.remove(command);
                    });
                } else {
                    Log.e("RemoteControlService", "Socket not connected");
                }
            }
            if (remove != null) {
                if (isSocketConnected) {
                    socket.emit(REMOVE, remove, (Ack) args -> {
                        removeCommandBuffer.remove(remove);
                    });
                } else {
                    Log.e("RemoteControlService", "Socket not connected");
                }
            }
        }
    };

    public static void notifyCommand(Context context, String command) {
        Intent intent = new Intent(NOTIFY_ACTION);
        intent.putExtra(ADD, command);
        addCommandBuffer.add(command);
        context.sendBroadcast(intent);
    }

    public static void notifyCommandRemoval(Context context, String command) {
        Intent intent = new Intent(NOTIFY_ACTION);
        intent.putExtra(REMOVE, command);
        removeCommandBuffer.add(command);
        context.sendBroadcast(intent);
    }

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
        socket.emit(CLIENT_UP);
        isSocketConnected = true;
        Intent intent = new Intent(SOCKET_CONNECTED_ACTION);
        intent.putExtra(ID, socket.id());
        sendBroadcast(intent);
        for (String command : addCommandBuffer) {
            socket.emit(ADD, command, (Ack) args1 -> {
                addCommandBuffer.remove(command);
            });
        }
        for (String command : removeCommandBuffer) {
            socket.emit(REMOVE, command, (Ack) args1 -> {
                removeCommandBuffer.remove(command);
            });
        }
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
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.disconnect();
            socket.off(Socket.EVENT_CONNECT, onConnect);
            socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.off(COMMAND, onCommand);
            socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            isSocketConnected = false;
        }
        unregisterReceiver(broadcastReceiver);
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

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(broadcastReceiver, new IntentFilter(NOTIFY_ACTION));
    }

    public interface CommandHandler {
        void handle();
    }
}