package com.example.zombiedefencesystem.Services;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.zombiedefencesystem.R;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class UsageTrackService extends Service {

    MediaPlayer mp = new MediaPlayer();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        String topPackageName;
                        int timeOpen = 0;
                        while (true) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
                                long time = System.currentTimeMillis();
                                // We get usage stats for the last 10 seconds
                                List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*10, time);
                                // Sort the stats by the last time used
                                if(stats != null) {
                                    SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
                                    for (UsageStats usageStats : stats) {
                                        mySortedMap.put(usageStats.getLastTimeUsed(),usageStats);
                                    }
                                    if(!mySortedMap.isEmpty()) {
                                        topPackageName =  mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                                        if(topPackageName.matches("com.insgram.andoid|com.snapchat.android|com.tiktok.android|com.youtube.android")) {
                                            timeOpen++;
                                            if(timeOpen > 900)  {
                                                Vibrator v = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                                } else {
                                                    v.vibrate(500);
                                                }

                                                if(!mp.isPlaying()) {
                                                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                                                    mp = MediaPlayer.create(getApplicationContext(), notification);
                                                    mp = MediaPlayer.create(getApplicationContext(), R.raw.alarmsound);
                                                    mp.setLooping(true);
                                                    mp.start();
                                                }
                                            }
                                        } else {
                                            timeOpen = 0;
                                            mp.stop();
                                        }
                                        Log.e("ddd", String.valueOf(timeOpen));
                                    }
                                }
                            }
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();

        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    CHANNELID,
                    CHANNELID,
                    NotificationManager.IMPORTANCE_LOW
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }
        }
        Notification.Builder notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNELID)
                    .setContentText("Service is running")
                    .setContentTitle("Service enabled");
        }

        startForeground(1001, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
