package com.project.hale.messgaesender;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutServiceData;
import com.project.hale.messgaesender.Wifi.SenderDevice;
import com.project.hale.messgaesender.Wifi.SenderWifiManager;
import com.project.hale.messgaesender.Wifi.WifiBoardCastManager;

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
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", device.salutDevice.readableName);
        intent.putExtras(bundle);
        SenderWifiManager.getInstance().nowdevice=device.salutDevice;
        startActivity(intent);

    }

    private void initwifi() {
        DeviceListFragment dfra = (DeviceListFragment) getSupportFragmentManager().findFragmentById(R.id.frag_list);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wm.init(mManager, mChannel, dfra);
    }

    private void initSalut() {
        SalutDataReceiver dataReceiver = new SalutDataReceiver(this, SenderWifiManager.getInstance());
        SalutServiceData sd = new SalutServiceData("sas", 52391, "xiaolan");
        SalutCallback sc = new SalutCallback() {
            @Override
            public void call() {
                Log.e("Salut", "not support wifi direct");
            }
        };
        snetwork = new Salut(dataReceiver, sd, sc);
        SenderWifiManager.getInstance().init(snetwork,dfra);


        //


    }


    @Override
    public void onDataReceived(Object o) {
        Log.d("Salut - on DataReceived", o.toString());
    }


}
