package com.weaverhong.lesson.chatchat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class WelcomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        // 这里检查是否登录过，登录状态可通过String的Token存放在SharedPreference中

        // 如果登录状态，则直接去MainActivity

        // 如果没有登录，则展示动画效果，进入LoginActivity
        Intent intent = LoginActivity.newIntent(this);
        startActivity(intent);
    }
}
