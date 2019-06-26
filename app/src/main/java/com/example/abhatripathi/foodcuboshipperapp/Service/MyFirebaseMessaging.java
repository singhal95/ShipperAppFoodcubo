package com.example.abhatripathi.foodcuboshipperapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.abhatripathi.foodcuboshipperapp.Common;
import com.example.abhatripathi.foodcuboshipperapp.Helper.NotificationHelper;
import com.example.abhatripathi.foodcuboshipperapp.HomeActivity;
import com.example.abhatripathi.foodcuboshipperapp.MainActivity;
import com.example.abhatripathi.foodcuboshipperapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotificatonAPI26(remoteMessage);
            } else {
                sendNotificaton(remoteMessage);
            }
        }
    }

    private void sendNotificatonAPI26(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("msg");

        //here we will fix to click to notification -->go to order list
        PendingIntent pendingIntent;
        NotificationHelper helper;
        Notification.Builder builder;

        if (Common.currentShipper != null) {
            Intent intent = new Intent(this, HomeActivity.class);//why home activity(me)????
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            helper = new NotificationHelper(this);
            builder = helper.channelNotification(title, message, pendingIntent, defaultSoundUri);

            helper.getManager().notify(new Random().nextInt(), builder.build());
        } else {
            //fix crash of notification send from news system
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            helper = new NotificationHelper(this);
            builder = helper.channelNotification(title, message, defaultSoundUri);

            helper.getManager().notify(new Random().nextInt(), builder.build());
        }
    }


    private void sendNotificaton(RemoteMessage remoteMessage) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        RemoteMessage.Notification data = remoteMessage.getNotification();
        String title = data.getTitle();
        String message = data.getBody();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
//        mBuilder.setSmallIcon(R.drawable.ic_local_shipping_black_24dp);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setAutoCancel(true);
        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.baseline_local_shipping_white_48dp);
            mBuilder.setColor(getResources().getColor(android.R.color.white));
        } else {*/
            mBuilder.setSmallIcon(R.drawable.ic_local_shipping_black_24dp);
        //}
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());



      /*  Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");
        if (Common.currentShipper != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_local_shipping_black_24dp)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert noti != null;
            noti.notify(0, builder.build());

        }
        else{
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_local_shipping_black_24dp)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri);

                NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert noti != null;
                noti.notify(0, builder.build());
        }*/
    }
}



