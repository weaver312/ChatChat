package com.weaverhong.lesson.chatchat.Activity_Autoshutdown;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.weaverhong.lesson.chatchat.OpenfireConnector;

public class BaseActivity extends Activity {
    private DestroyReceiver mDestroyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDestroyReceiver = new DestroyReceiver();
        IntentFilter intentFilter = new IntentFilter(OpenfireConnector.EXIT_ALL);
        registerReceiver(mDestroyReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDestroyReceiver);
        SharedPreferences sp = getSharedPreferences("chatchat", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("lastlogintime", System.currentTimeMillis());
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("chatchat", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("lastlogintime", System.currentTimeMillis());
        editor.apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sp = getSharedPreferences("chatchat", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("lastlogintime", System.currentTimeMillis());
        editor.apply();
    }

    public class DestroyReceiver extends BroadcastReceiver {
        public void onReceive(Context arg0, Intent intent) {
            finish();
        }
    }
}
