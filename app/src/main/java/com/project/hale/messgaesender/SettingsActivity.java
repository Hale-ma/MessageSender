package com.project.hale.messgaesender;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    EditText checkinterval_edit, retrycounts_edit;
    Button deldb, savesetting;

    SQLiteDatabase mainDB;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        preferences = getSharedPreferences("SenderSettings", Context.MODE_PRIVATE);
        checkinterval_edit = (EditText) findViewById(R.id.CheckInterval_edit);
        retrycounts_edit = (EditText) findViewById(R.id.RetryCounts_edit);
        deldb = (Button) findViewById(R.id.cleandb_button);
        savesetting = (Button) findViewById(R.id.save_button);
        checkinterval_edit.setText(String.valueOf(preferences.getInt("checkinterval", 8000)));
        retrycounts_edit.setText(String.valueOf(preferences.getInt("retrycount", 3)));
        deldb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainDB = SQLiteDatabase.openOrCreateDatabase(getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "sendermsg.db", null);
                mainDB.execSQL("delete from msg");
                Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                mainDB.close();
            }
        });
        savesetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cd=Integer.parseInt(checkinterval_edit.getText().toString());
                int retrycount=Integer.parseInt(retrycounts_edit.getText().toString());
                if(cd<3000){
                    checkinterval_edit.setError("This value can be smaller than 3000");
                }
                if(retrycount<2){
                    checkinterval_edit.setError("This value can be smaller than 2");
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("checkinterval",cd);
                editor.putInt("retrycount",retrycount);
                editor.commit();
            }
        });
    }
}
