package net.refy.android.mstcc.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by yutaka on 10/15/2016.
 */
public  class WifiManagerReflection {
    // consts
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;

    // Base instance
    private WifiManager mManager;

    // Method caches
    private Method mGetWifiApConfiguration;
    private Method mSetWifiApEnabled;
    private Method mGetWifiApState;

    private Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return mManager.getClass().getMethod(name, parameterTypes);
    }

    public WifiManagerReflection(WifiManager manager) {
        mManager = manager;
    }

    public WifiConfiguration getWifiApConfiguration() {
        try {
            if (mGetWifiApConfiguration == null
                    && ((mGetWifiApConfiguration = getMethod("getWifiApConfiguration")) == null)) {
                return null;
            }
            return (WifiConfiguration) mGetWifiApConfiguration.invoke(mManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean setWifiApEnabled(WifiConfiguration configuration, boolean enabled) {
        try {
            if (mSetWifiApEnabled == null
                    && ((mSetWifiApEnabled = getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class)) == null)) {
                return false;
            }
            return (boolean) mSetWifiApEnabled.invoke(mManager, configuration, enabled);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getWifiApState() {
        try {
            if (mGetWifiApState == null
                    && ((mGetWifiApState = getMethod("getWifiApState")) == null)) {
                return WIFI_AP_STATE_FAILED;
            }
            return (int) mGetWifiApState.invoke(mManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return WIFI_AP_STATE_FAILED;
    }

    public byte[] getBSSID(String ifname) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/system/bin/ip", "addr"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            // split lines
            String[] lines = output.toString().split("\n");
            byte[] bssid = new byte[6];
            for (int i = 0; i < lines.length; ++i) {
                if (!lines[i].contains(ifname + ":")) {
                    continue;
                }
                String hwaddr = lines[i + 1].trim().split(" ")[1];
                String[] bytes = hwaddr.split(":");
                for (int j = 0; j < bytes.length; ++j) {
                    bssid[j] = (byte) Integer.parseInt(bytes[j].toUpperCase(), 16);
                }
                return bssid;
            }
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
        return new byte[6];
    }
}