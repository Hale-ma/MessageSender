package com.project.hale.messgaesender.Wifi;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.project.hale.messgaesender.Bluetooth.SenderBluetoothManager;
import com.project.hale.messgaesender.DeviceListFragment;

import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.project.hale.messgaesender.Wifi.SenderCore.finishDeviceUpdate;
import static com.project.hale.messgaesender.Wifi.SenderCore.startupdateDeviceInformation;

/**
 * Created by mahon on 2016/12/20.
 */

public class SenderWifiManager implements SalutDataCallback {
    private static SenderWifiManager sInstance = new SenderWifiManager();
    public Salut snetwork;
    private SalutDataReceiver sdr;
    public static String MacAddr;
    private SQLiteDatabase mainDB;
    private SharedPreferences preferences;
    public boolean isInit = false;
    private boolean isDiscovering = false;

    private Context context;

    private SalutServiceData cachedata = new SalutServiceData("loc|all|" + getTime(), 52391, "x", SenderBluetoothManager.getInstance().getbtMAC());
    private WifiStatus nowstatus = WifiStatus.DEFAULT;
    private String cache_tar, cache_msg, cache_tar_ex, cache_msg_ex;


    private Handler d_handler = new Handler();
    private Handler status_handler;

    private int SELF_CHECK_INTERVAL = 20000;
    private int WIFI_ENABLE_INTERVAL = 3000;
    private int WIFI_DISABLE_INTERVAL = 1500;

    private SenderWifiManager() {

    }

    public static synchronized SenderWifiManager getInstance() {
        return sInstance;
    }

    public void init(SalutDataReceiver sdr, Salut s, Context context, SharedPreferences preferences) {
        this.sdr = sdr;
        this.snetwork = s;
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
                sd = new SalutServiceData(cache_tar, 52391, cache_msg, SenderBluetoothManager.getInstance().getbtMAC());
                nowstatus = WifiStatus.OSEND;
            } else {
                cache_tar = tar;
                cache_msg = msg;
                sd = new SalutServiceData(cache_tar, 52391, cache_msg, SenderBluetoothManager.getInstance().getbtMAC());
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
                sd = new SalutServiceData(getMacAddr() + "|" + tar + "|" + getTime(), 52391, msg, SenderBluetoothManager.getInstance().getbtMAC());
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
                sd = new SalutServiceData(cache_tar_ex, 52391, cache_msg_ex, SenderBluetoothManager.getInstance().getbtMAC());
                sd.addextra(getMacAddr() + "|" + tar + "|" + getTime(), msg);
                cache_tar = cache_tar_ex;
                cache_msg = cache_msg_ex;
                cache_tar_ex = getMacAddr() + "|" + tar + "|" + getTime();
                cache_msg_ex = msg;
                nowstatus = WifiStatus.DSEND;
            } else {
                sd = new SalutServiceData(cache_tar_ex, 52391, cache_msg_ex, SenderBluetoothManager.getInstance().getbtMAC());
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
                sd = new SalutServiceData(cache_tar, 52391, cache_msg, SenderBluetoothManager.getInstance().getbtMAC());
                sd.addextra(cache_tar_ex, cache_msg_ex);
                nowstatus = WifiStatus.DOUBLE;
            } else {
                sd = new SalutServiceData(cache_tar_ex, 52391, cache_msg_ex, SenderBluetoothManager.getInstance().getbtMAC());
                sd.addextra(tar, msg);
                cache_tar = cache_tar_ex;
                cache_msg = cache_msg_ex;
                cache_tar_ex = tar;
                cache_msg_ex = msg;
                nowstatus = WifiStatus.DFORD;
            }
        } else {//DOUBLE
            if (action == 0) {
                sd = new SalutServiceData(getMacAddr() + "|" + tar + "|" + getTime(), 52391, msg, SenderBluetoothManager.getInstance().getbtMAC());
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


    private void praseData() {
        startupdateDeviceInformation();
        Iterator<String> it = snetwork.rawData.iterator();
        while (it.hasNext()) {
            String raw = it.next();
            Log.d("Salut", "splited Raw data: " + raw);
            //load devices into content provider
            String[] splited = raw.split("\\|");
            SenderCore.updateDeviceInformation(splited[0], splited[5], 1, splited[0]);
            if (splited[2].compareTo("all") != 0) {
                SenderCore.updateDeviceInformation(splited[1], "UNKNOWN", 2, splited[0]);
            }

            if (splited[2].compareTo("all") == 0) {
                Log.d("Salut", "prase Data: I recieved all from " + splited[0] + " when " + splited[3]);
            } else {
                SenderCore.onReceive(splited[1], splited[2], splited[3], splited[4]);
            }
            snetwork.rawData = new ArrayList<String>();
        }
        finishDeviceUpdate();
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


    public void setStatus_handler(Handler status_handler) {
        this.status_handler = status_handler;
    }
}
