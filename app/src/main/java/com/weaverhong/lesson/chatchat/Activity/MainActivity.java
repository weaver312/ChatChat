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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.weaverhong.lesson.chatchat.BaseAppCompatActivity;
import com.weaverhong.lesson.chatchat.Datalabs.ContactLab;
import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Chats;
import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Contacts;
import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Profile;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

public class MainActivity extends BaseAppCompatActivity {

    BottomNavigationView mBottomNavigationView;
    ViewPager mViewPager;
    Context mContext;

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, MainActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
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

        // final ArrayList<Fragment> fgLists=new ArrayList<>(3);
        MainFragment_Chats frag0 = MainFragment_Chats.newInstance();
        MainFragment_Contacts frag1 = MainFragment_Contacts.newInstance();
        MainFragment_Profile frag2 = MainFragment_Profile.newInstance();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    try {
                        ContactLab.refreshdatalocal(getApplicationContext());
                        frag1.updateUI();
                    } catch (Exception e) {
                    }
                }
                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });
        FragmentPagerAdapter mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return frag0;
                    case 1:
                        return frag1;
                    case 2:
                        return frag2;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 3;
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

        mBottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_navigation_chat_list:
                        mViewPager.setCurrentItem(0, false);
                        break;
                    case R.id.action_navigation_friends:
                        ContactLab.refreshdatalocal(getApplicationContext());
                        // 这里有点疑问，为什么Toast显示不出来，不管是getApplicationContext还是什么，好像都不行，诡谲
                        Toast.makeText(mContext,"Updating contacts from server... please refresh more times!", Toast.LENGTH_SHORT);
                        ContactLab.refreshdataonline(getApplicationContext());
                        frag1.updateUI();
                        mViewPager.setCurrentItem(1, false);
                        break;
                    case R.id.action_navigation_profile:
                        mViewPager.setCurrentItem(2, false);
                        break;
                }
            }
        });
    }

    private long exitTime = 0;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(),
                    "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            OpenfireConnector.breakConn();
            Intent intent = new Intent();
            intent.setAction(OpenfireConnector.EXIT_ALL);
            sendBroadcast(intent);
            finish();
        }
    }

}
