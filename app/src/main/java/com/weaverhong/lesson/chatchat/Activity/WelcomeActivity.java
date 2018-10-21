package com.weaverhong.lesson.chatchat.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.weaverhong.lesson.chatchat.Datalabs.ContactLab;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WelcomeActivity extends Activity {

    Handler mHandler;
    MyReceiver myReceiver = new MyReceiver();

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_welcome);
        mHandler = new Handler();


        // 临时的intent监听器
        // 注册之后，就可以在MyReceiver中弹出Dialog，对intentFilter的Action及时做出反应，比较轻量
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.weaverhong.lesson.chatchat.addfriend");
        registerReceiver(myReceiver,intentFilter);

        // 这里检查是否登录过，登录状态可通过String的Token存放在SharedPreference中

        // 如果登录状态，则直接去MainActivity

        SharedPreferences sp = getSharedPreferences("chatchat",MODE_PRIVATE);
        Long lastlogintime = sp.getLong("lastlogintime", 0);
        Long currenttime = System.currentTimeMillis();
        // 持续登录60秒
        if (currenttime-lastlogintime>(1000*60)) {
            // Toast.makeText(WelcomeActivity.this,"login out-dated!", Toast.LENGTH_SHORT);
            Intent intent = LoginActivity.newIntent(WelcomeActivity.this);
            startActivity(intent);
        } else {
            String username = sp.getString("username", "admin");
            String password = sp.getString("password", "null");
            final boolean[] result = new boolean[1];
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        result[0] = OpenfireConnector.login(username, password);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            if (!result[0]) {
                // login fail, jump to loginactivity
                Intent intent = LoginActivity.newIntent(WelcomeActivity.this);
                startActivity(intent);
            } else {
                // login success, jump to main activity
                Intent intent = MainActivity.newIntent(WelcomeActivity.this);
                startActivity(intent);
            }
        }
    }

    private static String fromInputStreamtoString(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        String state = os.toString();
        os.close();
        return state;
    }



    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 下面是Receiver处理Connection唤醒的Listener传出的Intent请求的过程
            Bundle bundle = intent.getExtras();
            // 先处理response，看一下是哪种（那五种presence-xml信息里面的哪一种）
            String response = bundle.getString("response");
            Log.e("MYLOG7", "broadcast received:"+response);
            // text_response.setText(response);
            // 这里非空才表示要处理的是加好友的presence信息
            // 因为response空表示这是普通的上下线行为
            if (response.equals("requestfriend")) {
                String acceptAdd = bundle.getString("acceptAdd");
                String alertName = bundle.getString("fromName");
                String alertSubName = "";
                // 这里name是openfire的带domain形式，比如admin@weaver或admin@192.168.191.1
                if (alertName != null)
                    alertSubName = alertName.substring(0, alertName.indexOf("@"));
                if (acceptAdd.equals("receivedrequest")) {
                    // Subname 是可用的名字
                    // Log.e("MYLOG10", "alertSubName:"+alertSubName);
                    // Log.e("MYLOG10", "acceptAdd:"+acceptAdd);
                    // Log.e("MYLOG10", "alertName:"+alertName);

                    // NEW METHOD: store friends who are waiting to be added in DB or SP
                    // show them when calling updateUI(), in the friend list.
                    // ContactLab.mContactItems_nogroup.add(new ContactListItem())
                    // 20181020, consider a subscribe presence could be sent more than one time.
                    // using a HashSet in ContactLab to store the information.
                    ContactLab.mContactItems_nogroup.add(alertSubName);

                    // NOTIFICATION: a important rule,
                    // Alertdialog is not allowed using here, INSIDE a broadcast intent receiver.
                    // the former edition can be found on Github
                }
            }
        }
    }
}
