package com.project.hale.messgaesender.Wifi;

import android.util.Log;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDevice;
import com.project.hale.messgaesender.DeviceListFragment;

import java.util.Iterator;

/**
 * Created by mahon on 2016/12/20.
 */

public class SenderWifiManager implements SalutDataCallback {
    private static SenderWifiManager sInstance = new SenderWifiManager();
    public Salut snetwork;
    public SalutDevice nowdevice;
    DeviceListFragment dfra = null;

    private SenderWifiManager() {

    }

    public static SenderWifiManager getInstance() {
        return sInstance;
    }

    public void init(Salut s, DeviceListFragment dlf) {
        this.snetwork = s;
        this.dfra = dlf;
        snetwork.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {
                Log.d("Salut", salutDevice.readableName + "has connected");
            }
        });
        snetwork.discoverNetworkServices(new SalutCallback() {
            @Override
            public void call() {
                Log.d("Salut", "Look at all these devices! " + snetwork.foundDevices.toString());
                Iterator<SalutDevice> it = snetwork.foundDevices.iterator();
                while (it.hasNext()) {
                    dfra.addDevice(it.next());
                }
            }
        }, true);

    }

    @Override
    public void onDataReceived(Object o) {
        Log.d("Salut - on DataReceived", o.toString());
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

    public void sendmsg(String msg){

    }
}
