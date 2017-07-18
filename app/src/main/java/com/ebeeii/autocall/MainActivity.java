package com.ebeeii.autocall;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText etPhone;
    private CheckBox mCbAutoCall;

    private SharedPreferences sharedPreference;
    private SharedPreferences.Editor editor;
    private AudioManager audioManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        etPhone = (EditText) findViewById(R.id.etPhone);
        mCbAutoCall = (CheckBox)findViewById(R.id.cb_autocall);
        mCbAutoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCbAutoCall.isChecked() == false) {

                }
            }
        });

        String telnum = sharedPreference.getString("tel_number", "");
        etPhone.setText(telnum);

        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelListner listener = new TelListner() ;
        telManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);
    }

    // ACTION_CALL方式拨打电话(直接拨打)
    public void onClickActionCall(View v) {
        //这里的Intent.ACTION_CALL实际就是一个特定的字符串，
        //ACTION_CALL = "android.intent.action.CALL"，
        //告诉系统我要直接拨号了。

        editor = sharedPreference.edit();
        editor.putString("tel_number", etPhone.getText().toString());
        editor.apply();

        call(Intent.ACTION_CALL);
    }

    // ACTION_DIAL方式拨打电话(打开拨号界面)
    public void onClickActionDial(View v) {
        //同理，这里的Intent.ACTION_DIAL也是一个特定的字符串
        //ACTION_DIAL = "android.intent.action.DIAL"
        //告诉系统我要打开拨号界面，并把要拨的号显示在拨号界面上，由用户决定是否要拨打。
        call(Intent.ACTION_DIAL);
    }

    private void call(String action){
        String phone = etPhone.getText().toString();
        if(phone!=null&&phone.trim().length()>0){
            //这里"tel:"+电话号码 是固定格式，系统一看是以"tel:"开头的，就知道后面应该是电话号码。
            Intent intent = new Intent(action, Uri.parse("tel:" + phone.trim()));
            startActivity(intent);//调用上面这个intent实现拨号
        }else{
            Toast.makeText(this, "电话号码不能为空", Toast.LENGTH_LONG).show();
        }
    }

    private class TelListner extends PhoneStateListener {
        boolean comingPhone=false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:/* 无任何状态 */
                    Log.d(TAG, "phone idle");
                    if(this.comingPhone){
                        this.comingPhone=false;
                    }

/*
                    if (mCbAutoCall.isChecked()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Thread.sleep(3000);
                                handler.sendMessage();//告诉主线程执行任务
                            }
                        }).start();
                    }
                    */

                    if (mCbAutoCall.isChecked()) {
                        new Handler().postDelayed(new Runnable()
                        {
                            public void run()
                            {
                                if (mCbAutoCall.isChecked()){
                                    call(Intent.ACTION_CALL);
                                    setSpeekModle(true);
                                }
                            }
                        }, 3000);
                        Log.e(TAG, "onCallStateChanged: CALL_STATE_IDLE" );
                    }

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:   /* 接起电话 */
                    Log.d(TAG, "phone answer");
                    this.comingPhone=true;
                    setSpeekModle(true);
//                    mCbAutoCall.setChecked(false);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:   /* 电话进来 */
                    Log.d(TAG, "phone coming");
                    this.comingPhone=true;
                    setSpeekModle(true);
                    mCbAutoCall.setChecked(false);
                    break;
            }
        }
    }


    void setSpeekModle(boolean open){
        //audioManager.setMode(AudioManager.ROUTE_SPEAKER);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setMode(AudioManager.MODE_IN_CALL);

        if(!audioManager.isSpeakerphoneOn()&&true==open) {
            audioManager.setSpeakerphoneOn(true);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
        }else if(audioManager.isSpeakerphoneOn()&&false==open){
            audioManager.setSpeakerphoneOn(false);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,currVolume,
                    AudioManager.STREAM_VOICE_CALL);
        }
    }



}
