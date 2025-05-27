package com.example.nannynetapp;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Notification helper.
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    /**
     * Show notification.
     *
     * @param context the context
     * @param title   the title
     * @param message the message
     */
    public static void showNotification(Context context, String title, String message) {
        String channelId = "nannynet_channel";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // יצירת ערוץ התראות (לאנדרואיד 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "NannyNet Alerts", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.notification_icon)
                .setAutoCancel(true);

        manager.notify(1, builder.build());
    }

    /**
     * Send push notification.
     *
     * @param context the context
     * @param token   the token
     * @param title   the title
     * @param message the message
     */
    public static void sendPushNotification(Context context, String token, String title, String message) {
        // מימוש עם Firebase Cloud Messaging אם נדרש (לשליחה אמיתית לפי token)
        showNotification(context, title, message); // לצורך הדגמה
    }

    /**
     * Send fcm notification.
     *
     * @param userToken the user token
     * @param title     the title
     * @param message   the message
     */
    public static void sendFCMNotification(String userToken, String title, String message) {
        // Create FCM message
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);

        RemoteMessage.Builder builder = new RemoteMessage.Builder(userToken)
                .setData(data);

        // Send FCM message
        FirebaseMessaging.getInstance().send(builder.build());
    }

    /**
     * Update user fcm token.
     *
     * @param userId the user id
     */
    public static void updateUserFCMToken(String userId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        // Save the token to the user's profile in Firebase
                        FirebaseDatabase.getInstance().getReference()
                                .child("Users")
                                .child(userId)
                                .child("fcmToken")
                                .setValue(token)
                                .addOnSuccessListener(aVoid -> 
                                    Log.d(TAG, "FCM Token updated successfully"))
                                .addOnFailureListener(e -> 
                                    Log.e(TAG, "Failed to update FCM token", e));
                    }
                })
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Failed to get FCM token", e));
    }
}
