package com.project.hale.messgaesender;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutServiceData;
import com.project.hale.messgaesender.Bluetooth.SenderBluetoothManager;
import com.project.hale.messgaesender.Wifi.SenderDevice;
import com.project.hale.messgaesender.Wifi.SenderWifiManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The main activity of the application , it will be first load when startup.
 * When loading this activity , it will initiate other part of the application.
 */
public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnFragmentInteractionListener, SalutDataCallback {
    Salut snetwork;
    DeviceListFragment dfra;
    TextView detail_wifi,detail_bt;
    Handler statusUpdateHandler;
    SharedPreferences preferences ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init bluetooth
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 5230);
        }
        initBluetooth();
        setContentView(R.layout.activity_main);

        //init wifi

        initWifi();

        //init core
        detail_wifi = (TextView) findViewById(R.id.my_detail);
        detail_bt = (TextView) findViewById(R.id.my_bt_detail);
        preferences = getSharedPreferences("user-around", Context.MODE_PRIVATE);
        dfra = (DeviceListFragment) getSupportFragmentManager().findFragmentById(R.id.frag_list);
        SenderCore.getsInstance().init(this,preferences,dfra);
        statusUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String status = msg.getData().getString("status");
                String bt = msg.getData().getString("bt");
                String queue = msg.getData().getString("queue");
                detail_wifi.setText("Message in Queue:"+queue+" Wi-Fi:"+status);
                detail_bt.setText("BT connection:"+bt);
            }
        };
        SenderCore.getsInstance().setStatus_handler(statusUpdateHandler);


    }

    @Override
    public void onFragmentInteraction(SenderDevice device) {
        Log.d("Sender GUI", "onclick - " + device.wifiAddress);
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("MAC", device.wifiAddress);
        bundle.putString("BTMAC", device.btaddress);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    private void initWifi() {
        if (!SenderWifiManager.getInstance().isInit) {
            SalutDataReceiver dataReceiver = new SalutDataReceiver(this, SenderWifiManager.getInstance());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String date = simpleDateFormat.format(new Date());
            SalutServiceData sd = new SalutServiceData("loc|all|" + date, 52391, "x", SenderBluetoothManager.getInstance().getbtMAC());// a default broadcast that show the existence of the phone
            SalutCallback sc = new SalutCallback() {
                @Override
                public void call() {
                    Log.e("Salut", "not support wifi direct");
                }
            };
            snetwork = new Salut(dataReceiver, sd, sc);
            SharedPreferences preferences = getSharedPreferences("SenderSettings", Context.MODE_PRIVATE);
            SenderWifiManager.getInstance().init(dataReceiver, snetwork,  this, preferences);
            SenderWifiManager.getInstance().isInit = true;
        } else {
            Log.d("Salut", "no need to init");
        }
    }

    private void initBluetooth() {
        if (!SenderBluetoothManager.getInstance().isInit) {
            SharedPreferences preferences = getSharedPreferences("SenderSettings", Context.MODE_PRIVATE);
            SenderBluetoothManager.getInstance().init(this,preferences);
        }
    }


    @Override
    public void onDataReceived(Object o) {
        Log.d("Salut - on DataReceived", o.toString());
    }

    /**
     * Destroying this activity means the application is exiting , it will stop other module of the application
     */
    @Override
    protected void onDestroy() {
        Log.d("Salut", "Ending");
        SenderWifiManager.getInstance().endservice();
        SenderBluetoothManager.getInstance().endbt();
        SenderCore.getsInstance().stop();
        super.onDestroy();
    }


    protected void onResume(){
        dfra.updateUI();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_settings) {
            Intent pre = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(pre);
        }
        return super.onOptionsItemSelected(item);
    }
}
