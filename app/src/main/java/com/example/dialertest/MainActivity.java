package com.example.dialertest;

import static android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME;

import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dialertest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int REQUEST_DEFAULT_DIALER = 1;
    private static final int REQUEST_DEFAULT_SPAM_BLOCKER = 2;
    private static final int REQUEST_OVERLAY_PERMISSION = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Проверяем доступность на устройстве телефонных функций и GSM-модуля
        PackageManager pm = getPackageManager();
        boolean isTelephonySupported = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        boolean isGSMSupported = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_GSM);

        binding.defaultDialerButton.setEnabled(isTelephonySupported && isGSMSupported);
        binding.defaultAntiSpamButton.setEnabled(isTelephonySupported && isGSMSupported);

        binding.defaultDialerButton.setOnClickListener(view -> offerReplacingDefaultDialer());
        binding.defaultAntiSpamButton.setOnClickListener(view -> offerReplacingDefaultSpamBlocker());

        requestOverlayPermission();
    }

    private void offerReplacingDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent intent;
            intent = ((RoleManager) this.getSystemService(Context.ROLE_SERVICE)).createRequestRoleIntent(RoleManager.ROLE_DIALER);
            startActivityForResult(intent, REQUEST_DEFAULT_DIALER);
        } else {
            Intent changeDialerIntent;
            changeDialerIntent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            changeDialerIntent.putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
            startActivity(changeDialerIntent);
        }
    }

    private void offerReplacingDefaultSpamBlocker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent intent = ((RoleManager) this.getSystemService(Context.ROLE_SERVICE)).createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
            startActivityForResult(intent, REQUEST_DEFAULT_SPAM_BLOCKER);
        }
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestOverlayPermission() {
        if (!hasOverlayPermission()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Разрешение на отображение поверх других приложений");
            builder.setMessage("Для корректной работы приложения необходимо разрешение на отображение поверх других приложений. Разрешить?");
            builder.setPositiveButton("Да", (dialogInterface, i) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            });
            builder.setNegativeButton("Нет", (dialogInterface, i) -> dialogInterface.cancel());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DEFAULT_SPAM_BLOCKER) {
            requestOverlayPermission();
        }
    }
}