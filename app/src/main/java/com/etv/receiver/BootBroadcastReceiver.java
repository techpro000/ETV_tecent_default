package com.etv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.etv.activity.StartActivity;
import com.etv.config.AppConfig;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.i("BootBroadcastReceiver", "=ETV开机广播=============" + action);
        MyLog.cdl("开机广播====", true);
        if (action.equals(ACTION_BOOT)) {
            SharedPerManager.setLastDownTraff(0);
            SharedPerManager.setLastUploadTraff(0);
        }
    }
}