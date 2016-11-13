package com.project.hale.messgaesender.Wifi;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by mahon on 2016/11/13.
 */

public class SenderDevice {
    public WifiP2pDevice wifip2pdevice,nearestDevice;
    public int distance=0;
    public SenderDevice(WifiP2pDevice _wifip2pdevice,WifiP2pDevice _nearestDevice,int _distance){
        wifip2pdevice=_wifip2pdevice;
        nearestDevice=_nearestDevice;
        distance=_distance;
    }
    public SenderDevice(WifiP2pDevice _wifip2pdevice){
        wifip2pdevice=_wifip2pdevice;
        nearestDevice=_wifip2pdevice;
        distance=1;
    }
    public String toString(){
        return wifip2pdevice.deviceName;
    }
}
