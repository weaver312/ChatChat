package com.weaverhong.lesson.chatchat.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.weaverhong.lesson.chatchat.R;

public class RegistActivity extends Activity {

    EditText mEditTextusername;
    EditText mEditTextpassword;
    Button mButtonregist;

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, RegistActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_regist);
        mEditTextusername = findViewById(R.id.login_username);
        mEditTextpassword = findViewById(R.id.login_password);
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
                // 检查合法性
            }
        });
    }
}
