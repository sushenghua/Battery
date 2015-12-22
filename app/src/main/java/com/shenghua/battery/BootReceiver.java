package com.shenghua.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by shenghua on 11/16/15.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(ACTION_BOOT)) {
            Intent monitorIntent = new Intent(context, BatteryLogService.class);

//            monitorIntent.putExtra(BatteryLogService.HANDLE_REBOOT, true);
            context.startService(monitorIntent);
        }
    }
}
