package com.project.hale.messgaesender.Bluetooth;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * Created by mahon on 2017/2/9.
 */

public class SenderBluetoothManager {
    private Context context;
    private BluetoothSPP bluetoothSPP;
    public static SenderBluetoothManager sInstance = new SenderBluetoothManager();
    public String[] paired_device;
    public boolean isInit = false;
    public String connectedMAC = null;
    private String btMAC;
    private String cacheMAC, cachedata;

    private SenderBluetoothManager() {

    }

    public static synchronized SenderBluetoothManager getInstance() {
        return sInstance;
    }

    public void init(Context c) {
        this.context = c;
        btMAC = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
        bluetoothSPP = new BluetoothSPP(context);
        if (!bluetoothSPP.isBluetoothEnabled()) {
            bluetoothSPP.enable();
            Log.d("bt", "bluetooth has not enabled");
        } else {

            Log.d("bt", "bluetooth has  enabled");
        }
        bluetoothSPP.setupService();
        bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
        paired_device = bluetoothSPP.getPairedDeviceAddress();
        for (int i = 0; i < paired_device.length; i++) {
            Log.d("bt", paired_device[i]);
        }
        bluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Log.d("bt", "onDeviceConnected:" + name + " " + address);
                connectedMAC = address;
                if (cacheMAC == connectedMAC) {
                    Log.d("bt", "connected, sending..");
                    bluetoothSPP.send(cachedata, true);
                }
            }

            public void onDeviceDisconnected() {
                Log.d("bt", "onDeviceDisconnected");
                connectedMAC = null;
            }

            public void onDeviceConnectionFailed() {
                Log.d("bt", "onDeviceConnectionFailed");
            }
        });
        bluetoothSPP.setOnDataReceivedListener(new datareceive());
//
        //    bluetoothSPP.connect("C0:C9:76:DA:53:B3");
        Log.d("bt", "mac:" + getbtMAC());

        isInit = true;

    }

    public void send(String btMAC, String data) {
        cacheMAC = btMAC;
        cachedata = data;
        if (connectedMAC == null) {
            Log.d("bt", "sending, connecting");
            bluetoothSPP.connect(btMAC);
        } else if (connectedMAC.compareTo(btMAC) != 0) {
            Log.d("bt", "sending, switch connection!");
            bluetoothSPP.disconnect();
            bluetoothSPP.connect(btMAC);
        } else {
            Log.d("bt", "send directly");
            bluetoothSPP.send(data, true);
        }
    }



    public String getbtMAC() {
        return btMAC == null ? android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address") : btMAC;
    }


    private class datareceive implements BluetoothSPP.OnDataReceivedListener {

        @Override
        public void onDataReceived(byte[] data, String message) {
            Toast.makeText(context, message + " " + data.length, Toast.LENGTH_LONG);
            Log.d("bt", "onDataReceived:" + message);
        }
    }

    public void endbt(){
        bluetoothSPP.stopService();
    }
}
