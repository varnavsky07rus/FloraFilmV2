package com.alaka_ala.florafilm.ui.util.local;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    public static final String TAG = "PermissionManager";
    private static final int PERMISSION_REQUEST_CODE = 100;

    public interface PermissionResultCallback {
        void onPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
    }

    private final Context context;
    private final Activity activity;
    private final Fragment fragment;
    private PermissionResultCallback callback;
    private List<String> permissionsToRequest;

    public PermissionManager(Context context, Activity activity, Fragment fragment, PermissionResultCallback callback) {
        this.context = context;
        this.activity = activity;
        this.fragment = fragment;
        this.callback = callback;
        this.permissionsToRequest = new ArrayList<>();
    }

    public void requestPermissions() {
        permissionsToRequest.clear();

        // Запрос разрешения на чтение файлов
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        // Запрос разрешения на запись файлов
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // Запрос разрешения на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
            if (fragment != null) {
                fragment.requestPermissions(permissionsArray, PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(activity, permissionsArray, PERMISSION_REQUEST_CODE);
            }
        } else {
            Log.d(TAG, "All permissions already granted");
            if (callback != null) {
                callback.onPermissionsGranted();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            if (deniedPermissions.isEmpty()) {
                Log.d(TAG, "All permissions granted");
                if (callback != null) {
                    callback.onPermissionsGranted();
                }
            } else {
                Log.d(TAG, "Some permissions denied: " + deniedPermissions);
                if (callback != null) {
                    callback.onPermissionsDenied(deniedPermissions);
                }
            }
        }
    }

    public boolean hasAllPermissions() {
        List<String> permissionsToCheck = new ArrayList<>();

        // Проверка разрешения на чтение файлов
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToCheck.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissionsToCheck.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissionsToCheck.add(Manifest.permission.READ_MEDIA_AUDIO);
        } else {
            permissionsToCheck.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        // Проверка разрешения на запись файлов
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionsToCheck.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // Проверка разрешения на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToCheck.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        for (String permission : permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}