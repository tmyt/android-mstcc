package net.refy.android.mstcc.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

import net.refy.android.mstcc.wifi.WifiManagerReflection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by yutaka on 10/15/2016.
 */
public class MsTccServer {
    private String MsTccUuidString = "232E51D8-91FF-4c24-AC0F-9EE055DA30A5";
    private UUID MsTccUuid = UUID.fromString(MsTccUuidString);
    private BluetoothAdapter mAdapter;
    private WifiManagerReflection mWifiManagerRef;

    public MsTccServer(BluetoothAdapter adapter, WifiManagerReflection wifiManagerRef) {
        mAdapter = adapter;
        mWifiManagerRef = wifiManagerRef;
    }

    public void start() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                serverThread();
            }
        });
        thread.start();
    }

    private void serverThread() {
        while (true) {
            try {
                WifiConfiguration config = mWifiManagerRef.getWifiApConfiguration();
                BluetoothServerSocket socket = mAdapter.listenUsingRfcommWithServiceRecord(config.SSID, MsTccUuid);
                Log.d("MS-TCC", "Start listening MS-TCC socket");
                try {
                    final BluetoothSocket client = socket.accept();
                    Log.d("MS-TCC", "Client arrived");
                    clientThread(client);
                    Log.d("MS-TCC", "Cleaning up sockets");
                    client.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void clientThread(BluetoothSocket client) {
        if (client == null) return;
        try {
            InputStream input = client.getInputStream();
            OutputStream output = client.getOutputStream();
            byte[] header = new byte[3];
            while (true) {
                input.read(header);
                handleMessage(header, input, output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(byte[] header, InputStream input, OutputStream output) throws IOException {
        Log.i("MS-TCC", "Request Header ID: " + header[0]);
        if (header[0] == 1) {
            int state = bringingUpWifiAp();
            if (state == 0) {
                // send success response
                output.write(generateSuccessResponse());
                output.flush();
            } else {
                // send failure response
                output.write(generateFailureResponse(state));
                output.flush();
            }
        } else {
            output.write(generateProtocolErrorResponse(header[0]));
            output.flush();
            input.skip(input.available());
        }
    }

    private byte[] generateSuccessResponse() throws UnsupportedEncodingException {
        WifiConfiguration config = mWifiManagerRef.getWifiApConfiguration();
        // get ingredients
        byte[] ssidchars = config.SSID.getBytes("UTF-8");
        byte[] passchars = config.preSharedKey.getBytes("UTF-8");
        byte[] bssidVal = mWifiManagerRef.getBSSID("wlan0");
        // gen strusts
        byte[] ssid = buildStruct(2, ssidchars);
        byte[] bssid = buildStruct(3, bssidVal);
        byte[] passphrase = buildStruct(4, passchars);
        byte[] displayName = buildStruct(5, ssidchars);
        // gen response
        byte[] response = buildStruct(2, ssid, bssid, passphrase, displayName);
        dumpBytes(response);
        return response;
    }

    private byte[] generateFailureResponse(int state) {
        byte[] errorMessage = buildStruct(1, new byte[]{(byte) state});
        byte[] response = buildStruct(3, errorMessage);
        dumpBytes(response);
        return response;
    }

    private byte[] generateProtocolErrorResponse(int type) {
        byte[] errorType = buildStruct(7, new byte[]{(byte) type});
        byte[] response = buildStruct(4, errorType);
        dumpBytes(response);
        return response;
    }

    private byte[] buildStruct(int id, byte[]... payloads) {
        int payloadLength = 0;
        for (byte[] payload : payloads) payloadLength += payload.length;
        byte[] struct = new byte[3 + payloadLength];
        struct[0] = (byte) id;
        // payload length stored with NetworkByteOrder
        struct[1] = (byte) ((payloadLength >> 8) & 0xff);
        struct[2] = (byte) (payloadLength & 0xff);
        // copy payload
        int offset = 3;
        for (int i = 0; i < payloads.length; ++i) {
            copyBytesTo(struct, offset, payloads[i]);
            offset += payloads[i].length;
        }
        return struct;
    }

    private void copyBytesTo(byte[] target, int offset, byte[] source) {
        for (int i = 0; i < source.length; ++i) target[offset + i] = source[i];
    }

    private void dumpBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i % 16 == 0) sb.append(String.format("%1$04X: ", i));
            sb.append(String.format("%1$02X ", ((int) bytes[i]) & 0xff));
            if (i % 16 == 15) {
                Log.d("MS-TCC", sb.toString());
                sb.setLength(0);
            }
        }
    }

    private int bringingUpWifiAp() {
        mWifiManagerRef.setWifiApEnabled(null, true);
        long timer = System.currentTimeMillis() + (30 * 1000);
        while (timer > System.currentTimeMillis()) {
            switch (mWifiManagerRef.getWifiApState()) {
                case WifiManagerReflection.WIFI_AP_STATE_DISABLING:
                case WifiManagerReflection.WIFI_AP_STATE_FAILED:
                    // error
                    return 1;
                case WifiManagerReflection.WIFI_AP_STATE_DISABLED:
                case WifiManagerReflection.WIFI_AP_STATE_ENABLING:
                    // wait
                    break;
                case WifiManagerReflection.WIFI_AP_STATE_ENABLED:
                    // success
                    return 0;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        return 1;
    }
}