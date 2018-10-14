package com.weaverhong.lesson.chatchat.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Chats;
import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Contacts;
import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Profile;
import com.weaverhong.lesson.chatchat.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView mBottomNavigationView;
    ViewPager mViewPager;

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, MainActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.main_fragmentcontainer);

        // 这句话真的是要命，找了一晚上，太蛋疼了
        // 网上有两种自定义toolbar的方案（包括官方文档）
        // 1. 设置主题为noactionbar，然而这样就永远插不进去任何的actionbar了
        // 2. 保持原主题，在这里直接setSupportActionBar(toolbar)，但是会报错说不能同时用两个bar
        // 真正的解决方案在下面这句，必须告诉系统我要用自定义的toolbar了，系统才会用
        // 网上的办法都是在不支持bar的版本的系统上使用的，都不能用
        // link: https://www.cnblogs.com/Peter-Chen/p/6421354.html
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_logo);

        // ActionBar actionBar = getSupportActionBar();
        // actionBar.setDisplayHomeAsUpEnabled(true);


        mBottomNavigationView = findViewById(R.id.navigation_container);
        mViewPager = findViewById(R.id.viewpager);

        if (fragment == null) {
            fragment = new MainFragment_Chats();
            fm.beginTransaction()
                    .add(R.id.main_fragmentcontainer, fragment)
                    .commit();
        }

        final ArrayList<Fragment> fgLists=new ArrayList<>(3);
        fgLists.add(MainFragment_Chats.newInstance());
        fgLists.add(MainFragment_Contacts.newInstance());
        fgLists.add(MainFragment_Profile.newInstance());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override public void onPageScrollStateChanged(int state) { }
            @Override
            public void onPageSelected(int position) {
                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });
        FragmentPagerAdapter mPagerAdapter=new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override public Fragment getItem(int position) {
                return fgLists.get(position);
            }
            @Override public int getCount() {
                return fgLists.size();
            }
        };
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_navigation_chat_list:
                        mViewPager.setCurrentItem(0, false);
                        break;
                    case R.id.action_navigation_friends:
                        mViewPager.setCurrentItem(1, false);
                        break;
                    case R.id.action_navigation_profile:
                        mViewPager.setCurrentItem(2, false);
                        break;
                }
                return true;
            }
        });
    }
}
