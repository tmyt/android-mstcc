package net.refy.android.mstcc.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import net.refy.android.mstcc.wifi.WifiManagerReflection;

/**
 * Created by yutaka on 10/15/2016.
 */
public class MsTccService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        WifiManager man = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiManagerReflection refman = new WifiManagerReflection(man);

        MsTccServer server = new MsTccServer(BluetoothAdapter.getDefaultAdapter(), refman);
        server.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
}
