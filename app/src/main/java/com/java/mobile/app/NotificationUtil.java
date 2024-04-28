package com.java.mobile.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationUtil {

    static void createNotification(Context context, String title, String content) {
        String channelId = "my_channel_id";
        int notificationId = 1;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel (for Android Oreo and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "My Channel", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Create an intent for when the notification is tapped
        Intent intent = new Intent(context, ObjectDetectionActivity.class); // Replace with the desired activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your notification icon
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setAutoCancel(true); // Dismiss the notification when tapped

        // Notify
        notificationManager.notify(notificationId, builder.build());
    }
}
