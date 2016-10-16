package net.refy.android.mstcc.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.refy.android.mstcc.services.MsTccService;

/**
 * Created by yutaka on 10/15/2016.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context.getApplicationContext(), MsTccService.class);
        context.getApplicationContext().startService(i);
    }
}
