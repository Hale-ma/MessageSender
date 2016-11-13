package com.project.hale.messgaesender.Wifi;

import android.annotation.TargetApi;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mahon on 2016/11/9.
 */

public class WifiBoardCastManager {
    private static WifiBoardCastManager sInstance = new WifiBoardCastManager();
    private WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiP2pManager.DnsSdTxtRecordListener txtListener;
    WifiP2pManager.DnsSdServiceResponseListener servListener;
    List<SenderDevice> availableDevice = new ArrayList<SenderDevice>();

    //singleton constructer
    private WifiBoardCastManager() {

    }

    public static WifiBoardCastManager getsInstance() {
        return sInstance;
    }

    public void startRegistration(String target, String content) {
        //Create a string map containing information about your service.
        Map record = new HashMap();
        // Service information.Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "message.diffuse", record);
        }
        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(mChannel, serviceInfo, new myWifiActionListener("addLocalService"));
        Log.d("wifi service", "Service Registration - finish");

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

    public void init(WifiP2pManager wm, WifiP2pManager.Channel wc) {
        this.mManager = wm;
        this.mChannel = wc;
    }

    private class myDnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {
        @Override
        public void onDnsSdTxtRecordAvailable(String s, Map<String, String> record, WifiP2pDevice wifiP2pDevice) {
            Log.d("wifi", "DnsSdTxtRecord available -" + record.toString());
        }
    }

    private class myDnsSdServiceResponseListener implements WifiP2pManager.DnsSdServiceResponseListener {
        @Override
        public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {
            Log.d("wifi", "onBonjourServiceAvailable " + instanceName + " registrationtype :" + registrationType);

        }
    }
}