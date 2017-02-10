package com.project.hale.messgaesender.Bluetooth;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.project.hale.messgaesender.Wifi.SenderWifiManager;

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

    private SenderBluetoothManager() {

    }

    public static synchronized SenderBluetoothManager getInstance() {
        return sInstance;
    }

    public void init(Context c) {
        this.context = c;
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
                bluetoothSPP.send(("heiheiheihuhuhei"+name).getBytes(),false);
            }

            public void onDeviceDisconnected() {
                Log.d("bt", "onDeviceDisconnected");
            }

            public void onDeviceConnectionFailed() {
                Log.d("bt", "onDeviceConnectionFailed");
            }
        });
        bluetoothSPP.setOnDataReceivedListener(new datareceive());
//
        bluetoothSPP.connect("C0:C9:76:DA:53:B3");


        isInit=true;

    }

    private class datareceive implements BluetoothSPP.OnDataReceivedListener {

        @Override
        public void onDataReceived(byte[] data, String message) {
            Toast.makeText(context,message+" "+data.length,Toast.LENGTH_LONG);
            Log.d("bt", "onDataReceived:"+ message);
        }
    }
}
