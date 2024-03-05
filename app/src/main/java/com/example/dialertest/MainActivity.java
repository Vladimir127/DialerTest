package com.example.dialertest;

import static android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME;

import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dialertest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Проверяем доступность на устройстве телефонных функций и GSM-модуля
        PackageManager pm = getPackageManager();
        boolean isTelephonySupported = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        boolean isGSMSupported = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_GSM);

        binding.setDefaultButton.setEnabled(isTelephonySupported && isGSMSupported);

        binding.setDefaultButton.setOnClickListener(view -> offerReplacingDefaultDialer());
    }

    private void offerReplacingDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent changeDialerIntent;
            changeDialerIntent = ((RoleManager) this.getSystemService(Context.ROLE_SERVICE)).createRequestRoleIntent(RoleManager.ROLE_DIALER);
            startActivityForResult(changeDialerIntent, 120);
        } else {
            Intent changeDialerIntent;
            changeDialerIntent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            changeDialerIntent.putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
            startActivity(changeDialerIntent);
        }
    }
}