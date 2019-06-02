package com.example.abhatripathi.foodcuboshipperapp.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.example.abhatripathi.foodcuboshipperapp.R;

public class NotificationHelper extends ContextWrapper {


    public static final String FOOD_CHANNEL_ID="com.example.abhatripathi.foodcuboshipperapp";
    public static final String FOOD_CHANNEL_NAME="FoodCubo";

    public NotificationManager manager;


    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
            createChannel();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel= new NotificationChannel(FOOD_CHANNEL_ID,
                FOOD_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);


    }

    public NotificationManager getManager() {
        if (manager == null)
            manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification .Builder channelNotification(String title, String body, PendingIntent contentIntent, Uri SoundUri){
        return new Notification.Builder(getApplicationContext(),FOOD_CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_local_shipping_black_24dp)
                .setSound(SoundUri)
                .setAutoCancel(false);

    }
    @TargetApi(Build.VERSION_CODES.O)
    public Notification .Builder channelNotification(String title, String body, Uri SoundUri){
        return new Notification.Builder(getApplicationContext(),FOOD_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_local_shipping_black_24dp)
                .setSound(SoundUri)
                .setAutoCancel(false);

    }

}



