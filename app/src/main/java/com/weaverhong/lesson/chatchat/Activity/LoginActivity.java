package com.weaverhong.lesson.chatchat.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

public class LoginActivity extends Activity {

    EditText mEditTextusername;
    EditText mEditTextpassword;
    Button mButtonlogin;
    Button mButtongotoregist;
    Handler mHandler;

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, LoginActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_login);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        mEditTextusername = findViewById(R.id.login_username);
        mEditTextpassword = findViewById(R.id.login_password);
        mButtonlogin = findViewById(R.id.loginbutton);
        mButtongotoregist = findViewById(R.id.gotoregistbutton);

        mButtonlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mEditTextusername.getText().toString();
                String password = mEditTextpassword.getText().toString();
                // 登录，如果登录成功则跳转，失败就提示是没有用户名还是密码不对，return
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OpenfireConnector.login(username, password);
                        } catch (Exception e) { Log.e("MYLOG1", e.toString()); }
                        try {
                            if (OpenfireConnector.isAuthenticated()) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "Yeah, login success!", Toast.LENGTH_LONG).show();
                                        Intent intent = MainActivity.newIntent(LoginActivity.this);
                                        startActivity(intent);
                                    }
                                });
                            } else {
                                Looper.prepare();
                                if (OpenfireConnector.isConnected())
                                    Toast.makeText(LoginActivity.this, "Shit, login fail!", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(LoginActivity.this, "God, connect fail", Toast.LENGTH_LONG).show();
                                Looper.loop();
                            }
                        } catch (Exception e) { Log.e("MYLOG2", e.toString()); }
                    }
                }).start();
            }
        });
        mButtongotoregist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转去RegistActivity，
                Intent intent = RegistActivity.newIntent(LoginActivity.this);
                startActivity(intent);
            }
        });
    }
}
