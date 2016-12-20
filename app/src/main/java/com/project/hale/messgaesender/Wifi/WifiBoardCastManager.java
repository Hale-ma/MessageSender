package com.project.hale.messgaesender.Wifi;

import android.annotation.TargetApi;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.project.hale.messgaesender.DeviceListFragment;
import com.project.hale.messgaesender.MainActivity;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;

/**
 * Created by mahon on 2016/11/9.
 */

public class WifiBoardCastManager {
    private static WifiBoardCastManager sInstance = new WifiBoardCastManager();
    private WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiP2pManager.DnsSdTxtRecordListener txtListener;
    WifiP2pManager.DnsSdServiceResponseListener servListener;
    Hashtable<String, SenderDevice> availableDevice = new Hashtable<String, SenderDevice>();
    WifiP2pDnsSdServiceInfo serviceInfo = null;
    ArrayList<WifiP2pDnsSdServiceInfo> boardcastingList = new ArrayList<WifiP2pDnsSdServiceInfo>();
    DeviceListFragment dfra = null;
    public static String MacAddr;

    //singleton constructor
    private WifiBoardCastManager() {

    }

    public static WifiBoardCastManager getsInstance() {
        return sInstance;
    }

    public void startRegistration() {
        Map record = new HashMap();
        Iterator it = availableDevice.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            record.put(entry.getKey(), ((SenderDevice) (entry.getValue())).distance + "");
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("find", "message.diffuse", record);
        }
        mManager.addLocalService(mChannel, serviceInfo, new myWifiActionListener("addLocalService_find"));
        Log.d("wifi service", "Service Registration - Available:" + availableDevice.size());
    }

    public void startBoradcast(String targetMac, String message) {
        Map record = new HashMap();
        record.put("sor", getMacAddr());
        record.put("tar",targetMac);
        record.put("msg", message);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("send", "message.diffuse", record);
        }
        mManager.addLocalService(mChannel, serviceInfo, new myWifiActionListener("addLocalService_send"));
        Log.d("wifi service", "target:" + targetMac + " message：" + message);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void discoverService() {
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new myWifiActionListener("Server Request"));
        mManager.discoverServices(mChannel, new myWifiActionListener("discoverServices"));
        txtListener = new myDnsSdTxtRecordListener();
        servListener = new myDnsSdServiceResponseListener();
        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);
        Log.d("wifi", "setDnsSdResponseListeners ");

    }

    public void init(WifiP2pManager wm, WifiP2pManager.Channel wc, DeviceListFragment df) {
        this.mManager = wm;
        this.mChannel = wc;
        this.dfra = df;
        this.discoverService();
        startRegistration();
    }

    private class myDnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {
        @Override
        public void onDnsSdTxtRecordAvailable(String s, Map<String, String> record, WifiP2pDevice wifiP2pDevice) {
            if (s.equals("find.message.diffuse.local.") && !availableDevice.containsKey(wifiP2pDevice.deviceAddress)) {//to be change
                if (availableDevice.containsKey(wifiP2pDevice.deviceAddress)) {
                    availableDevice.get(wifiP2pDevice.deviceAddress).distance = 1;
                }
            //    availableDevice.put(wifiP2pDevice.deviceAddress, new SenderDevice(wifiP2pDevice));
                Log.d("wifi service-receive", "DnsSdTxtRecord available -" + record.toString() + wifiP2pDevice.deviceAddress);
                if (serviceInfo != null) {
                    sInstance.mManager.removeLocalService(mChannel, serviceInfo, new myWifiActionListener("remove"));
                }
                WifiBoardCastManager.getsInstance().discoverService();
                sInstance.startRegistration();
                dfra.updateUI();

            } else if (s.equals("send.message.diffuse.local.")) {
                Log.d("wifi eceive",record.toString());
                if(record.get("tar").equals(getMacAddr())){
                    Log.d("wifi receive","It is mine:"+record.get("msg"));
                }else {
                    Log.d("wifi receive","It not mine:"+record.get("msg"));
                }
                WifiBoardCastManager.getsInstance().discoverService();

            } else {
                dfra.updateUI();
                Log.d("wifi service", "s:" + s + " " + wifiP2pDevice.deviceAddress);
                WifiBoardCastManager.getsInstance().discoverService();
            }
        }
    }

    private class myDnsSdServiceResponseListener implements WifiP2pManager.DnsSdServiceResponseListener {
        @Override
        public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {
            Log.d("wifi service-receive", "onBonjourServiceAvailable " + instanceName + " registrationtype :" + registrationType);

        }
    }

    public List<SenderDevice> getDevice() {
        return new ArrayList<>(availableDevice.values());
    }

    @NonNull
    public static String getMacAddr() {
        if(MacAddr!=null){
            return MacAddr;
        }
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                MacAddr=res1.toString();
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }
}