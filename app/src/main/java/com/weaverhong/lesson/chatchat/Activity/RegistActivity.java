package com.weaverhong.lesson.chatchat.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.weaverhong.lesson.chatchat.R;

public class RegistActivity extends Activity {

    EditText mEditTextusername;
    EditText mEditTextpassword;
    Button mButtonregist;
    Button mButtongotologin;

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_regist);
        mEditTextusername = findViewById(R.id.login_username);
        mEditTextpassword = findViewById(R.id.login_password);
        mButtonregist = findViewById(R.id.registbutton);
        mButtongotologin = findViewById(R.id.gotologinbutton);
    }
}
