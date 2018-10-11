package com.weaverhong.lesson.chatchat.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.weaverhong.lesson.chatchat.R;

public class LoginActivity extends Activity {

    EditText mEditTextusername;
    EditText mEditTextpassword;
    Button mButtonlogin;
    Button mButtongotoregist;

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_login);
        mEditTextusername = findViewById(R.id.login_username);
        mEditTextpassword = findViewById(R.id.login_password);
        mButtonlogin = findViewById(R.id.loginbutton);
        mButtongotoregist = findViewById(R.id.gotoregistbutton);
    }
}
