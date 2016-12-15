package com.android.tv.remotecontrol.service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.tv.remotecontrol.service.RemoteControlInputService;

public class BootReceiver extends BroadcastReceiver
{
    private static final String TAG = "BootReceiver";
    private static final boolean DEBUG = true;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (DEBUG) Log.d(TAG, "BootupActivity initiated");
        if (intent.getAction().endsWith(Intent.ACTION_BOOT_COMPLETED)) {
            if (DEBUG) Log.d(TAG, "Starting  RemoteControlInputService");
            Intent  rcinput = new Intent(context, RemoteControlInputService.class);
            context.startService(rcinput);
        }
    }
}