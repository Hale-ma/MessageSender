package com.project.hale.messgaesender;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    chatMsgAdapter chatMsgAdapter;
    Handler mUpdateHandler;

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
        chatMsgAdapter = new chatMsgAdapter(this, R.id.msglist, chatmsgList);
        msglist.setAdapter(chatMsgAdapter);
        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                refreshmsglist();
                chatMsgAdapter = new chatMsgAdapter(getApplicationContext(), R.id.msglist, chatmsgList);
                msglist.setAdapter(chatMsgAdapter);
                msglist.smoothScrollToPosition(chatmsgList.size() - 1);

            }
        };

        SenderWifiManager.getInstance().setMsg_handler(mUpdateHandler);


    }

    private void refreshmsglist() {
        if (mainDB.isOpen()) {
            Cursor cursor = mainDB.rawQuery("SELECT * from msg where (tar ='" + Macadd + "' and sor ='"+SenderWifiManager.getMacAddr()+"') or (sor ='" + Macadd + "' and tar ='"+SenderWifiManager.getMacAddr()+"') order by time", null);
            while (cursor.moveToNext()) {
                String tar = cursor.getString(cursor.getColumnIndex("tar"));
                String sor = cursor.getString(cursor.getColumnIndex("sor"));
                String msg = cursor.getString(cursor.getColumnIndex("msg"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                Chatmsg tempmsg = new Chatmsg(sor, tar, time, msg);
                chatmsgList.add(tempmsg);
            }
            cursor.close();
        }
    }


    private class inputKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String newMsg = input_text.getText().toString();
                    if (newMsg.length() > 255) {
                        input_text.setError("Message can not be too long!");
                    } else if (newMsg.length() == 0) {
                        input_text.setError("Can not send empty message!");
                    } else {
                        input_text.setText("");
                        this.sendMessage_boradcast(newMsg);
                        mainDB.execSQL("INSERT INTO msg('sor','tar','time','msg')values('" + SenderWifiManager.getMacAddr() + "','" + Macadd + "','" + SenderWifiManager.getTime() + "','" + newMsg + "')");
                        chatmsgList.add(new Chatmsg(SenderWifiManager.getMacAddr(), Macadd, SenderWifiManager.getTime(), newMsg));// make a temp message object to update the UI
                        chatMsgAdapter = new chatMsgAdapter(getApplicationContext(), R.id.msglist, chatmsgList);
                        msglist.setAdapter(chatMsgAdapter);
                        msglist.smoothScrollToPosition(chatmsgList.size() - 1);

                        return true;
                    }
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
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        public chatMsgAdapter(Context context, int textViewResourceId,
                              List<Chatmsg> objects) {
            super(context, textViewResourceId, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

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
            TextView send = (TextView) v.findViewById(R.id.msg_sender);
            time.setText(cm.time);
            msg.setText(cm.msg);
            return v;
        }


    }

    @Override
    protected void onDestroy() {
        mainDB.close();
        super.onDestroy();
    }
}