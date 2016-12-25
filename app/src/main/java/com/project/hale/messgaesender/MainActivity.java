package com.project.hale.messgaesender;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnFragmentInteractionListener, SalutDataCallback {
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
        Log.d("Sender GUI", "onclick - " + device.wifiAddress);
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("MAC", device.wifiAddress);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    private void initSalut() {
        if(!SenderWifiManager.getInstance().isInit) {
            SalutDataReceiver dataReceiver = new SalutDataReceiver(this, SenderWifiManager.getInstance());
            SalutServiceData sd = new SalutServiceData("all", 52391, "xiaolan");
            SalutCallback sc = new SalutCallback() {
                @Override
                public void call() {
                    Log.e("Salut", "not support wifi direct");
                }
            };
            snetwork = new Salut(dataReceiver, sd, sc);
            SenderWifiManager.getInstance().init(dataReceiver, snetwork, dfra);
            SenderWifiManager.getInstance().isInit=true;
        }
        else{
            Log.d("Salut - me","no need to init");
        }
    }


    @Override
    public void onDataReceived(Object o) {
        Log.d("Salut - on DataReceived", o.toString());
    }


}
