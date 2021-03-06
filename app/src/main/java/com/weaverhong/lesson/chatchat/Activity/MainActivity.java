package com.weaverhong.lesson.chatchat.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.weaverhong.lesson.chatchat.Activity_Autoshutdown.BaseAppCompatActivity;
import com.weaverhong.lesson.chatchat.Datalabs.ContactLab;
import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Chats;
import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Contacts;
import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Profile;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

import static com.weaverhong.lesson.chatchat.OpenfireConnector.NEW_ADDFRIEND;
import static com.weaverhong.lesson.chatchat.OpenfireConnector.sAbstractXMPPConnection;

public class MainActivity extends BaseAppCompatActivity {

    BottomNavigationView mBottomNavigationView;
    ViewPager mViewPager;
    Context mContext;
    private MessageReceiver mMessageReceiver;


    MainFragment_Chats frag0;
    MainFragment_Contacts frag1;
    MainFragment_Profile frag2;

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
        // getcolor仅适用API23(android 6.0)及以后的系统
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //     mBottomNavigationView.setBackgroundColor(getColor(R.color.navigationBackground));
        // }
        mViewPager = findViewById(R.id.viewpager);

        if (fragment == null) {
            fragment = new MainFragment_Chats();
            fm.beginTransaction()
                    .add(R.id.main_fragmentcontainer, fragment)
                    .commit();
        }
        if (sAbstractXMPPConnection.isAuthenticated())
            ContactLab.refreshdataonline(getApplicationContext());

        // final ArrayList<Fragment> fgLists=new ArrayList<>(3);
        frag0 = MainFragment_Chats.newInstance();
        frag1 = MainFragment_Contacts.newInstance();
        frag2 = MainFragment_Profile.newInstance();
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
                        try {
                            ContactLab.refreshdatalocal(getApplicationContext());
                            frag1.updateUI();
                        } catch (Exception e) {
                        }
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
                        // Log.e("MainActivity-onNavigationItemReselected!", "refreshing from server");
                        // 这里必须先检测服务器是否联通
                        if (OpenfireConnector.sAbstractXMPPConnection.isAuthenticated()) {
                            // try {
                                Toast.makeText(MainActivity.this, "Updating contacts from server...", Toast.LENGTH_SHORT).show();
                                ContactLab.refreshdataonline(getApplicationContext());
                                // getContactsFromServerByRoster();
                                // Thread.sleep(500);
                            // } catch (InterruptedException e) {
                            //     e.printStackTrace();
                            // }
                        } else {
                            Toast.makeText(MainActivity.this, "Please check network", Toast.LENGTH_SHORT).show();
                        }
                        ContactLab.refreshdatalocal(getApplicationContext());
                        frag1.updateUI();
                        mViewPager.setCurrentItem(1, false);
                        break;
                    case R.id.action_navigation_profile:
                        mViewPager.setCurrentItem(2, false);
                        break;
                }
            }
        });

        IntentFilter intentFilter = new IntentFilter(NEW_ADDFRIEND);
        registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            ContactLab.refreshdatalocal(getApplicationContext());
            frag1.updateUI();
        } catch (Exception e) {
        }
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
                    "press back to exit", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            OpenfireConnector.breakConn();
            Intent intent = new Intent();
            intent.setAction(OpenfireConnector.EXIT_ALL);
            sendBroadcast(intent);
            finish();
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ContactLab.refreshdatalocal(mContext);
            frag1.updateUI();
        }
    }
}
