package com.project.hale.messgaesender.Wifi;

import android.os.Handler;
import android.util.Log;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;
import com.project.hale.messgaesender.DeviceListFragment;
import com.project.hale.messgaesender.MainActivity;

import java.util.Iterator;
import java.util.ServiceConfigurationError;

/**
 * Created by mahon on 2016/12/20.
 */

public class SenderWifiManager implements SalutDataCallback {
    private static SenderWifiManager sInstance = new SenderWifiManager();
    public Salut snetwork;
    public SalutDevice nowdevice;
    private SalutDataReceiver sdr;
    DeviceListFragment dfra = null;

    public boolean isInit = false;
    private boolean isDiscovering = false;
    private int count=0;

    private Handler d_handler = new Handler();
    private final int SERVICE_DISCOVERY_INTERVAL = 8000;

    private SenderWifiManager() {

    }

    public static SenderWifiManager getInstance() {
        return sInstance;
    }

    public void init(SalutDataReceiver sdr, Salut s, DeviceListFragment dlf) {
        this.sdr = sdr;
        this.snetwork = s;
        this.dfra = dlf;
        snetwork.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {
                Log.d("Salut", salutDevice.readableName + "has connected");
            }
        });//althou
        d_handler.postDelayed(mServiceDiscoveringRunnable, SERVICE_DISCOVERY_INTERVAL);

    }


//    public boolean sendto(Message ms) {
//        final String sdtail = nowdevice.toString();
//        final Message msg = ms;
//        snetwork.registerWithHost(nowdevice, new SalutCallback() {
//            @Override
//            public void call() {
//                Log.d("Salut", "we are connected:" + sdtail);
//                snetwork.sendToHost(msg, new SalutCallback() {
//                    @Override
//                    public void call() {
//                        Log.d("Salut", "failed to send!");
//                    }
//                });
//            }
//        }, new SalutCallback() {
//            @Override
//            public void call() {
//                Log.d("Salut", "failed to connect");
//            }
//        });
//
//
//        return true;
//    }

    public void discover() {
        Log.d("Salut", "discovering..." + isDiscovering);
        if (!isDiscovering) {
            isDiscovering = true;
            snetwork.discoverNetworkServices(new SalutCallback() {
                @Override
                public void call() {
                    Log.d("Salut", "Look at all these devices! " + snetwork.foundDevices.toString());
                    Log.d("Salut", "Raw data: " + snetwork.rawData.toString());
                    Iterator<SalutDevice> it = snetwork.foundDevices.iterator();
                    while (it.hasNext()) {
                        dfra.addDevice(it.next());
                    }
                    isDiscovering = false;
                }
            }, true);
        }else {
            count++;
            if(count>3){
                snetwork.stopServiceDiscovery(false);
                count=0;
                isDiscovering=false;
            }
        }
    }

    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {
            discover();
            d_handler.postDelayed(mServiceDiscoveringRunnable, SERVICE_DISCOVERY_INTERVAL);
        }
    };

    public void sendmsg(String msg) {
        snetwork.stopNetworkService(false);
        SalutServiceData sd = new SalutServiceData("new", 52391, msg);
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


}
