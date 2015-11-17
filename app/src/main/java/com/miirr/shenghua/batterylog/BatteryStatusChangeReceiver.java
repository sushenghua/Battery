package com.miirr.shenghua.batterylog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by shenghua on 11/16/15.
 */
public class BatteryStatusChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            Intent monitorIntent = new Intent(context, BatteryLogService.class);
            context.startService(monitorIntent);
        }
    }
}

