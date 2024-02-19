package com.example.mynail360ssh;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyNotificationListenerService extends NotificationListenerService {
    private final String TAG = this.getClass().getSimpleName();
    private final String Notification_Settings = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

//    List<String> activeHandledNotificationKeys = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

//        if(activeHandledNotificationKeys.contains(sbn.getKey())) return;

        // Intercept and read the text of the posted notification
        String notificationText="";
//        for (StatusBarNotification sbm : MyNotificationListenerService.this.getActiveNotifications()) {
//            String title = String.valueOf(sbm.getNotification().tickerText);
//            //String text = sbm.getNotification().extras.getString("android.text");
//            String text = bundle2string(sbm.getNotification().extras);
//            String package_name = sbm.getPackageName();
//            notificationText = package_name+ ":"+title + "\n" + text;
//
//            Log.v("Notification title is:", package_name);
//            Log.v("Notification text is:", text);
//            Log.v("Notification Package Name is:", package_name);
//        }

//        Log.d("NotificationInterceptor", "Package: " + packageName + ", Text: " + notificationText);
//        Toast.makeText(this, "Noti:"+ notificationText, Toast.LENGTH_LONG).show();
        // Process the intercepted notification text as needed
        // For example, display it in your app's UI, analyze it, or take specific actions based on its content

        try {
            String android_title = sbn.getNotification().extras.getString("android.title", "");

            if (sbn.getNotification().actions != null && android_title != null && android_title.equals("999")) {
                for (Notification.Action action: sbn.getNotification().actions) {

                    if (action.title.toString().equalsIgnoreCase("Mark as read")) {
                        Toast.makeText(this, "Processed 999", Toast.LENGTH_LONG).show();
                        PendingIntent intent = action.actionIntent;

                        try {

                            intent.send();
                        } catch(PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string = "Bundle{";
        for (String key : bundle.keySet()) {
            string += " " + key + " => " + bundle.get(key) + ";";
        }
        string += " }Bundle";
        return string;
    }

    @Override
    public IBinder onBind(Intent i) {
        return super.onBind(i);
    }
}