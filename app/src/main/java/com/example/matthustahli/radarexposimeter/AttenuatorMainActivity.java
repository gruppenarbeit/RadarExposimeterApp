package com.example.matthustahli.radarexposimeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import static java.lang.Thread.sleep;

public class AttenuatorMainActivity extends AppCompatActivity implements View.OnClickListener{


    ImageButton b_batterie;

    String myMode;
    Integer counter=0;
    WifiManager wifi_manager;
    Button b_modeNormal,b_mode21dB,b_mode41dB, b_mode_accumulation,b_chico;
    ProgressBar progressBar;
    LinearLayout layout_settings;
    Handler h = new Handler();
    Intent service;
    final String LOG_TAG = "AttenuatorMainActivity";
    final WifiDataBuffer buffer = new WifiDataBuffer();
    final AttenuatorMainActivityReceiver attenuatorMainActivityReceiver = new AttenuatorMainActivityReceiver(LOG_TAG, buffer);




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attenuator_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        initializeButtons();
        activateClickListener();
        testToLetprogressRun();

        Log.d("AttenuatorMainActivity" , "onCreate finished");
    }


    private void initializeButtons(){
        Log.d("AttenuatorMainActivity" , "initializeButtons called");
        b_modeNormal = (Button) findViewById(R.id.b_mode_normal);
        b_mode21dB = (Button) findViewById(R.id.b_mode_21db);
        b_mode41dB = (Button) findViewById(R.id.b_mode_42db);
        b_mode_accumulation = (Button) findViewById(R.id.b_mode_accumulator);
        b_batterie = (ImageButton) findViewById(R.id.b_batterie);
        b_chico = (Button) findViewById(R.id.b_chico);
        layout_settings= (LinearLayout) findViewById(R.id.settings_atStart);
        progressBar= (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void activateClickListener(){
        b_modeNormal.setOnClickListener(this);
        b_batterie.setOnClickListener(this);
        b_mode_accumulation.setOnClickListener(this);
        b_mode41dB.setOnClickListener(this);
        b_mode21dB.setOnClickListener(this);
        b_chico.setOnClickListener(this);
        layout_settings.setVisibility(View.GONE);
        progressBar.setVisibility(ProgressBar.VISIBLE);

    }

    private void testToLetprogressRun() {

        Thread timer = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "progressbar called");
                progressBar.setMax(100);
                while(progressBar.getProgress() < progressBar.getMax()){
                    if(!buffer.isDataWaiting_FromESP()){
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        byte[] received = buffer.deque_FromESP();
                        if(new String(split_packet(4, 7, received)).equals("PROG")){
                            Progress_Packet_Exposi progPack = new Progress_Packet_Exposi(received);
                            progressBar.setProgress(progPack.get_progress());
                            Log.d(LOG_TAG, "setProgressbar");
                        }
                    }
                }
                Log.d(LOG_TAG, "ProgressThread done");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        progressBar.setVisibility(ProgressBar.GONE);
                        layout_settings.setVisibility(View.VISIBLE);
                    }
                });

            }
        });
        timer.start();

    }



    private void goToNextActivityWithSpecifivMode(String mode) {
        myMode = mode;
        Intent intent = new Intent(AttenuatorMainActivity.this, OverviewScanPlotActivity.class);
        intent.putExtra("MODE", myMode);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        Log.d("AttenuatorMainActivity" , "onStart called");
        StartService();

        super.onStart();
    }

    private void StartService() {
        Intent intent = new Intent(this, CommunicationService.class);
        startService(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(LOG_TAG, "in onResume");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CommunicationService.TRIGGER_Serv2Act);
        registerReceiver(attenuatorMainActivityReceiver, intentFilter);

    }

    @Override
    public void onPause(){
        Log.d(LOG_TAG, "in onPause");
        super.onPause();
        unregisterReceiver(attenuatorMainActivityReceiver);
    }

    private void sendTrigger(byte[] TriggerPack) {
        Intent intent = new Intent();
        intent.setAction(CommunicationService.ACTION_FROM_ACTIVITY);
        intent.putExtra(CommunicationService.TRIGGER_Act2Serv, TriggerPack);
        sendBroadcast(intent);
    }

    @Override
    public void onStop() {
        Log.d(LOG_TAG, "in onstop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "in ondestroy()");

/*        if(this.isFinishing()) {
            stopService(service);
            wifi_manager.setWifiEnabled(true);// sets wifi back on
            Toast.makeText(this, "app finaly closed", Toast.LENGTH_SHORT).show();
        }*/
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG, "in onClick()");
        switch (v.getId()){
            case R.id.b_mode_normal:
                goToNextActivityWithSpecifivMode("normal");
                break;
            case R.id.b_mode_21db:
                goToNextActivityWithSpecifivMode("21dB");
                break;
            case R.id.b_mode_42db:
                goToNextActivityWithSpecifivMode("41dB");
                break;
            case R.id.b_mode_accumulator:
                goToNextActivityWithSpecifivMode("accu");
                break;
            case R.id.b_batterie:
                counter++;
                if (counter % 4 == 0)
                    b_batterie.setBackgroundResource(R.drawable.ic_batterie_empty);
                if (counter % 4 == 1) b_batterie.setBackgroundResource(R.drawable.ic_batterie_low);
                if (counter % 4 == 2)
                    b_batterie.setBackgroundResource(R.drawable.ic_batterie_middle);
                if (counter % 4 == 3) b_batterie.setBackgroundResource(R.drawable.ic_batterie_high);
                break;
            case R.id.b_chico:
                counter++;
                ImageView image = (ImageView) findViewById(R.id.andi);
                if (counter%2==0){ image.setVisibility(ImageView.VISIBLE);
                }else{
                    image.setVisibility(ImageView.GONE);
                }
        }
    }


    protected byte[] split_packet (int start, int end, byte[] packet){

        int length = end - start + 1;
        byte[] splitted = new byte[length];
        for (int i = 0; i < length; i++){
            splitted[i] = packet[i + start];
        }

        return splitted;
    }

    public class AttenuatorMainActivityReceiver extends BroadcastReceiver {
        final String LOG_TAG;
        final WifiDataBuffer wifiDataBufffer;

        public AttenuatorMainActivityReceiver(String LOG_TAG, WifiDataBuffer wifiDataBuffer){
            this.LOG_TAG = LOG_TAG;
            this.wifiDataBufffer = wifiDataBuffer;
        }
        @Override
        public void onReceive(Context arg0, Intent data) {
            Log.d(LOG_TAG, "MyActivityReceiver in onReceive");
            byte[] orgData = data.getByteArrayExtra(CommunicationService.DATA_BACK);
            if (orgData != null) {
                wifiDataBufffer.enque_FromESP(orgData);
            }

        }
    }
}
