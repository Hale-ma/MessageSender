package com.project.hale.messgaesender.Wifi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import com.peak.salut.SalutDevice;

/**
 * Created by mahon on 2016/11/13.
 */

public class SenderDevice {
    public String wifiAddress,btaddress;
    public int distance=0;
    public String time="";

    public SenderDevice(String wifiAddress){
        this.wifiAddress=wifiAddress;
        this.distance=0;
    }
    public SenderDevice(String wifiAddress,int distance,String time){
        this.wifiAddress=wifiAddress;
        this.distance=distance;
        this.time=time;

    }


    public String toString(){
        return wifiAddress;
    }

}
