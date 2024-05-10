package com.example.phl.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {

    public static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1001;

    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1003;

    public static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1002;

    public static void checkAndRequestRecordAudioPermission(Activity activity) {
        // Check if the RECORD_AUDIO permission is already granted
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            // Do something that requires this permission
        }
    }

    public static boolean checkAndRequestCameraPermission(Activity activity) {
        // Check if the CAMERA permission is already granted
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        } else {
            // Permission already granted
            // Do something that requires this permission
            return true;
        }
    }

    public static void checkAndRequestBluetoothPermission(Activity activity) {
//        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Request the permission
//            ActivityCompat.requestPermissions(activity,
//                    new String[]{Manifest.permission.RECORD_AUDIO},
//                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
//        } else {
//            // Permission already granted
//            // Do something that requires this permission
//        }
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_ADVERTISE},
                    BLUETOOTH_PERMISSION_REQUEST_CODE);
        }
    }
}
