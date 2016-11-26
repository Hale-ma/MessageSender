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
import com.project.hale.messgaesender.Wifi.WifiBoardCastManager;

public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnFragmentInteractionListener, SalutDataCallback {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiBoardCastManager wm = WifiBoardCastManager.getsInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initwifi();


    }

    @Override
    public void onFragmentInteraction(SenderDevice device) {
        Log.d("Sender GUI", "onclick - " + device.deviceAddress);
        Intent intent= new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra("mac",device.deviceAddress);
        startActivity(intent);



    }

    private void initwifi() {
        DeviceListFragment dfra = (DeviceListFragment) getSupportFragmentManager().findFragmentById(R.id.frag_list);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wm.init(mManager, mChannel, dfra);
    }


    @Override
    public void onDataReceived(Object o) {
        Log.d("wifi connection", o.toString());
    }
}
