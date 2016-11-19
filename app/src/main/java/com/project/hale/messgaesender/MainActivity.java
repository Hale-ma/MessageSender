package com.project.hale.messgaesender;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.project.hale.messgaesender.Wifi.WifiBoardCastManager;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnFragmentInteractionListener {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiBoardCastManager wm=WifiBoardCastManager.getsInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initwifi();
        wm.startRegistration("","");


    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        
    }

    private void initwifi(){
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wm.init(mManager, mChannel);
    }



}
