package com.project.hale.messgaesender.Wifi;

import android.util.Log;

/**
 * Created by mahon on 2016/11/13.
 */

public class SenderDevice {
    public String wifiAddress, btaddress, nearestaddress;
    public int distance = 0;
    public String time = "";

//    public SenderDevice(String wifiAddress) {
//            this.wifiAddress = wifiAddress;
//            this.distance = 0;
//        }
//
//        public SenderDevice(String wifiAddress, int distance, String time) {
//            this(wifiAddress, null, distance, time);
//
//    }

//    public SenderDevice(String wifiAddress, String btaddress, int distance, String time) {
//        this.wifiAddress = wifiAddress;
//        this.distance = distance;
//        this.time = time;
//        this.btaddress = btaddress;
//    }

    public SenderDevice(String mac, String information) {
        this.wifiAddress = mac;
        String[] temp = information.split("\\|");
        this.time = temp[0];
        this.distance = Integer.parseInt(temp[1]);
        if (distance == 0) {
            this.btaddress = temp[2];
            this.nearestaddress = wifiAddress;
        } else {
            this.btaddress = null;
            this.nearestaddress = temp[2];
        }
    }


    public String toString() {
        return wifiAddress;
    }

}
