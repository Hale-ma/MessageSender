package com.project.hale.messgaesender.Wifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;
import com.project.hale.messgaesender.DeviceListFragment;

import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by mahon on 2016/12/20.
 */

public class SenderWifiManager implements SalutDataCallback {
    private static SenderWifiManager sInstance = new SenderWifiManager();
    public Salut snetwork;
    public List<SenderDevice> deviceList = new ArrayList<>();
    private SalutDataReceiver sdr;
    DeviceListFragment dfra = null;
    public static String MacAddr;
    private SQLiteDatabase mainDB;
    private SharedPreferences preferences;
    public boolean isInit = false;
    private boolean isDiscovering = false;
    private int count = 0;
    private SalutServiceData cachedata = new SalutServiceData("loc|all|" + getTime(), 52391, "x");;
    private Context context;

    private Handler d_handler = new Handler();
    private Handler msg_handler;
    private final int SERVICE_DISCOVERY_INTERVAL = 8000;
    private final int RETRY_INTERVAL = 3;

    private SenderWifiManager() {

    }

    public static synchronized SenderWifiManager getInstance() {
        return sInstance;
    }

    public void init(SalutDataReceiver sdr, Salut s, DeviceListFragment dlf, Context context, SharedPreferences preferences) {
        this.sdr = sdr;
        this.snetwork = s;
        this.dfra = dlf;
        this.mainDB = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().getAbsolutePath().replace("files", "databases") + "sendermsg.db", null);
        this.context = context;
        this.preferences = preferences;
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS msg(sor char(64),tar char(64),time char(64),msg char(255))");
        snetwork.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {
                Log.d("Salut", salutDevice.readableName + "has connected");
            }
        });//althou
        d_handler.postDelayed(mServiceDiscoveringRunnable, SERVICE_DISCOVERY_INTERVAL);

    }


    public void discover() {
        Log.d("Salut", "discovering..." + isDiscovering);
        if (!isDiscovering) {
            isDiscovering = true;
            snetwork.discoverNetworkServices(new SalutCallback() {
                @Override
                public void call() {
                    praseData();
                    isDiscovering = false;
                }
            }, true);
        } else {
            count++;
            if (count > RETRY_INTERVAL) {
                praseData();
                snetwork.stopServiceDiscovery(true);
                Salut.disableWiFi(context);
                count = 0;
                isDiscovering = false;
            }
        }
    }

    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {
            if (!Salut.isWiFiEnabled(context)) {
                Salut.enableWiFi(context);
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SalutCallback sc = new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e("Salut", "not support wifi direct");
                    }
                };
                snetwork = new Salut(sdr, cachedata, sc);
                snetwork.startNetworkService(new SalutDeviceCallback() {
                    @Override
                    public void call(SalutDevice salutDevice) {
                        Log.d("Salut", salutDevice.readableName + "has connected");
                    }
                });
                discover();
                d_handler.postDelayed(mServiceDiscoveringRunnable, SERVICE_DISCOVERY_INTERVAL);
            } else {
                discover();
                d_handler.postDelayed(mServiceDiscoveringRunnable, SERVICE_DISCOVERY_INTERVAL);
            }
        }
    };

    public void sendmsg(String tar, String msg) {
        snetwork.stopNetworkService(false);
        SalutServiceData sd = new SalutServiceData(getMacAddr() + "|" + tar + "|" + getTime(), 52391, msg);
        cachedata = sd;
        SalutCallback sc = new SalutCallback() {
            @Override
            public void call() {
                Log.e("Salut", "not support wifi direct");
            }
        };
        snetwork = new Salut(sdr, sd, sc);
        snetwork.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {
                Log.d("Salut", salutDevice.readableName + "has connected");
            }
        });
    }

    @Override
    public void onDataReceived(Object o) {
        Log.d("Salut - on DataReceived", o.toString());
    }

    public List<SenderDevice> getDeviceList() {
        return deviceList;
    }

    private void praseData() {
        SharedPreferences.Editor editor = preferences.edit();
        Iterator<String> it = snetwork.rawData.iterator();
        while (it.hasNext()) {
            String raw = it.next();
            Log.d("Salut", "splited Raw data: " + raw);
            String[] splited = raw.split("\\|");
            editor.putString(splited[0], getTime());
            //Log.d("Salut", "["+splited[2] + "][" + getMacAddr() + "]cp:" + getMacAddr().compareTo(splited[2]));
            if (splited[2].compareTo("all") == 0) {
                Log.d("Salut", "prase Data: I recieved all from " + splited[0] + " when " + splited[3]);
            } else {
                Cursor c = mainDB.rawQuery("SELECT * FROM msg WHERE sor='" + splited[1] + "' and tar='" + splited[2] + "' and time='" + splited[3] + "' and msg ='" + splited[4] + "'", null);
                if (c.getCount() != 0) {
                    Log.d("db", "duplicate" + splited[4]);
                } else {
                    mainDB.execSQL("INSERT INTO msg('sor','tar','time','msg')values('" + splited[1] + "','" + splited[2] + "','" + splited[3] + "','" + splited[4] + "')");
                    if (msg_handler != null) {
                        msg_handler.handleMessage(new Message());
                    }
                    if (splited[2].compareTo(getMacAddr()) == 0) {//i am the target!
                        Log.d("Salut", "prase Data: I recieved:" + splited[4] + "from " + splited[0] + " when " + splited[3]);
                    } else {//i am not the target
                        Log.d("Salut", "prase Data: I need to route the messgae:" + splited[4] + "from " + splited[0] + " when " + splited[3]);
                    }
                }
            }

            snetwork.rawData = new ArrayList<String>();
        }
        editor.commit();


        Map<String, ?> usr = preferences.getAll();
        deviceList = new ArrayList<SenderDevice>();
        Iterator<String> iter = usr.keySet().iterator();
        while (iter.hasNext()) {
            String mac = iter.next();
            String time = (String) usr.get(mac);
            deviceList.add(new SenderDevice(mac, 0, time));
        }
        if (dfra != null) {
            dfra.updateUI();
        }


    }

    @NonNull
    public static String getMacAddr() {
        if (MacAddr != null) {
            return MacAddr;
        }
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            boolean first = true;
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    //  res1.append(Integer.toHexString(b & 0xFF).compareTo("0") == 0 ? "00:" : Integer.toHexString(b & 0xFF) + ":");//TODO fix bug here
                    if ((b & 0xFF) < 10) {
                        if (first) {
                            res1.append("0" + Integer.toHexString((b & 0xFF) + 2) + ":");
                        } else {
                            res1.append("0" + Integer.toHexString(b & 0xFF) + ":");
                        }
                    } else {
                        if (first) {
                            res1.append(Integer.toHexString((b & 0xFF) + 2) + ":");
                        } else {
                            res1.append(Integer.toHexString(b & 0xFF) + ":");
                        }
                    }
                    first = false;
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                MacAddr = res1.toString();
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    public static String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    public void endservice() {
        snetwork.stopServiceDiscovery(false);
        snetwork.stopNetworkService(false);
        mainDB.close();
    }

    public void setMsg_handler(Handler msg_handler) {
        this.msg_handler = msg_handler;
    }
}
