package com.project.hale.messgaesender.Wifi;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.View;


/**
 * Created by mahon on 2016/10/22.
 */

public class myWifiActionListener implements WifiP2pManager.ActionListener {
    String actionname = "Wifi Action";
    View v = null;

    public myWifiActionListener(String _actionname) {
        actionname = _actionname;
    }

    public myWifiActionListener(String _actionname, View v) {

    }

    @Override
    public void onSuccess() {
        Log.d("wifi", actionname + ": Success");
    }

    @Override
    public void onFailure(int arg0) {
        Log.d("wifi", actionname + ": failed" + arg0);
    }
}