package com.project.hale.messgaesender;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.project.hale.messgaesender.Wifi.SenderWifiManager;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    EditText input_text;
    ListView msglist;
    String Macadd;
    List<Chatmsg> chatmsgList = new ArrayList<>();
    SQLiteDatabase mainDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Macadd = this.getIntent().getExtras().getString("MAC");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        input_text = (EditText) findViewById(R.id.edit_input);
        input_text.setOnKeyListener(new inputKeyListener());
        msglist = (ListView) findViewById(R.id.msglist);
        mainDB = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().getAbsolutePath().replace("files", "databases") + "sendermsg.db", null);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS msg(sor char(64),tar char(64),time char(64),msg char(255))");
        refreshmsglist();

        //Chatmsg test = new Chatmsg("00:22:22:22:22", "c2:c9:76:d9:ff:b7", "2016-12-3 17:13:03", "shishikan");
        msglist.setAdapter(new chatMsgAdapter(this, R.id.msglist, chatmsgList));
    }

    private void refreshmsglist() {
        Cursor cursor = mainDB.rawQuery("SELECT * from msg where tar ='" + Macadd + "' or sor ='" + Macadd + "' order by time", null);
        while (cursor.moveToNext()) {
            String tar = cursor.getString(cursor.getColumnIndex("tar"));
            String sor = cursor.getString(cursor.getColumnIndex("sor"));
            String msg = cursor.getString(cursor.getColumnIndex("msg"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            Chatmsg tempmsg = new Chatmsg(sor, tar, time, msg);
            chatmsgList.add(tempmsg);
           // Log.d("db",sor+" "+tar+" "+time+" "+msg);
        }
        cursor.close();
    }


    private class inputKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String newMsg = input_text.getText().toString();
                    input_text.setText("");
                    this.sendMessage_boradcast(newMsg);
                    return true;
                }
            }
            return false;
        }

        private void sendMessage_boradcast(String message) {
            SenderWifiManager.getInstance().sendmsg(Macadd, message);
        }
    }

    private class Chatmsg {
        String sor, tar, time, msg;

        public Chatmsg(String s, String ta, String ti, String mg) {
            sor = s;
            tar = ta;
            time = ti;
            msg = mg;
        }
    }

    private class chatMsgAdapter extends ArrayAdapter<Chatmsg> {
        private List<Chatmsg> msgs;

        public chatMsgAdapter(Context context, int textViewResourceId,
                              List<Chatmsg> objects) {
            super(context, textViewResourceId, objects);
            msgs = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Chatmsg cm = this.getItem(position);
            if (cm.tar.compareTo(SenderWifiManager.getMacAddr()) == 0) {
                if (v == null) {
                    v = vi.inflate(R.layout.msgrow_in, null);
                }
            } else {
                if (v == null) {
                    v = vi.inflate(R.layout.msgrow_out, null);
                }
            }
            TextView time = (TextView) v.findViewById(R.id.msg_time);
            TextView msg = (TextView) v.findViewById(R.id.msg_row);
            time.setText(cm.time);
            time.setText(cm.msg);
            return v;
        }


    }
}