package com.example.smartpomodoro;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView timerText, statusText;
    private boolean receiverRegistered = false;
    private final BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (TimerService.ACTION_TICK.equals(intent.getAction())) {
                int seconds = intent.getIntExtra(TimerService.EXTRA_REMAINING, 25 * 60);
                timerText.setText(formatTime(seconds));
                statusText.setText("專注中…");
            } else if (TimerService.ACTION_FINISHED.equals(intent.getAction())) {
                timerText.setText("00:00");
                statusText.setText("專注完成！");
            }
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 40, 28, 24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Smart Pomodoro Focus Timer");
        title.setTextSize(23); title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.BLACK); title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, 70));

        statusText = new TextView(this);
        statusText.setText("準備對焦"); statusText.setTextSize(17);
        statusText.setTextColor(Color.rgb(76, 150, 90)); statusText.setGravity(Gravity.CENTER);
        root.addView(statusText, new LinearLayout.LayoutParams(-1, 55));

        timerText = new TextView(this);
        timerText.setText("25:00"); timerText.setTextSize(52);
        timerText.setTypeface(null, Typeface.BOLD); timerText.setTextColor(Color.rgb(220, 65, 65));
        timerText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams timerParams = new LinearLayout.LayoutParams(-1, 230);
        timerParams.setMargins(0, 45, 0, 70); root.addView(timerText, timerParams);

        Button start = new Button(this); start.setText("開始對焦");
        Button stop = new Button(this); stop.setText("停止");
        LinearLayout buttons = new LinearLayout(this); buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(start, new LinearLayout.LayoutParams(0, 60, 1));
        buttons.addView(stop, new LinearLayout.LayoutParams(0, 60, 1));
        root.addView(buttons, new LinearLayout.LayoutParams(-1, 65));
        start.setOnClickListener(v -> startFocus());
        stop.setOnClickListener(v -> stopFocus());
        setContentView(root);
    }

    private void startFocus() {
        registerTimerReceiver();
        Intent service = new Intent(this, TimerService.class);
        service.setAction(TimerService.ACTION_START);
        service.putExtra(TimerService.EXTRA_DURATION, 25 * 60);
        startService(service);
        statusText.setText("專注中…");
    }

    private void stopFocus() {
        stopService(new Intent(this, TimerService.class));
        timerText.setText("25:00"); statusText.setText("已停止／準備對焦");
    }

    private void registerTimerReceiver() {
        if (receiverRegistered) return;
        IntentFilter filter = new IntentFilter();
        filter.addAction(TimerService.ACTION_TICK);
        filter.addAction(TimerService.ACTION_FINISHED);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(timerReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(timerReceiver, filter);
        receiverRegistered = true;
    }

    @Override protected void onDestroy() {
        if (receiverRegistered) { unregisterReceiver(timerReceiver); receiverRegistered = false; }
        super.onDestroy();
    }

    private String formatTime(int seconds) { return String.format(java.util.Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60); }
}
