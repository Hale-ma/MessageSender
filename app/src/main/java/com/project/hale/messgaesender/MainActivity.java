package com.project.hale.messgaesender;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutServiceData;
import com.project.hale.messgaesender.Wifi.SenderDevice;
import com.project.hale.messgaesender.Wifi.WifiBoardCastManager;

import java.util.List;

import top.wuhaojie.bthelper.BtHelperClient;
import top.wuhaojie.bthelper.MessageItem;
import top.wuhaojie.bthelper.OnSearchDeviceListener;
import top.wuhaojie.bthelper.OnSendMessageListener;

public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnFragmentInteractionListener, SalutDataCallback {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiBoardCastManager wm = WifiBoardCastManager.getsInstance();
    BtHelperClient btHelperClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Android 6.0 Bluetooth
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 5230);
        }
        //enable bluetooth discoverable
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);


        setContentView(R.layout.activity_main);
        btHelperClient = BtHelperClient.from(MainActivity.this);
        btHelperClient.searchDevices(new OnSearchDeviceListener() {
            @Override
            public void onStartDiscovery() {
                Log.d("BT","onStart");
            }

            @Override
            public void onNewDeviceFounded(BluetoothDevice bluetoothDevice) {
                Log.d("BT","onFound"+bluetoothDevice.getAddress());
                MessageItem mi=new MessageItem("Messageoasfklajsasdhfkajsfdhk");
                btHelperClient.sendMessage(bluetoothDevice.getAddress(), mi, true, new OnSendMessageListener() {
                    @Override
                    public void onSuccess(int i, String s) {
                        Log.d("BT","onSuccess"+i+" "+s);
                    }

                    @Override
                    public void onConnectionLost(Exception e) {
                        Log.d("BT","onConnectionLost");
                        e.printStackTrace();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("BT","onError"+e);
                    }
                });

            }

            @Override
            public void onSearchCompleted(List<BluetoothDevice> bondedList, List<BluetoothDevice> newList) {
                Log.d("BT", "SearchCompleted: bondedList" + bondedList.toString());
                Log.d("BT", "SearchCompleted: newList" + newList.toString());
            }

            @Override
            public void onError(Exception e) {
                Log.d("BT","onError"+e);
            }
        });
   //     initwifi();


    }

    @Override
    public void onFragmentInteraction(SenderDevice device) {
        Log.d("Sender GUI", "onclick - " + device.deviceAddress);
        Intent intent= new Intent(MainActivity.this,ChatActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("mac",device.deviceAddress);
        intent.putExtras(bundle);
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
