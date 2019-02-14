package com.github.javiersantos.appupdater;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null || intent.getAction() == null)
            return;

        switch (intent.getAction()) {
            case "disable":
                new LibraryPreferences(context).setAppUpdaterShow(false);
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
                break;
        }
    }
}
