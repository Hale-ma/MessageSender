package com.project.hale.messgaesender;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

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
        if (!SenderWifiManager.getInstance().isInit) {
            SalutDataReceiver dataReceiver = new SalutDataReceiver(this, SenderWifiManager.getInstance());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String date = simpleDateFormat.format(new Date());
            SalutServiceData sd = new SalutServiceData("loc|all|" + date, 52391, "x");
            SalutCallback sc = new SalutCallback() {
                @Override
                public void call() {
                    Log.e("Salut", "not support wifi direct");
                }
            };
            snetwork = new Salut(dataReceiver, sd, sc);
            SharedPreferences preferences = getSharedPreferences("user-around", Context.MODE_PRIVATE);
            Map<String, ?> usr = preferences.getAll();
            SenderWifiManager.getInstance().deviceList = new ArrayList<SenderDevice>();
            Iterator<String> iter = usr.keySet().iterator();
            while (iter.hasNext()) {
                String mac = iter.next();
                String time = (String) usr.get(mac);
                SenderWifiManager.getInstance().deviceList.add(new SenderDevice(mac, 0, time));
            }
            dfra.updateUI();


            SQLiteDatabase mainDB = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().getAbsolutePath().replace("files", "databases") + "sendermsg.db", null);
            SenderWifiManager.getInstance().init(dataReceiver, snetwork, dfra, mainDB, preferences);
            SenderWifiManager.getInstance().isInit = true;
        } else {
            Log.d("Salut", "no need to init");
        }
    }


    @Override
    public void onDataReceived(Object o) {
        Log.d("Salut - on DataReceived", o.toString());
    }

    @Override
    protected void onDestroy() {
        SenderWifiManager.getInstance().endservice();
        super.onDestroy();
    }
}
