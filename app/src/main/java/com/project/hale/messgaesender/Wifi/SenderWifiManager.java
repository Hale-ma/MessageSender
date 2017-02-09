package com.project.hale.messgaesender.Wifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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


    private SalutServiceData cachedata = new SalutServiceData("loc|all|" + getTime(), 52391, "x");
    private WifiStatus nowstatus = WifiStatus.DEFAULT;
    private String cache_tar, cache_msg, cache_tar_ex, cache_msg_ex;

    private int testcount = 0;
    private Context context;
    private Date sendtime;
    private long avgdelay = 0;


    private Handler d_handler = new Handler();
    private Handler msg_handler, status_handler;

    private int SELF_CHECK_INTERVAL = 20000;
    private int WIFI_ENABLE_INTERVAL = 3000;
    private int WIFI_DISABLE_INTERVAL = 1500;

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
        SELF_CHECK_INTERVAL = preferences.getInt("checkinterval", 20000);
        WIFI_ENABLE_INTERVAL = preferences.getInt("enable", 3000);
        WIFI_DISABLE_INTERVAL = preferences.getInt("disable", 1500);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS msg(sor char(64),tar char(64),time char(64),msg char(255))");
        snetwork.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {
                Log.d("Salut", salutDevice.readableName + "has connected");
            }
        });//althou
        discover();
        d_handler.postDelayed(mServiceDiscoveringRunnable, SELF_CHECK_INTERVAL);

    }


    public void discover() {
        Log.d("Salut", "discovering..." + isDiscovering);
        isDiscovering = true;
        snetwork.discoverNetworkServices(new SalutCallback() {
            @Override
            public void call() {
                praseData();
                Log.d("Salut", "Update timer.." + isDiscovering);
                d_handler.removeCallbacks(mServiceDiscoveringRunnable);
                d_handler.postDelayed(mServiceDiscoveringRunnable, SELF_CHECK_INTERVAL);
            }
        }, true);

    }

    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {

            Log.d("Salut", "restarting...");
            praseData();
            snetwork.stopServiceDiscovery(true);
            Salut.disableWiFi(context);
            try {
                Log.d("Salut", "disableWiFi...");
                Thread.sleep(WIFI_DISABLE_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("Salut", "enableWiFi...");
            Salut.enableWiFi(context);
            try {
                Thread.sleep(WIFI_ENABLE_INTERVAL);
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
            d_handler.postDelayed(mServiceDiscoveringRunnable, SELF_CHECK_INTERVAL);
        }

    };

    public void sendmsg(String tar, String msg, int action) {//action: 0 = message from myself 1 = forwarding message tar:when sending ,only put MAC address, when forwarding, put full tar
        WifiStatus before = nowstatus;
        snetwork.stopNetworkService(false);
        SalutServiceData sd = null;
        if (nowstatus == WifiStatus.DEFAULT) {
            if (action == 0) {
                cache_tar = getMacAddr() + "|" + tar + "|" + getTime();
                cache_msg = msg;
                sd = new SalutServiceData(cache_tar, 52391, cache_msg);
                nowstatus = WifiStatus.OSEND;
            } else {
                cache_tar = tar;
                cache_msg = msg;
                sd = new SalutServiceData(cache_tar, 52391, cache_msg);
                nowstatus = WifiStatus.OFORD;
            }
        } else if (nowstatus == WifiStatus.OSEND) {

            if (action == 0) {
                cache_tar_ex = getMacAddr() + "|" + tar + "|" + getTime();
                cache_msg_ex = msg;
                sd = cachedata;
                sd.addextra(cache_tar_ex, cache_msg_ex);
                nowstatus = WifiStatus.DSEND;
            } else {
                cache_tar_ex = tar;
                cache_msg_ex = msg;
                sd = cachedata;
                sd.addextra(cache_tar_ex, cache_msg_ex);
                nowstatus = WifiStatus.DOUBLE;
            }
        } else if (nowstatus == WifiStatus.OFORD) {
            if (action == 0) {
                sd = new SalutServiceData(getMacAddr() + "|" + tar + "|" + getTime(), 52391, msg);
                sd.addextra(cache_tar, cache_msg);
                cache_tar_ex = cache_tar;
                cache_msg_ex = cache_msg;
                cache_tar = getMacAddr() + "|" + tar + "|" + getTime();
                cache_msg = msg;
                nowstatus = WifiStatus.DOUBLE;
            } else {
                sd = cachedata;
                sd.addextra(tar, msg);
                cache_tar_ex = tar;
                cache_msg_ex = msg;
                nowstatus = WifiStatus.DFORD;
            }
        } else if (nowstatus == WifiStatus.DSEND) {
            if (action == 0) {
                sd = new SalutServiceData(cache_tar_ex, 52391, cache_msg_ex);
                sd.addextra(getMacAddr() + "|" + tar + "|" + getTime(), msg);
                cache_tar = cache_tar_ex;
                cache_msg = cache_msg_ex;
                cache_tar_ex = getMacAddr() + "|" + tar + "|" + getTime();
                cache_msg_ex = msg;
                nowstatus = WifiStatus.DSEND;
            } else {
                sd = new SalutServiceData(cache_tar_ex, 52391, cache_msg_ex);
                sd.addextra(tar, msg);
                cache_tar = cache_tar_ex;
                cache_msg = cache_msg_ex;
                cache_tar_ex = tar;
                cache_msg_ex = msg;
                nowstatus = WifiStatus.DOUBLE;
            }
        } else if (nowstatus == WifiStatus.DFORD) {
            if (action == 0) {
                cache_tar = getMacAddr() + "|" + tar + "|" + getTime();
                cache_msg = msg;
                sd = new SalutServiceData(cache_tar, 52391, cache_msg);
                sd.addextra(cache_tar_ex, cache_msg_ex);
                nowstatus = WifiStatus.DOUBLE;
            } else {
                sd = new SalutServiceData(cache_tar_ex, 52391, cache_msg_ex);
                sd.addextra(tar, msg);
                cache_tar = cache_tar_ex;
                cache_msg = cache_msg_ex;
                cache_tar_ex = tar;
                cache_msg_ex = msg;
                nowstatus = WifiStatus.DFORD;
            }
        } else {//DOUBLE
            if (action == 0) {
                sd = new SalutServiceData(getMacAddr() + "|" + tar + "|" + getTime(), 52391, msg);
                cache_tar = getMacAddr() + "|" + tar + "|" + getTime();
                cache_msg = msg;
                sd.addextra(cache_tar_ex, cache_msg_ex);
            } else {
                sd = cachedata;
                sd.addextra(tar, msg);
                cache_tar_ex = tar;
                cache_msg_ex = msg;
            }

        }
        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", nowstatus + "");

        Message message = new Message();
        message.setData(messageBundle);
        if (status_handler != null) {
            status_handler.sendMessage(message);
        }
        Log.d("Salut", before + " => " + nowstatus);

        cachedata = sd;//
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

    public void sendmsg(String tar, String msg) {
        this.sendmsg(tar, msg, 0);
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
            if (splited[2].compareTo("all") == 0) {
                Log.d("Salut", "prase Data: I recieved all from " + splited[0] + " when " + splited[3]);
            } else {
                Cursor c = mainDB.rawQuery("SELECT * FROM msg WHERE sor='" + splited[1] + "' and tar='" + splited[2] + "' and time='" + splited[3] + "' and msg ='" + splited[4] + "'", null);
                if (c.getCount() != 0) {
                    Log.d("db", "duplicate" + splited[4]);
                } else {
                    //add the messge into database
                    mainDB.execSQL("INSERT INTO msg('sor','tar','time','msg')values('" + splited[1] + "','" + splited[2] + "','" + splited[3] + "','" + splited[4] + "')");
                    if (msg_handler != null) {
                        msg_handler.handleMessage(new Message());
                    }
                    if (splited[2].compareTo(getMacAddr()) == 0) {//i am the target!
                        Log.d("Salut", "prase Data: I recieved:" + splited[4] + "from " + splited[0] + " when " + splited[3]);
                        String[] temp = splited[4].split("XX");
                        int hc = Integer.valueOf(temp[0]);
                        long delay = 0;
                        if (hc > testcount) {
                            if (sendtime != null) {
                                Date now = new Date();
                                delay = (now.getTime() - sendtime.getTime()) / 2000;
                                if (avgdelay != 0) {
                                    avgdelay = (avgdelay + delay) / 2;
                                } else {
                                    avgdelay = delay;
                                }
                            }
                            sendtime = new Date();
                            testcount = hc + 1;

                            sendmsg(splited[0], testcount + "XX" + "Delay:" + delay + "s" + "average: " + avgdelay + "s", 0);
                        }


                    } else {//i am not the target
                        Log.d("Salut", "prase Data: I need to route the messgae:" + splited[4] + "from " + splited[0] + " when " + splited[3]);
                        if (splited[1].compareTo(getMacAddr()) != 0) {// do not "route" the message from itself
                            sendmsg(splited[1] + "|" + splited[2] + "|" + splited[3], splited[4], 1);
                        }
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

    public void setStatus_handler(Handler status_handler) {
        this.status_handler = status_handler;
    }
}
