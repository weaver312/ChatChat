package com.weaverhong.lesson.chatchat.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.weaverhong.lesson.chatchat.R;

public class UserActivity extends AppCompatActivity {

    TextView mChatwithuser;
    TextView mDeleteuser;

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, UserActivity.class);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_logo);

        mChatwithuser = findViewById(R.id.user_username);
        mDeleteuser = findViewById(R.id.user_delete);

        String fuck = getIntent().getStringExtra("username");

        mChatwithuser.setText(fuck==null?"":fuck);
    }
}
