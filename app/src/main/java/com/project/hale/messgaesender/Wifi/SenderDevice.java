package com.project.hale.messgaesender.Wifi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import com.peak.salut.SalutDevice;

/**
 * Created by mahon on 2016/11/13.
 */

public class SenderDevice {
    public String wifiAddress;
    public SalutDevice salutDevice;
    public int distance=0;

    public SenderDevice(SalutDevice s){
        this.salutDevice=s;
    }
    public String toString(){
        return salutDevice.toString();
    }

}
