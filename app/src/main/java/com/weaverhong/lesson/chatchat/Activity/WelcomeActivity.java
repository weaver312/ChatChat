package com.weaverhong.lesson.chatchat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WelcomeActivity extends Activity {

    Handler mHandler;

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_welcome);
        mHandler = new Handler();
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

}
