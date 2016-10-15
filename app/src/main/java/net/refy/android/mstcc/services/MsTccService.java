package net.refy.android.mstcc.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import net.refy.android.mstcc.wifi.WifiManagerReflection;

/**
 * Created by yutaka on 10/15/2016.
 */
public class MsTccService extends Service {
    MsTccServer mServer;
    BluetoothBroadcastReceiver mReceiver;

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

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        mServer = new MsTccServer(adapter, refman);
        if (adapter.isEnabled()) runServer();

        // handle bluetooth state changing
        BluetoothBroadcastReceiver mReceiver = new BluetoothBroadcastReceiver();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        Log.i("MS-TCC", "Service started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Log.i("MS-TCC", "Service stopping");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    private void runServer() {
        mServer.start();
    }

    private void stopServer() {
        mServer.stop();
    }

    class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            int state = extras.getInt(BluetoothAdapter.EXTRA_STATE);
            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    runServer();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopServer();
                    break;
            }
        }
    }
}
