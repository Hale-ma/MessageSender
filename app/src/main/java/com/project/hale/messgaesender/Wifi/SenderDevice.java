package com.project.hale.messgaesender.Wifi;



public class SenderDevice {
    public String wifiAddress, btaddress, nearestaddress;
    public int distance = 0;
    public String time = "";
    public int newMsg=0;

    /**
     * Construct a new SenderDevice
     *
     * @param wifiAddress
     * @param nearestaddress nearest node wifi address
     * @param btaddress its BT address ,can be "UNKNOWN"
     * @param distance 1= can connect directly
     * @param time the last time find it avaliable
     */
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
