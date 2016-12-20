package com.project.hale.messgaesender;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.project.hale.messgaesender.Wifi.WifiBoardCastManager;

public class ChatActivity extends AppCompatActivity {
    EditText input_text;
    String chating_mac;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        chating_mac=this.getIntent().getExtras().getString("mac");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        input_text = (EditText) findViewById(R.id.edit_input);
        input_text.setOnKeyListener(new inputKeyListener());
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

        private void sendMessage_boradcast(String message){
           // WifiBoardCastManager.getsInstance().startBoradcast(chating_mac,message);
        }
    }
}