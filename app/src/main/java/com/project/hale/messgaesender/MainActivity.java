package com.project.hale.messgaesender;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;
import com.project.hale.messgaesender.Wifi.SenderDevice;
import com.project.hale.messgaesender.Wifi.WifiBoardCastManager;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnFragmentInteractionListener, SalutDataCallback {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiBoardCastManager wm = WifiBoardCastManager.getsInstance();
    Salut snetwork;
    DeviceListFragment dfra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dfra = (DeviceListFragment) getSupportFragmentManager().findFragmentById(R.id.frag_list);
        initSalut();
        //  initwifi();


    }

    @Override
    public void onFragmentInteraction(SenderDevice device) {
        Log.d("Sender GUI", "onclick - " + device.salutDevice.readableName);
//        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putString("mac", device.deviceAddress);
//        intent.putExtras(bundle);
//        startActivity(intent);
//        snetwork.registerWithHost();

    }

    private void initwifi() {
        DeviceListFragment dfra = (DeviceListFragment) getSupportFragmentManager().findFragmentById(R.id.frag_list);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wm.init(mManager, mChannel, dfra);
    }

    private void initSalut() {
        SalutDataReceiver dataReceiver = new SalutDataReceiver(this, this);
        SalutServiceData sd = new SalutServiceData("sas", 52391, "2232");
        SalutCallback sc = new SalutCallback() {
            @Override
            public void call() {
                Log.e("Salut", "not support wifi direct");
            }
        };
        snetwork = new Salut(dataReceiver, sd, sc);
        snetwork.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {
                Log.d("233", salutDevice.readableName + "has connected");
            }
        });

        //

        snetwork.discoverNetworkServices(new SalutCallback() {
            @Override
            public void call() {
                Log.d("Salut", "Look at all these devices! " + snetwork.foundDevices.toString());
                Iterator<SalutDevice> it=snetwork.foundDevices.iterator();
                while(it.hasNext()) {
                    dfra.addDevice(it.next());
                }
            }
        }, true);
    }


    @Override
    public void onDataReceived(Object o) {
        Log.d("Salut - on DataReceived", o.toString());
    }
}
