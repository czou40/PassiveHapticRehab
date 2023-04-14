package com.example.phl.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class BluetoothService {
    private static final String TAG = "BluetoothService";
    private static final UUID SERVICE_UUID = UUID.fromString("0000111f-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("00001112-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    public BluetoothService(Context context) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connectToDevice(BluetoothDevice device) {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        bluetoothGatt = device.connectGatt(null, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true);
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                String message = new String(characteristic.getValue());
                Log.i(TAG, "Received message: " + message);
                Toast.makeText(null, message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public static class DeviceListDialog {
        public interface OnDeviceSelectedListener {
            void onDeviceSelected(BluetoothDevice device);
        }

        private Context context;
        private OnDeviceSelectedListener listener;

        public DeviceListDialog(Context context, OnDeviceSelectedListener listener) {
            this.context = context;
            this.listener = listener;
        }

        public void show() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);

            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(bluetoothAdapter, null);

            for (ParcelUuid uuid: uuids) {
                Log.d(TAG, "UUID: " + uuid.getUuid().toString());
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            ArrayList<String> deviceNames = new ArrayList<>();
            final ArrayList<BluetoothDevice> devices = new ArrayList<>();

            for (BluetoothDevice device : pairedDevices) {
                deviceNames.add(device.getName());
                devices.add(device);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select a Bluetooth device");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    context, android.R.layout.select_dialog_singlechoice, deviceNames);

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BluetoothDevice selectedDevice = devices.get(which);
                    listener.onDeviceSelected(selectedDevice);
                    dialog.dismiss();
                }
            });

            builder.show();
        }
    }
}
