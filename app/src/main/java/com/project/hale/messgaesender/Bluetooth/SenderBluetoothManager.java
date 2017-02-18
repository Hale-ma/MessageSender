package com.project.hale.messgaesender.Bluetooth;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.project.hale.messgaesender.Wifi.SenderCore;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;


public class SenderBluetoothManager {
    private Context context;
    private BluetoothSPP bluetoothSPP;
    private static SenderBluetoothManager sInstance = new SenderBluetoothManager();
    private String[] paired_device;
    public boolean isInit = false;
    private String connectedMAC = null;
    private String tarBT;
    private String cacheMAC="";
    private JSONObject cachedata;

    private SenderBluetoothManager() {

    }

    public static synchronized SenderBluetoothManager getInstance() {
        return sInstance;
    }

    public void init(Context c) {
        this.context = c;
        tarBT = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
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
        SenderCore.paired_device=paired_device;
        bluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Log.d("bt", "onDeviceConnected:" + name + " " + address);
                connectedMAC = address;
                if (cacheMAC.compareTo(connectedMAC)==0) {
                    Log.d("bt", "connected, sending.."); 
                    bluetoothSPP.send(cachedata.toString(), true);
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
        Log.d("bt", "mac:" +  getbtMAC());

        isInit = true;

    }

    public void send(String tarBT,String sorWiFi,String tarWiFi,String data) {
        cacheMAC = tarBT;
        cachedata = craftmessage(sorWiFi,tarWiFi,data);
        if (connectedMAC == null) {
            Log.d("bt", "sending, connecting");
            bluetoothSPP.connect(tarBT);
        } else if (connectedMAC.compareTo(tarBT) != 0) {
            Log.d("bt", "sending, switch connection!");
            bluetoothSPP.disconnect();
            bluetoothSPP.connect(tarBT);
        } else {
            Log.d("bt", "send directly");
            bluetoothSPP.send(cachedata.toString(), true);
        }
    }

    private JSONObject craftmessage(String sor, String tar, String data) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("sor", sor);
            jo.put("tar", tar);
            jo.put("time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            jo.put("data",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }


    public String getbtMAC() {
        return tarBT == null ? android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address") : tarBT;
    }


    private class datareceive implements BluetoothSPP.OnDataReceivedListener {

        @Override
        public void onDataReceived(byte[] data, String message) {
            Toast.makeText(context, message + " " + data.length, Toast.LENGTH_LONG);
            Log.d("bt", "onDataReceived:" + message);
            try {
                JSONObject jo=new JSONObject(message);
                SenderCore.onReceive(jo.getString("sor"),jo.getString("tar"),jo.getString("time"),jo.getString("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void endbt() {
        bluetoothSPP.stopService();
    }
}
