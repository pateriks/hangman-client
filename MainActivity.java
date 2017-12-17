package com.example.patrik.hangclient;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.util.TimeUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {
TextView tv;
BufferedReader br;
PrintWriter bw;
String display ="init";
Boolean lock = false;
String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy p = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(p);

        byte [] ip = {(byte)100, (byte)69, (byte)9, (byte)34};
        Socket s = null;
        try {
            s = new Socket(InetAddress.getByAddress(ip), 8888);
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            bw = new PrintWriter((s.getOutputStream()));
            try {
                long b = System.currentTimeMillis();
                while(!br.ready()) {
                    if(System.currentTimeMillis()- b > 1000){
                        throw new TimeoutException();
                    }
                    Log.i(TAG, "onCreate: " + (System.currentTimeMillis() - b));
                }
                display = br.readLine();
            }catch (Exception e){
                display = "no server";
            }
            if(display == null){
                display = "no server";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv = findViewById(R.id.input);
        tv.setText(display);

        Button b = (Button) findViewById(R.id.button);
        Button b2 = (Button) findViewById(R.id.button2);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = (EditText) findViewById(R.id.editText);
                String g = et.getText().toString();
                if(!lock) {
                    lock = true;
                    startTx(g);
                }
                tv.setText(display);
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*EditText et = (EditText) findViewById(R.id.editText);
                String g = et.getText().toString();
                if(!lock) {
                    lock = true;
                    startTx("new");
                }*/
                tv.setText(display);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    String input;
                    try {
                        if((input = br.readLine()) != null){
                            display = input;
                            Log.i(TAG, "startTx: BufferedReader: " + br.toString() + " has read from server");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    try {
                        Thread.sleep(23);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }).start();
    }

    public void onResume() {
        super.onResume();
    }


    public void onDestroy() {
        super.onDestroy();
        if(bw != null){
            try {
                bw.close();
                if(br != null) {
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startTx(String send){
        String input = send;
        bw.println(input);
        bw.flush();
        commitTx();
    }

    public void commitTx(){
        Log.i(TAG, "commitTx: ");
        lock = false;
    }

    public void rollback(){
        Log.i(TAG, "rollback: " + this.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
