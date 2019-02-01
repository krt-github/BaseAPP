package com.krt.basemodule.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.krt.basemodule.debug.Debug;

/**
 * @author KRT
 * 2018/11/26
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Debug.i("BootBroadcastReceiver", "------ Boot completed  new task??? ------");
        Intent service = new Intent(context, AbstractPushService.class);
        service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        }else{
            context.startService(service);
        }
    }
}
