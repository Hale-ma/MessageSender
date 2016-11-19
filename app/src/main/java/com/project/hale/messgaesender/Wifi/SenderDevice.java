package com.project.hale.messgaesender.Wifi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by mahon on 2016/11/13.
 */

public class SenderDevice {
    public String deviceAddress;
    public WifiP2pDevice nearestDevice;
    public int distance=0;
    public SenderDevice(String _wifip2pdevice,WifiP2pDevice _nearestDevice,int _distance){
        deviceAddress=_wifip2pdevice;
        nearestDevice=_nearestDevice;
        distance=_distance;
    }
    public SenderDevice(WifiP2pDevice _wifip2pdevice){
        deviceAddress=_wifip2pdevice.deviceAddress;
        nearestDevice=_wifip2pdevice;
        distance=1;
    }
    public String toString(){
        return deviceAddress+"->"+nearestDevice;
    }

}
