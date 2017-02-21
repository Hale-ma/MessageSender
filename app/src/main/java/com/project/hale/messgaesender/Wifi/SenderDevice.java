package com.project.hale.messgaesender.Wifi;

/**
 * Created by mahon on 2016/11/13.
 */

public class SenderDevice {
    public String wifiAddress, btaddress, nearestaddress;
    public int distance = 0;
    public String time = "";

    public SenderDevice(String wifiAddress, String nearestaddress, String btaddress, int distance, String time) {
        this.wifiAddress = wifiAddress;
        this.distance = distance;
        this.nearestaddress = nearestaddress;
        this.time = time;
        this.btaddress = btaddress;
    }

    public SenderDevice(String mac, String information) {
        this.wifiAddress = mac;
        String[] temp = information.split("\\|");
        this.time = temp[0];
        this.btaddress=temp[1];
        this.nearestaddress = temp[2];
        this.distance = Integer.parseInt(temp[3]);
    }


    public String toString() {
        return wifiAddress;
    }

    public String getdetail() {
        //      [0]          [1]                 [2]                  [3]
        return time + "|" + btaddress + "|" + nearestaddress + "|" + distance;
    }


}
