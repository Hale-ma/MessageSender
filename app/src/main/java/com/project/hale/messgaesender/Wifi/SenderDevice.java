package com.project.hale.messgaesender.Wifi;

import android.os.Handler;
import android.util.Log;

/**
 * Created by mahon on 2016/11/13.
 */

public class SenderDevice {
    public String wifiAddress, btaddress, nearestaddress;
    public int distance = 0;
    public String time = "";

    public SenderDevice(String wifiAddress, String nearestaddress, int distance, String time) {
        this.wifiAddress = wifiAddress;
        this.distance = distance;
        this.nearestaddress = nearestaddress;
        this.time = time;
        this.btaddress = "UNKNOWN";
    }

    public SenderDevice(String mac, String information) {
        this.wifiAddress = mac;
        String[] temp = information.split("\\|");
        this.time = temp[0];
        this.distance = Integer.parseInt(temp[1]);
        if (distance == 1) {
            this.btaddress = temp[2];
            this.nearestaddress = wifiAddress;
        } else {
            this.btaddress = "UNKNOWN";
            this.nearestaddress = temp[2];
        }
    }


    public String toString() {
        return wifiAddress;
    }


}
