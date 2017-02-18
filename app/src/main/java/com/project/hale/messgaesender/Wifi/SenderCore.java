package com.project.hale.messgaesender.Wifi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.project.hale.messgaesender.Bluetooth.SenderBluetoothManager;

import java.util.HashMap;


public class SenderCore {
    private static SQLiteDatabase mainDB;
    //Hash map store the
    public static String[] paired_device;
    public static HashMap<String, SenderDevice> wbMap = new HashMap<>();
    private static Handler msg_handler;

    public static void init(Context context) {
        mainDB = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().getAbsolutePath().replace("files", "databases") + "sendermsg.db", null);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS msg(sor char(64),tar char(64),time char(64),msg char(255))");
    }


    /**
     * In this application, wifi MAC is the unique identifier
     *
     * @param tarWiFi
     * @param data
     */
    public static void send(String sorWiFi, String tarWiFi, String data) {
        Log.d("SenderCore", sorWiFi + "=>" + tarWiFi + " : " + data);
        SenderDevice sd = wbMap.get(tarWiFi);
        Log.d("SenderCore", sd + " " + sd.btaddress);
        if (sd.btaddress.compareTo("UNKNOWN") != 0) { //know the bt address
            for (int i = 0; i < paired_device.length; i++) {
                if (paired_device[i].compareTo(sd.btaddress) == 0) { //it is also paired

                    SenderBluetoothManager.getInstance().send(sd.btaddress, sorWiFi, tarWiFi, data);// try to send with bluetooth without considering the distance

                }
            }
        } else {//it is impossible to send it direct with bluetooth
            if (!send_by_BT_neighbour(sorWiFi, tarWiFi, data)) {  //try to send by the nearest bt neighbourhood,if it failed to send by by, then send by wifi
                send_by_Wifi(sorWiFi, tarWiFi, data);
            }
        }
    }

    public static boolean send_by_BT_neighbour(String sorWiFi, String tarWiFi, String data) {
        SenderDevice sd = wbMap.get(wbMap.get(tarWiFi).nearestaddress);
        if (sd.btaddress.compareTo("UNKNOWN") != 0) { //know the bt address
            for (int i = 0; i < paired_device.length; i++) {
                if (paired_device[i].compareTo(sd.btaddress) == 0) { //it is also paired
                    SenderBluetoothManager.getInstance().send(sd.btaddress, sorWiFi, tarWiFi, data);
                    return true;
                }
            }
        }
        return false;
    }

    public static void onReceive(String sorWiFi, String tarWiFi, String time, String data) {
        Cursor c = mainDB.rawQuery("SELECT * FROM msg WHERE sor='" + sorWiFi + "' and tar='" + tarWiFi + "' and time='" + time + "' and msg ='" + data + "'", null);
        if (c.getCount() != 0) {
            Log.d("db", "duplicate" + data);
        } else {
            //add the messge into database
            mainDB.execSQL("INSERT INTO msg('sor','tar','time','msg')values('" + sorWiFi + "','" + tarWiFi + "','" + time + "','" + data + "')");
            if (tarWiFi.compareTo(SenderWifiManager.getInstance().getMacAddr()) == 0) {//i am the target!
                Log.d("SenderCore", "prase Data: I recieved:" + data + "from " + sorWiFi + " when " + time);
                msg_handler.handleMessage(new Message());
            } else {//i am not the target
                Log.d("SenderCore", "prase Data: I need to route the messgae:" + data + "from " + sorWiFi + " when " + time);
                if (sorWiFi.compareTo(SenderWifiManager.getInstance().getMacAddr()) != 0) {// do not "route" the message from itself
                    //  sendmsg(sorWiFi + "|" + tarWiFi + "|" + time, data, 1);
                    send(sorWiFi, tarWiFi, data);
                }
            }
        }
    }

    public static void send_by_Wifi(String sorWiFi, String tarWiFi, String data) {
        SenderWifiManager.getInstance().sendmsg(tarWiFi, data, sorWiFi.compareTo(SenderWifiManager.getInstance().getMacAddr()) == 0 ? 0 : 1);
    }

    public static void stop() {
        mainDB.close();
    }

    public static void setMsg_handler(Handler msg_handler) {
        SenderCore.msg_handler = msg_handler;
    }

}
