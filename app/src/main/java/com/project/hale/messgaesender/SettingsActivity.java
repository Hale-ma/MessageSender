package com.project.hale.messgaesender;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.project.hale.messgaesender.Wifi.SenderWifiManager;

//The Activty provoid GUI of settings.
public class SettingsActivity extends AppCompatActivity {
    EditText checkinterval_edit, enable_edit,disable_edit,bt_edit;
    Button deldb, savesetting,delcache;

    SQLiteDatabase mainDB;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        preferences = getSharedPreferences("SenderSettings", Context.MODE_PRIVATE);
        checkinterval_edit = (EditText) findViewById(R.id.CheckInterval_edit);
        enable_edit = (EditText) findViewById(R.id.wifi_enable_time_edit);
        disable_edit= (EditText) findViewById(R.id.wifi_disable_time_edit);
        bt_edit=(EditText)findViewById(R.id.bt_interval_edit);
        deldb = (Button) findViewById(R.id.cleandb_button);
        delcache=(Button)findViewById(R.id.clean_cache);
        savesetting = (Button) findViewById(R.id.save_button);

        //load settings from Sharedpreference, if it does not exist, use default settings instead
        checkinterval_edit.setText(String.valueOf(preferences.getInt("checkinterval", 30000)));
        enable_edit.setText(String.valueOf(preferences.getInt("enable", 3000)));
        disable_edit.setText(String.valueOf(preferences.getInt("disable", 1500)));
        bt_edit.setText(String.valueOf(preferences.getInt("bt_interval",10000)));


        deldb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainDB = SQLiteDatabase.openOrCreateDatabase(getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "sendermsg.db", null);
                mainDB.execSQL("delete from msg");
                Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                mainDB.close();
            }
        });
        delcache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainDB = SQLiteDatabase.openOrCreateDatabase(getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "sendermsg.db", null);
                mainDB.execSQL("delete from msg where tar!='" + SenderWifiManager.getMacAddr() + "' and sor!='" + SenderWifiManager.getMacAddr() + "'");
                Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                mainDB.close();
            }
        });
        savesetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cd=Integer.parseInt(checkinterval_edit.getText().toString());
                int eni=Integer.parseInt(enable_edit.getText().toString());
                int dni =Integer.parseInt(disable_edit.getText().toString());
                int bti=Integer.parseInt(bt_edit.getText().toString());
                //check invalid input
               if(cd<eni+dni){
                   checkinterval_edit.setError("This value can not be less than the sum of enable interval and disable interval");
               }else if(eni<500){
                   enable_edit.setError("Too small");
               }else if(dni<500){
                   disable_edit.setText("Too small");
               }else if(bti<5000){
                   bt_edit.setText("Too small");
               }
               else {
                   SharedPreferences.Editor editor = preferences.edit();
                   editor.putInt("checkinterval", cd);
                   editor.putInt("enable", eni);
                   editor.putInt("disable", dni);
                   editor.putInt("bt_interval", bti);
                   editor.commit();
                   finish();
               }
            }
        });
    }
}
