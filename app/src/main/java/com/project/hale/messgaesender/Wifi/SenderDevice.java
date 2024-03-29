package com.project.hale.messgaesender.Wifi;


/**
 * An device object in this application, it stores all the information for a device
 * It is the basic element that builds the routing table
 */
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

    //construct the object from string, used when load from Sharedperference
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

    //This method is desgined to store this object as a String into Sharedperference
    public String getdetail() {
        //      [0]          [1]                 [2]                  [3]
        return time + "|" + btaddress + "|" + nearestaddress + "|" + distance;
    }


}
