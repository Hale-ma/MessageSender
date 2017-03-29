package com.project.hale.messgaesender;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.project.hale.messgaesender.Bluetooth.SenderBluetoothManager;
import com.project.hale.messgaesender.Wifi.SenderDevice;
import com.project.hale.messgaesender.Wifi.SenderWifiManager;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.project.hale.messgaesender.SenderCore.connectionType.DIRECT;
import static com.project.hale.messgaesender.SenderCore.connectionType.NEIGHBOUR;

public class SenderCore {
    private static SenderCore sInstance = new SenderCore();

    private SQLiteDatabase mainDB;
    //Hash map store the
    public static String[] paired_device;
    public HashMap<String, SenderDevice> wbMap = new HashMap<>();
    private Handler msg_handler, status_handler;
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
        if (sd == null) {
            Log.d("SenderCore", "send_by_BT_neighbour:FAILED:Can't find BT address of " + tarWiFi);
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
                //ping
                String[] sp = data.split(" ");
                if (sp[0].compareTo("-ping") == 0) {
                    if (sp.length <= 1) {
                        send(SenderWifiManager.getMacAddr(), sorWiFi, "ping reply from" + SenderWifiManager.getMacAddr());
                    } else {
                        if (sp[1].compareTo("-t") == 0) {
                            int count = Integer.parseInt(sp[2]) + 1;
                            send(SenderWifiManager.getMacAddr(), sorWiFi, "-ping -t " + count + " messages");
                        } else {
                            send(SenderWifiManager.getMacAddr(), sorWiFi, "ping reply from" + SenderWifiManager.getMacAddr());
                        }
                    }
                }
            } else {//i am not the target
                Log.d("SenderCore", "prase Data: I need to route the messgae:" + data + "from " + sorWiFi + " when " + time);
                if (sorWiFi.compareTo(SenderWifiManager.getInstance().getMacAddr()) != 0) {// do not "route" the message from itself
                    send(sorWiFi, tarWiFi, data);
                }
            }
        }
    }

    public JSONArray neighbour_message(boolean btuse) {
        JSONArray ja = new JSONArray();

        Iterator<Map.Entry<String, SenderDevice>> it = SenderCore.getsInstance().wbMap.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry<String, SenderDevice> e = it.next();
            SenderDevice sd = e.getValue();
            if (sd.btaddress != SenderBluetoothManager.getInstance().connectedMAC) {
                ja.put(sd.wifiAddress + "|" + sd.btaddress + "|" + sd.distance);
            }
            i++;
            if (!btuse && i > 5) {
                break;//wifi boradcast can only send 6 nodes informatoin at a time.
            }
        }
        if (btuse) {
            ja.put(SenderWifiManager.getMacAddr() + "|" + SenderBluetoothManager.getInstance().getbtMAC() + "|0");//itself
        }
        return ja;
    }

    public void onBTfaild() {
        Log.d("SenderCore", "onBTfaild():" + connectionType);
        if (connectionType == DIRECT) {
            if (wbMap.get(nowSending[1]).distance == 1) {
                SenderDevice sd = wbMap.get(nowSending[1]);
                if (sd.nearestaddress.compareTo(sd.wifiAddress) != 0) {
                    SenderDevice nei = wbMap.get(sd.nearestaddress);
                    if (nei != null) {
                        sd.distance = nei.distance + 1;
                        wbMap.put(sd.wifiAddress, sd);
                        startupdateDeviceInformation();
                        editor.putString(sd.wifiAddress, sd.getdetail());
                        finishDeviceUpdate();
                    }

                }

            }
            send_by_BT_neighbour(nowSending[0], nowSending[1], nowSending[2]);
        } else {
            send_by_Wifi(nowSending[0], nowSending[1], nowSending[2]);
            onSuccess();
        }
    }

    public void onSuccess() {
        Log.d("SenderCore", "onSuccess():" + connectionType);
        isSending = false;
        if (connectionType == DIRECT) {//if can send message directly by bluetooth, it means the distance between them is 1.
            SenderDevice sd = wbMap.get(nowSending[1]);
            if (sd.distance != 1) {
                sd.distance = 1;
                sd.nearestaddress = nowSending[1];
                wbMap.put(sd.wifiAddress, sd);
                startupdateDeviceInformation();
                editor.putString(sd.wifiAddress, sd.getdetail());
                finishDeviceUpdate();
            }


        }
        if (!cacheMessage.isEmpty()) {
            String[] temp = cacheMessage.peek();
            send_t(temp[0], temp[1], temp[2]);
        }
        updateMainUI();
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
        mainDB.execSQL("delete from msg where tar!='" + SenderWifiManager.getMacAddr() + "' and sor!='" + SenderWifiManager.getMacAddr() + "'");
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
            if (old.nearestaddress.compareTo(from) != 0 || old.distance < distance + 1) {//oldRecord does not cache nearest neighbourhood
                if (old.distance == 1 && old.nearestaddress.compareTo(old.wifiAddress) == 0) {
                    Log.d("updateNei", "cache nei");
                    SenderDevice sd = new SenderDevice(wifiAddress, from, btAddress, 1, getTime());
                    wbMap.put(wifiAddress, sd);
                    editor.putString(sd.wifiAddress, sd.getdetail());
                }
                return;
            } else {
                Log.d("updateNei", old.distance + " " + (distance + 1));
                SenderDevice sd = new SenderDevice(wifiAddress, from, btAddress, distance == 100 ? 100 : distance + 1, getTime());
                wbMap.put(wifiAddress, sd);
                editor.putString(sd.wifiAddress, sd.getdetail());
            }
        } else {//node does not exist in my routing table
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

    public void setStatus_handler(Handler status_handler) {
        this.status_handler = status_handler;
    }

    public void updateMainUI() {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("status", SenderWifiManager.getInstance().nowstatus + "");
        messageBundle.putString("queue", cacheMessage.size() + "");
        messageBundle.putString("bt", SenderBluetoothManager.getInstance().connectedMAC + "");
        Message message = new Message();
        message.setData(messageBundle);
        if (status_handler != null) {
            status_handler.sendMessage(message);
        }
    }
}
