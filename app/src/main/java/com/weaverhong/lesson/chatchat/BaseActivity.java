package com.weaverhong.lesson.chatchat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class BaseActivity extends Activity {
    private DestroyReceiver mDestroyReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDestroyReceiver = new DestroyReceiver();
        IntentFilter intentFilter = new IntentFilter(OpenfireConnector.EXIT_ALL);
        registerReceiver(mDestroyReceiver, intentFilter);
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDestroyReceiver);
    }

    public class DestroyReceiver extends BroadcastReceiver {
        public void onReceive(Context arg0, Intent intent) {
            finish();
        }
    }
}
