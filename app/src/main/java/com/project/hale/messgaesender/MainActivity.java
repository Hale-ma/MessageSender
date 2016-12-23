package com.project.hale.messgaesender;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.peak.salut.Callbacks.SalutDataCallback;
import com.project.hale.messgaesender.Wifi.SenderDevice;
import com.project.hale.messgaesender.Wifi.WifiBoardCastManager;

import java.util.UUID;

import eu.hgross.blaubot.android.BlaubotAndroid;
import eu.hgross.blaubot.android.BlaubotAndroidFactory;
import eu.hgross.blaubot.core.IBlaubotDevice;
import eu.hgross.blaubot.core.ILifecycleListener;
import eu.hgross.blaubot.messaging.BlaubotMessage;
import eu.hgross.blaubot.messaging.IBlaubotChannel;
import eu.hgross.blaubot.messaging.IBlaubotMessageListener;


public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnFragmentInteractionListener, SalutDataCallback {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiBoardCastManager wm = WifiBoardCastManager.getsInstance();
    final UUID APP_UUID = UUID.fromString("ec127529-2e9c-3946-a5a5-144feb30465f");
    BlaubotAndroid blaubot;

    // final UUID APP_UUID=UUID.randomUUID();
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
        //init BT library
        init_blaubot();



        //
        setContentView(R.layout.activity_main);
        //     initwifi();


    }

    @Override
    public void onFragmentInteraction(SenderDevice device) {
        Log.d("Sender GUI", "onclick - " + device.deviceAddress);
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("mac", device.deviceAddress);
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

    protected void onResume() {
        blaubot.startBlaubot();
        blaubot.registerReceivers(this);
        blaubot.setContext(this);
        blaubot.onResume(this);
        super.onResume();
    }

    protected void onPause() {
        blaubot.unregisterReceivers(this);
        blaubot.onPause(this);
        super.onPause();
    }

    protected void onStop() {
        blaubot.stopBlaubot();
        super.onStop();
    }

    protected void onNewIntent(Intent intent) {
        blaubot.onNewIntent(intent);
        super.onNewIntent(intent);
    }

    private void init_blaubot(){
        blaubot = BlaubotAndroidFactory.createBluetoothBlaubot(APP_UUID);
        blaubot.addLifecycleListener(new ILifecycleListener() {
            @Override
            public void onConnected() {
                Log.d("BT","onConnected");
                final IBlaubotChannel channel = blaubot.createChannel((short)1);
                channel.publish("Hello world 341234".getBytes(),true);
                channel.subscribe(new IBlaubotMessageListener() {
                    @Override
                    public void onMessage(BlaubotMessage message) {
                        // we got a message - our payload is a byte array
                        // deserialize
                        String msg = new String(message.getPayload());
                        Log.d("BT"," recevice:"+msg);
                    }
                });
            }

            @Override
            public void onDisconnected() {
                Log.d("BT","onDisconnected");
            }

            @Override
            public void onDeviceJoined(IBlaubotDevice blaubotDevice) {
                Log.d("BT","onDeviceJoined "+blaubotDevice.getReadableName());
            }

            @Override
            public void onDeviceLeft(IBlaubotDevice blaubotDevice) {
                Log.d("BT","onDeviceLeft "+blaubotDevice.getReadableName());
            }

            @Override
            public void onPrinceDeviceChanged(IBlaubotDevice oldPrince, IBlaubotDevice newPrince) {
                Log.d("BT","onPrinceDeviceChanged ");
            }

            @Override
            public void onKingDeviceChanged(IBlaubotDevice oldKing, IBlaubotDevice newKing) {
                Log.d("BT","onKingDeviceChanged ");
            }
        });
        if(!blaubot.isStarted()) {
            blaubot.startBlaubot();
        }

    }
}
