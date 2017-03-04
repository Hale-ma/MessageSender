package com.project.hale.messgaesender.Wifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.project.hale.messgaesender.Bluetooth.SenderBluetoothManager;
import com.project.hale.messgaesender.DeviceListFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.project.hale.messgaesender.Wifi.SenderCore.connectionType.DIRECT;
import static com.project.hale.messgaesender.Wifi.SenderCore.connectionType.NEIGHBOUR;

public class SenderCore {
    private static SenderCore sInstance = new SenderCore();

    private SQLiteDatabase mainDB;
    //Hash map store the
    public static String[] paired_device;
    public HashMap<String, SenderDevice> wbMap = new HashMap<>();
    private Handler msg_handler;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    public DeviceListFragment dlf;
    public List<SenderDevice> deviceList = new ArrayList<>();//the list just for GUI display

    private Queue<String[]> cacheMessage = new LinkedList<>();
    private boolean isSending = false;
    private String[] nowSending;

    public enum connectionType {
        DIRECT, NEIGHBOUR;
    }

    public connectionType connectionType = DIRECT;

    public void init(Context context, SharedPreferences sharedPreferences, DeviceListFragment dlf) {
        mainDB = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().getAbsolutePath().replace("files", "databases") + "sendermsg.db", null);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS msg(sor char(64),tar char(64),time char(64),msg char(255))");
        this.preferences = sharedPreferences;
        loadPerference();
        this.dlf = dlf;
    }

    private SenderCore() {

    }

    public static SenderCore getsInstance() {
        return sInstance;
    }

    /**
     * adding new message sending request to the system,system will cache it and send them
     *
     * @param sorWiFi
     * @param tarWiFi
     * @param data
     */
    public void send(String sorWiFi, String tarWiFi, String data) {
        Log.d("SenderCore", cacheMessage.size() + "");
        if (cacheMessage.isEmpty() && !isSending) {
            send_t(sorWiFi, tarWiFi, data);
        } else {
            String[] temp = {sorWiFi, tarWiFi, data};
            cacheMessage.add(temp);
        }
    }

    /**
     * In this application, wifi MAC is the unique identifier
     *
     * @param sorWiFi
     * @param tarWiFi
     * @param data
     */
    public void send_t(String sorWiFi, String tarWiFi, String data) {
        isSending = true;
        Log.d("SenderCore", sorWiFi + "=>" + tarWiFi + " : " + data);
        String[] ns = {sorWiFi, tarWiFi, data};
        nowSending = ns;
        SenderDevice sd = wbMap.get(tarWiFi);
        Log.d("SenderCore", "target:" + sd + " " + sd.btaddress);
        if (sd.btaddress.compareTo("UNKNOWN") != 0) { //know the bt address
            for (int i = 0; i < paired_device.length; i++) {
                if (paired_device[i].compareTo(sd.btaddress) == 0) { //it is also paired
                    Log.d("SenderCore", "send_t:" + sorWiFi + "=>" + tarWiFi + ":" + data);
                    connectionType = DIRECT;
                    SenderBluetoothManager.getInstance().send(sd.btaddress, sorWiFi, tarWiFi, data);// try to send with bluetooth without considering the distance
                    return;
                }
            }
            send_by_BT_neighbour(sorWiFi, tarWiFi, data);
        } else {//it is impossible to send it direct with bluetooth
            send_by_BT_neighbour(sorWiFi, tarWiFi, data); //try to send by the nearest bt neighbourhood,if it failed to send by by, then send by wifi

        }
    }

    /**
     * try to send the message to the nearest neighbour, if it faileds ,send it by wifi
     *
     * @param sorWiFi
     * @param tarWiFi
     * @param data
     * @return
     */
    public void send_by_BT_neighbour(String sorWiFi, String tarWiFi, String data) {
        Log.d("SenderCore", wbMap.get(tarWiFi) + " " + wbMap.get(tarWiFi).nearestaddress);
        SenderDevice sd = wbMap.get(wbMap.get(tarWiFi).nearestaddress);
        if(sd==null){
            Log.d("SenderCore","send_by_BT_neighbour:FAILED:Can't find BT address of "+tarWiFi);
            send_by_Wifi(sorWiFi, tarWiFi, data);
            onSuccess();
            return;
        }
        Log.d("SenderCore", "send_by_BT_neighbour:" + sorWiFi + "=>" + tarWiFi + "(" + sd.btaddress + "):" + data);

        if (sd.btaddress.compareTo("UNKNOWN") != 0) { //know the bt address
            for (int i = 0; i < paired_device.length; i++) {
                if (paired_device[i].compareTo(sd.btaddress) == 0) { //it is also paired
                    connectionType = NEIGHBOUR;
                    SenderBluetoothManager.getInstance().send(sd.btaddress, sorWiFi, tarWiFi, data);
                    return;// it is be possible to send the message by BT to its neighbour
                }
            }
            send_by_Wifi(sorWiFi, tarWiFi, data);
            onSuccess();
        } else {
            send_by_Wifi(sorWiFi, tarWiFi, data);
            onSuccess();
        }
    }


    public void onReceive(String sorWiFi, String tarWiFi, String time, String data) {
        Cursor c = mainDB.rawQuery("SELECT * FROM msg WHERE sor='" + sorWiFi + "' and tar='" + tarWiFi + "' and time='" + time + "' and msg ='" + data + "'", null);
        if (c.getCount() != 0) {
            Log.d("db", "duplicate" + data);
        } else {
            //add the messge into database
            mainDB.execSQL("INSERT INTO msg('sor','tar','time','msg')values('" + sorWiFi + "','" + tarWiFi + "','" + time + "','" + data + "')");
            if (tarWiFi.compareTo(SenderWifiManager.getInstance().getMacAddr()) == 0) {//i am the target!
                Log.d("SenderCore", "prase Data: I recieved:" + data + "from " + sorWiFi + " when " + time);
                if (msg_handler != null) {
                    msg_handler.handleMessage(new Message());
                }
                wbMap.get(sorWiFi).newMsg++;
                refeshDeviceList();
            } else {//i am not the target
                Log.d("SenderCore", "prase Data: I need to route the messgae:" + data + "from " + sorWiFi + " when " + time);
                if (sorWiFi.compareTo(SenderWifiManager.getInstance().getMacAddr()) != 0) {// do not "route" the message from itself
                    send(sorWiFi, tarWiFi, data);
                }
            }
        }
    }

    public void onBTfaild() {
        Log.d("SenderCore", "onBTfaild():" + connectionType);
        if (connectionType == DIRECT) {
            send_by_BT_neighbour(nowSending[0], nowSending[1], nowSending[2]);
        } else {
            send_by_Wifi(nowSending[0], nowSending[1], nowSending[2]);
            onSuccess();
        }
    }

    public void onSuccess() {
        Log.d("SenderCore", "onSuccess():" + connectionType);
        isSending = false;
        if (!cacheMessage.isEmpty()) {
            String[] temp = cacheMessage.peek();
            send_t(temp[0], temp[1], temp[2]);
        }
    }

    public void send_by_Wifi(String sorWiFi, String tarWiFi, String data) {
        Log.d("SenderCore", "send_by_Wifi:" + sorWiFi + "=>" + tarWiFi + ":" + data);
        if (sorWiFi.compareTo(SenderWifiManager.getInstance().getMacAddr()) == 0) {
            SenderWifiManager.getInstance().sendmsg(tarWiFi, data, 0);
        } else {
            SenderWifiManager.getInstance().sendmsg(sorWiFi + "|" + tarWiFi + "|" + getTime(), data, 1);
        }
    }

    public void stop() {
        mainDB.close();
    }

    public void setMsg_handler(Handler msg_handler) {
        this.msg_handler = msg_handler;
    }

    public void startupdateDeviceInformation() {
        editor = preferences.edit();
    }

    public void finishDeviceUpdate() {
        editor.commit();
        refeshDeviceList();

    }

    /**
     * @param wifiAddress device wifi mac address
     * @param btAddress   device bt mac address
     * @param distance    the distance between source device  and this device
     * @param from        source wifi mac address
     */
    public void updateDeviceInformation_bySharing(String wifiAddress, String btAddress, int distance, String from) {
        if (wifiAddress.compareTo(SenderWifiManager.getMacAddr()) == 0) {
            return;//do not update itself
        }
        if (wbMap.containsKey(wifiAddress)) {
            SenderDevice old = wbMap.get(wifiAddress);
            if (old.nearestaddress.compareTo(from) != 0 && old.distance < distance) {
                return;
            } else {
                SenderDevice sd = new SenderDevice(wifiAddress, from, btAddress, distance == 100 ? 100 : distance + 1, getTime());
                wbMap.put(wifiAddress, sd);
                editor.putString(sd.wifiAddress, sd.getdetail());
            }
        } else {
            SenderDevice sd = new SenderDevice(wifiAddress, from, btAddress, distance == 100 ? 100 : distance + 1, getTime());
            wbMap.put(wifiAddress, sd);
            editor.putString(sd.wifiAddress, sd.getdetail());
        }

    }

    public void updateDeviceInformation_bymessage(String wifiAddress, String btAddress, String from) {
        //update the neibghour node
        SenderDevice sd = new SenderDevice(wifiAddress, wifiAddress, btAddress, 1, getTime());
        wbMap.put(wifiAddress, sd);
        editor.putString(wifiAddress, sd.getdetail());
        if (wbMap.containsKey(from)) {
            SenderDevice temp = wbMap.get(from);
            if (temp.nearestaddress.compareTo(wifiAddress) != 0 && temp.distance >= 100) {
                SenderDevice sd_from = new SenderDevice(from, wifiAddress, temp.btaddress, 100, getTime());
                wbMap.put(wifiAddress, sd_from);
                editor.putString(sd.wifiAddress, sd.getdetail());
            }

        } else {
            SenderDevice sd_from = new SenderDevice(from, wifiAddress, "UNKNOWN", 100, getTime());
            wbMap.put(wifiAddress, sd_from);
            editor.putString(sd.wifiAddress, sd.getdetail());
        }


    }

    private void refeshDeviceList() {
        deviceList = new ArrayList<>();
        for (SenderDevice senderDevice : wbMap.values()) {
            deviceList.add(senderDevice);
        }
        if (dlf != null) {
            dlf.updateUI();
        }

    }

    private void loadPerference() {
        Map<String, ?> usr = preferences.getAll();
        deviceList = new ArrayList<>();
        Iterator<String> iter = usr.keySet().iterator();
        while (iter.hasNext()) {
            String mac = iter.next();
            String information = ((String) (usr.get(mac)));
            SenderDevice tempdevice = new SenderDevice(mac, information);
            deviceList.add(tempdevice);
            wbMap.put(mac, tempdevice);
        }
    }


    public static String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date());

    }

    public List<SenderDevice> getDeviceList() {
        return deviceList;
    }

    public String getWifiMac(String btMac) {
        Iterator<SenderDevice> it = deviceList.iterator();
        while (it.hasNext()) {
            SenderDevice tempsd = it.next();
            if (tempsd.btaddress.compareTo(btMac) == 0) {
                return tempsd.wifiAddress;
            }
        }
        Log.d("SenderCore", "getWifiMac return X :" + btMac);
        return "UNKNOWN";
    }


}
