package com.project.hale.messgaesender.Bluetooth;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.project.hale.messgaesender.Wifi.SenderCore;
import com.project.hale.messgaesender.Wifi.SenderDevice;
import com.project.hale.messgaesender.Wifi.SenderWifiManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;


public class SenderBluetoothManager {
    private Context context;
    private BluetoothSPP bluetoothSPP;
    private static SenderBluetoothManager sInstance = new SenderBluetoothManager();
    private String[] paired_device;
    public boolean isInit = false;
    public String connectedMAC = null;
    private String tarBT;
    private String cacheMAC = "";
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
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    bluetoothSPP.setupService();
                    bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
                }
            }, 1000);
            Log.d("bt", "bluetooth has not enabled");
        } else {
            bluetoothSPP.setupService();
            bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
            Log.d("bt", "bluetooth has  enabled");
        }
        paired_device = bluetoothSPP.getPairedDeviceAddress();
        SenderCore.getsInstance().paired_device = paired_device;
        bluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Log.d("bt", "onDeviceConnected:" + name + " " + address);
                connectedMAC = address;
                if (cacheMAC.compareTo(connectedMAC) == 0) {
                    Log.d("bt", "connected, sending..");
                    //when the first time it connect to a bluetooth device, it send it neighbour information to the node, in this way ,two device will exchange the neighbour information when connected
                    bluetoothSPP.send(SenderCore.getsInstance().neighbour_message(true).toString(), true);
                    bluetoothSPP.send(cachedata.toString(), true);
                    SenderCore.getsInstance().onSuccess();
                    // bluetoothSPP.disconnect();
                }
            }

            public void onDeviceDisconnected() {
                Log.d("bt", "onDeviceDisconnected");
                connectedMAC = null;
            }

            public void onDeviceConnectionFailed() {
                SenderCore.getsInstance().onBTfaild();
                Log.d("bt", "onDeviceConnectionFailed");

            }
        });
        bluetoothSPP.setOnDataReceivedListener(new datareceive());
        Log.d("bt", "mac:" + getbtMAC());

        isInit = true;

    }

    /**
     * try to connect to the target Bluetooth device and send the data .
     *
     * @param tarBT
     * @param sorWiFi
     * @param tarWiFi
     * @param data
     */
    public void send(String tarBT, String sorWiFi, String tarWiFi, String data) {
        cacheMAC = tarBT;
        cachedata = craftmessage(sorWiFi, tarWiFi, data);
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
            SenderCore.getsInstance().onSuccess();
        }
    }

    private JSONObject craftmessage(String sor, String tar, String data) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("sor", sor);
            jo.put("tar", tar);
            jo.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            jo.put("data", data);
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
            Log.d("SenderCore", "onDataReceived:" + message);
            try {
                JSONObject jo = new JSONObject(message);
                SenderCore.getsInstance().onReceive(jo.getString("sor"), jo.getString("tar"), jo.getString("time"), jo.getString("data"));
            } catch (JSONException e) {
                try {
                    SenderCore.getsInstance().startupdateDeviceInformation();
                    JSONArray ja = new JSONArray(message);
                    for (int i = 0; i < ja.length(); i++) {
                        String temp = ja.getString(i);
                        String splited[] = temp.split("\\|");
                        if (splited[1].compareTo(connectedMAC)==0) {
                            SenderCore.getsInstance().updateDeviceInformation_bymessage(splited[0], splited[1], splited[0]);
                            Log.d("SenderCore","new device by BT:"+splited[0]+" "+ splited[1]);
                        }else{
                        SenderCore.getsInstance().updateDeviceInformation_bySharing(splited[0], splited[1], Integer.parseInt(splited[2]), SenderCore.getsInstance().getWifiMac(connectedMAC));
                    }}
                    SenderCore.getsInstance().finishDeviceUpdate();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void endbt() {
        bluetoothSPP.disconnect();
        bluetoothSPP.stopService();
    }


}
