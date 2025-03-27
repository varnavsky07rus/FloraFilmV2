package com.alaka_ala.florafilm.ui.activities;

import static android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.ActivityMainBinding;
import com.alaka_ala.florafilm.ui.util.local.PermissionManager;
import com.google.android.material.navigation.NavigationView;

import java.util.List;


public class MainActivity extends AppCompatActivity implements PermissionManager.PermissionResultCallback {

    private ActivityMainBinding binding;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private AppBarConfiguration appBarConfiguration;
    private Toolbar toolbar;

    private float initialX;
    private float drawerWidth;
    private boolean isDragging = false;
    private PermissionManager permissionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        drawerLayout = binding.drawerLayout;
        NavigationView navigationView = binding.navView;


        // Получаем NavController из NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = navHostFragment.getNavController();

        // Создаем AppBarConfiguration
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setOpenableLayout(drawerLayout)
                .build();

        // Настраиваем ActionBar с NavController и AppBarConfiguration
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Настраиваем NavigationView с NavController
        NavigationUI.setupWithNavController(navigationView, navController);

        View header = navigationView.getHeaderView(0);
        ImageView donateButton = header.findViewById(R.id.imageViewDonate);
        // Загружаем анимацию из raw-ресурсов

        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.donateFragment);
            }
        });

        permissionManager = new PermissionManager(getApplicationContext(), this, null, this);
        checkPermissionsAndDoSomething();
        updateStatusBarIconColor(this);
    }

    private void checkPermissionsAndDoSomething() {
        if (permissionManager.hasAllPermissions()) {
            // Все разрешения есть, можно выполнять действия
            Toast.makeText(this, "Разрешения получены!", Toast.LENGTH_SHORT).show();
        } else {
            // Запрашиваем разрешения
            permissionManager.requestPermissions();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }


    public static void updateStatusBarIconColor(Activity activity) {
        int currentNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkTheme = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        if (isDarkTheme) {
            // White icons on dark background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.getWindow().getDecorView().getWindowInsetsController().setSystemBarsAppearance(0, APPEARANCE_LIGHT_STATUS_BARS);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.getWindow().getDecorView().getWindowInsetsController().setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS);
            }
            // Black icons on light background
        }

    }


    @Override
    public void onPermissionsGranted() {
        // Все разрешения предоставлены
        Log.d(PermissionManager.TAG, "onPermissionsGranted: All permissions granted");
        Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(List<String> deniedPermissions) {
        // Некоторые разрешения отклонены
        Log.d(PermissionManager.TAG, "onPermissionsDenied: Some permissions denied: " + deniedPermissions);
        Toast.makeText(this, "Some permissions denied", Toast.LENGTH_SHORT).show();
        // Обработать отклоненные разрешения
        // Например, показать сообщение пользователю
        for (String permission : deniedPermissions) {
            if (permission.equals(Manifest.permission.READ_MEDIA_IMAGES) ||
                    permission.equals(Manifest.permission.READ_MEDIA_VIDEO) ||
                    permission.equals(Manifest.permission.READ_MEDIA_AUDIO) ||
                    permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Обработка отклоненного разрешения на чтение
                Log.d(PermissionManager.TAG, "onPermissionsDenied: Read permission denied");
                Toast.makeText(this, "Read permission denied", Toast.LENGTH_SHORT).show();
            } else if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Обработка отклоненного разрешения на запись
                Log.d(PermissionManager.TAG, "onPermissionsDenied: Write permission denied");
                Toast.makeText(this, "Write permission denied", Toast.LENGTH_SHORT).show();
            } else if (permission.equals(Manifest.permission.POST_NOTIFICATIONS)) {
                // Обработка отклоненного разрешения на уведомления
                Log.d(PermissionManager.TAG, "onPermissionsDenied: Notification permission denied");
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}