package com.example.smartpomodoro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

public class TimerService extends Service {
    public static final String ACTION_START = "com.example.smartpomodoro.START";
    public static final String ACTION_TICK = "com.example.smartpomodoro.TIMER_UPDATE";
    public static final String ACTION_FINISHED = "com.example.smartpomodoro.TIMER_FINISHED";
    public static final String EXTRA_DURATION = "duration";
    public static final String EXTRA_REMAINING = "remaining";
    private static final String CHANNEL_ID = "pomodoro_timer";
    private volatile boolean running = false;
    private Thread timerThread;
    private int remaining = 25 * 60;
    private ToneGenerator tone;
    private boolean headsetReceiverRegistered = false;

    private final BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                Toast.makeText(TimerService.this, "保持專注！剩 " + (remaining / 60) + " 分鐘", Toast.LENGTH_SHORT).show();
            } else if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                int state = intent.getIntExtra("state", -1);
                if (state == 0) { if (tone != null) tone.stopTone(); }
                else if (state == 1 && running && tone != null) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
            }
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 35);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(deviceReceiver, filter);
        headsetReceiverRegistered = true;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_START.equals(intent.getAction())) {
            remaining = intent.getIntExtra(EXTRA_DURATION, 25 * 60);
            startForeground(1, buildNotification("專注計時進行中"));
            startTimer();
        }
        return START_NOT_STICKY;
    }

    private void startTimer() {
        if (running) return;
        running = true;
        timerThread = new Thread(() -> {
            while (running && remaining > 0) {
                broadcastTick();
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                remaining--;
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(1, buildNotification("剩餘時間 " + String.format(java.util.Locale.getDefault(), "%02d:%02d", remaining / 60, remaining % 60)));
            }
            if (running && remaining <= 0) {
                running = false;
                Intent update = new Intent(ACTION_FINISHED); update.setPackage(getPackageName()); sendBroadcast(update);
                Intent finish = new Intent(this, FinishActivity.class);
                finish.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(finish);
                stopSelf();
            }
        }, "PomodoroTimerThread");
        timerThread.start();
    }

    private void broadcastTick() {
        Intent update = new Intent(ACTION_TICK);
        update.setPackage(getPackageName());
        update.putExtra(EXTRA_REMAINING, remaining);
        sendBroadcast(update);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "番茄鐘計時", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String text) {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        if (Build.VERSION.SDK_INT >= 26) return new Notification.Builder(this, CHANNEL_ID).setContentTitle("智慧番茄鐘").setContentText(text).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentIntent(pi).setOngoing(true).build();
        return new Notification.Builder(this).setContentTitle("智慧番茄鐘").setContentText(text).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentIntent(pi).setOngoing(true).build();
    }

    @Override public void onDestroy() {
        running = false;
        if (timerThread != null) timerThread.interrupt();
        if (headsetReceiverRegistered) { unregisterReceiver(deviceReceiver); headsetReceiverRegistered = false; }
        if (tone != null) { tone.release(); tone = null; }
        if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_REMOVE); else stopForeground(true);
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
