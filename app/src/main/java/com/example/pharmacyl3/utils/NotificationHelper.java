package com.example.pharmacyl3.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.pharmacyl3.AdminActivity;
import com.example.pharmacyl3.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "pharmacy_app_channel";
    private static final String CHANNEL_NAME = "Pharmacy App Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for Pharmacy App";
    private static int notificationId = 0;

    public static void showNotification(Context context, String title, String message) {
        // Start the notification service
        Intent serviceIntent = new Intent(context, NotificationService.class);
        serviceIntent.putExtra("title", title);
        serviceIntent.putExtra("message", message);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, 
                    CHANNEL_NAME, 
                    importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static class NotificationService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            
            if (title != null && message != null) {
                createNotificationChannel(this);
                
                // Create an explicit intent for an Activity in your app
                Intent activityIntent = new Intent(this, AdminActivity.class);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        this, 
                        0, 
                        activityIntent, 
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                // Build the notification
                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

                // Start foreground service for Android 8.0+
                startForeground(notificationId + 1, notification);
                
                // Also show as a regular notification
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.notify(notificationId++, notification);
                }
                
                // Stop the service after showing the notification
                stopSelf();
            }
            
            return START_NOT_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
