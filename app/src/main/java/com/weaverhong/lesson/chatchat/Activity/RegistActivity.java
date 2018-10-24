package com.weaverhong.lesson.chatchat.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.weaverhong.lesson.chatchat.BaseActivity;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

public class RegistActivity extends BaseActivity {

    EditText mEditTextusername;
    EditText mEditTextpassword;
    EditText mEditEmail;
    Button mButtonregist;
    Handler mHandler;

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, RegistActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_regist);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
        mEditTextusername = findViewById(R.id.regist_username);
        mEditTextpassword = findViewById(R.id.regist_password);
        mEditEmail = findViewById(R.id.regist_emailaddress);
        mButtonregist = findViewById(R.id.registbutton);

        mEditTextusername.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入发生改变时，检查是否还有字，如果没字，button改为不可点状态
            }
        });
        mEditTextpassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) {
                // 检查是否空，如果空则返回

                // 检查用户名是否可用，可用才改button
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入发生改变时，检查是否还有字，如果没字，button是不可点击的
            }
        });

        mButtonregist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 编辑框和按键不可用

                // 检查合法性
                if (mEditTextpassword.getTextSize()==0 || mEditTextusername.getTextSize()==0 || mEditEmail.getTextSize()==0) {
                    Toast.makeText(RegistActivity.this, "fuck you, look at the fuck blank places in form, you blind idoit", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 重新可用并给出提示信息，如果注册成功则回退到LoginActivity
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean finished = false;
                        try {
                            OpenfireConnector.regist(mEditTextusername.getText().toString(),
                                    mEditTextpassword.getText().toString(),
                                    mEditEmail.getText().toString(),
                                    null);
                            finished = true;
                        } catch (Exception e) {
                            Looper.prepare();
                            Toast.makeText(RegistActivity.this, "username used OR network error! ", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            return;
                        }
                        if (finished) {
                            Looper.prepare();
                            Toast.makeText(RegistActivity.this, "regist success!", Toast.LENGTH_SHORT).show();
                            Intent intent = RegistActivity.this.getIntent().putExtra("username", mEditTextusername.getText().toString());
                            RegistActivity.this.setResult(1, intent);
                            RegistActivity.this.finish();
                            Looper.loop();
                            return;
                        } else {
                            return;
                        }
                    }
                }).start();
            }
        });
    }
}
