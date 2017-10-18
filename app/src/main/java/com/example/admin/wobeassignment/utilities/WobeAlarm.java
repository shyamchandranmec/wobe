package com.example.admin.wobeassignment.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.admin.wobeassignment.activities.DashboardActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shyam on 15/10/17.
 */

public class WobeAlarm extends BroadcastReceiver
{
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        String lastTransTime = SharedPreferenceManager.getInstance(context).getString(Constants.LAST_TRANS_TIME);
        System.out.println(lastTransTime);
        SimpleDateFormat simpleDateFormat =  new java.text.SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        try {
            if(lastTransTime != null) {
                String currentTime =  new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                Date currentDateTime = simpleDateFormat.parse(currentTime);
                Date lastTransDateTime = simpleDateFormat.parse(lastTransTime);
                long result = ((currentDateTime.getTime()/60000) - (lastTransDateTime.getTime()/60000));
                if(result > 5) {
                    notificationManager.notify(id, notification);
                }
                System.out.println(result);
            }

        } catch (Exception e) {
            System.out.println("Exception");
        }


    }
}