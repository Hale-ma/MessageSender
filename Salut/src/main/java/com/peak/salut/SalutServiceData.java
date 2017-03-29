package com.peak.salut;

import java.util.HashMap;

public class SalutServiceData {

    protected HashMap<String, String> serviceData;

    public SalutServiceData(String serviceName, int port, String instanceName,String btMAC) {
        serviceData = new HashMap<>();
        serviceData.put("SERVICE_NAME", serviceName);//Store the source address and target address
        serviceData.put("SERVICE_PORT", "" + port);//dummy field
        serviceData.put("INSTANCE_NAME", instanceName);//the data part
        serviceData.put("BT", btMAC);
    }
    //second message
    public void addextra(String addrstring, String content) {
        serviceData.put("SN", addrstring);//Store the source address and target address
        serviceData.put("IN", content);//the data part
    }



}
