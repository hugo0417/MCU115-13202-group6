package com.example.smartpomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("pomodoro", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("device_restarted", true).putLong("last_boot_notice", System.currentTimeMillis()).apply();
        }
    }
}
