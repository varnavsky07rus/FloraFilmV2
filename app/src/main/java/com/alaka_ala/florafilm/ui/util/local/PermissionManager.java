package com.alaka_ala.florafilm.ui.util.local;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    public static final String TAG = "PermissionManager";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "PermissionManagerPrefs";
    private static final String PREF_FIRST_LAUNCH = "isFirstLaunch";

    public interface PermissionResultCallback {
        void onPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
    }

    private final Context context;
    private final PermissionResultCallback callback;

    public PermissionManager(Context context, PermissionResultCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    private boolean isFirstLaunch() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_FIRST_LAUNCH, true);
    }

    private void setFirstLaunchDone() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_FIRST_LAUNCH, false);
        editor.apply();
    }

    public void requestPermissionsIfNeeded(Activity activity) {
        if (isFirstLaunch()) {
            requestPermissions(activity);
            setFirstLaunchDone();
        } else if (callback != null) {
            if (hasAllPermissions()) {
                callback.onPermissionsGranted();
            } else {
                requestPermissions(activity);
            }
        }
    }

    public void requestPermissionsIfNeeded(Fragment fragment) {
        if (isFirstLaunch()) {
            requestPermissions(fragment);
            setFirstLaunchDone();
        } else if (callback != null) {
            if (hasAllPermissions()) {
                callback.onPermissionsGranted();
            } else {
                requestPermissions(fragment);
            }
        }
    }

    public void requestPermissions(Activity activity) {
        List<String> permissionsToRequest = getPermissionsToRequest();
        if (!permissionsToRequest.isEmpty()) {
            String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity, permissionsArray, PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "All permissions already granted");
            if (callback != null) {
                callback.onPermissionsGranted();
            }
        }
    }

    public void requestPermissions(Fragment fragment) {
        List<String> permissionsToRequest = getPermissionsToRequest();
        if (!permissionsToRequest.isEmpty()) {
            String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
            fragment.requestPermissions(permissionsArray, PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "All permissions already granted");
            if (callback != null) {
                callback.onPermissionsGranted();
            }
        }
    }

    private List<String> getPermissionsToRequest() {
        List<String> requiredPermissions = new ArrayList<>();

        // Add permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 (API 33) and above, use granular media permissions
            requiredPermissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            requiredPermissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            requiredPermissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            // For older versions, use legacy storage permission
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        // WRITE_EXTERNAL_STORAGE is only needed for apps targeting below Android 10 (API 29)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // Filter out the permissions that are already granted
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        return permissionsToRequest;
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
        return getPermissionsToRequest().isEmpty();
    }
}
