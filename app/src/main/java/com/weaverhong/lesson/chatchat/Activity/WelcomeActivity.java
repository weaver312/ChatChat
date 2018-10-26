package com.weaverhong.lesson.chatchat.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.weaverhong.lesson.chatchat.Activity_Autoshutdown.BaseActivity;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

public class WelcomeActivity extends BaseActivity {

    // MyReceiver myReceiver = new MyReceiver();

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_welcome);

        // welcome注册了两个intent监听器，都是与数据库操作相关的，所以要在最开始的WelcomeActivity开始监听
        // addfriend负责接收presence加好友的信号，加到数据库里面（要改）
        // newmessage负责接收message信息，写进数据库
        // IntentFilter intentFilter = new IntentFilter();
        // intentFilter.addAction("com.weaverhong.lesson.chatchat.addfriend");
        // intentFilter.addAction("com.weaverhong.lesson.chatchat.newmessage");
        // registerReceiver(myReceiver,intentFilter);

        // 这里检查是否登录过，登录状态可通过String的Token存放在SharedPreference中
        // 如果登录状态，则直接去MainActivity
        SharedPreferences sp = getSharedPreferences("chatchat",MODE_PRIVATE);
        Long lastlogintime = sp.getLong("lastlogintime", 0);

        Long currenttime = System.currentTimeMillis();

        // 持续登录10分钟
        if (currenttime-lastlogintime>(1000*600)) {
            Intent intent = LoginActivity.newIntent(WelcomeActivity.this);
            startActivity(intent);
        } else {
            String username = sp.getString("username", "null");
            String password = sp.getString("password", "null");
            if (username.equals("null") && password.equals("null")) {
                SharedPreferences.Editor editor = sp.edit();
                editor.remove("lastlogintime");
                editor.clear();
                editor.commit();
                Intent intent = LoginActivity.newIntent(WelcomeActivity.this, username.equals("null")?"":username);
                startActivity(intent);
                finish();
            }
            final boolean[] result = new boolean[1];
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OpenfireConnector.buildConn(WelcomeActivity.this);
                        result[0] = OpenfireConnector.login(username, password);
                        Log.e("WelcomActivity","fuck3");
                        if (!result[0]) {
                            // login fail, go to loginactivity
                            Intent intent = LoginActivity.newIntent(WelcomeActivity.this, username.equals("null")?"":username);
                            startActivity(intent);
                            finish();
                        } else {
                            // login success, go to main activity
                            Intent intent = MainActivity.newIntent(WelcomeActivity.this);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    // public class MyReceiver extends BroadcastReceiver {
    //     @Override
    //     public void onReceive(Context context, Intent intent) {
    //         // 下面是Receiver处理Connection唤醒的Listener传出的Intent请求的过程
    //         Bundle bundle = intent.getExtras();
    //         // 先处理response，看一下是哪种（那五种presence-xml信息里面的哪一种）
    //         String response = bundle.getString("response");
    //         // Log.e("MYLOG7", "broadcast received:"+response);
    //         // text_response.setText(response);
    //         // 这里非空才表示要处理的是加好友的presence信息
    //         // 因为response空表示这是普通的上下线行为
    //         if (response.equals("requestfriend")) {
    //             Log.e("BROADCAST RECEIVED", "WelcomeActivity, request friend, " + bundle.getString("fromName"));
    //             String acceptAdd = bundle.getString("acceptAdd");
    //             String alertName = bundle.getString("fromName");
    //             String alertSubName = "";
    //             // 这里name是openfire的带domain形式，比如admin@weaver或admin@192.168.191.1
    //             if (alertName != null)
    //                 alertSubName = alertName.substring(0, alertName.indexOf("@"));
    //             if (acceptAdd.equals("receivedrequest")) {
    //                 // Subname 是可用的名字
    //                 // Log.e("MYLOG10", "alertSubName:"+alertSubName);
    //                 // Log.e("MYLOG10", "acceptAdd:"+acceptAdd);
    //                 // Log.e("MYLOG10", "alertName:"+alertName);
    //
    //                 // NEW METHOD: store friends who are waiting to be added in DB or SP
    //                 // show them when calling updateUI(), in the friend list.
    //                 // ContactLab.mContactItems_nogroup.add(new ContactListItem())
    //                 // 20181020, consider a subscribe presence could be sent more than one time.
    //                 // using a HashSet in ContactLab to store the information.
    //                 ContactLab.mContactItems_nogroup.add(alertSubName);
    //
    //                 // NOTIFICATION: a important rule,
    //                 // Alertdialog is not allowed using here, INSIDE a broadcast intent receiver.
    //                 // the former edition can be found on Github
    //             }
    //         } else if (response.equals("message")) {
    //             Log.e("BROADCAST RECEIVED", "WelcomeActivity, message");
    //             // Only message received are processed. Message sent is not listened
    //             String msgtranid = bundle.getString("MSGTRANID");
    //             String sendername= bundle.getString("SENDERNAME");
    //             String receivername = bundle.getString("RECEIVERNAME");
    //             String createtime = bundle.getString("CREATETIME");
    //             String content = bundle.getString("CONTENT");
    //
    //             MessageEntity item = new MessageEntity();
    //             item.setMsgtranid(msgtranid);
    //             item.setSendername(sendername);
    //             item.setReceivername(receivername);
    //             item.setCreatetime(createtime);
    //             item.setContent(content);
    //             // 1 is receive message
    //             item.setDirection(1);
    //
    //             // insert into database
    //             MessageDBManager messageDBManager = new MessageDBManager(context);
    //             messageDBManager.add(item);
    //
    //         }
    //     }
    // }
}
