package com.weaverhong.lesson.chatchat.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.weaverhong.lesson.chatchat.R;
import com.weaverhong.lesson.chatchat.fragment.MainFragment_Chats;
import com.weaverhong.lesson.chatchat.fragment.MainFragment_Contacts;
import com.weaverhong.lesson.chatchat.fragment.MainFragment_Profile;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView mBottomNavigationView;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.main_fragmentcontainer);
        mBottomNavigationView = findViewById(R.id.navigation_container);
        mViewPager = findViewById(R.id.viewpager);

        if (fragment == null) {
            fragment = new MainFragment_Chats();
            fm.beginTransaction()
                    .add(R.id.main_fragmentcontainer, fragment)
                    .commit();
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override public void onPageScrollStateChanged(int state) { }
            @Override
            public void onPageSelected(int position) {
                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });

        final ArrayList<Fragment> fgLists=new ArrayList<>(3);
        fgLists.add(new MainFragment_Chats());
        fgLists.add(new MainFragment_Contacts());
        fgLists.add(new MainFragment_Profile());

        FragmentPagerAdapter mPagerAdapter=new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override public Fragment getItem(int position) { return fgLists.get(position); }
            @Override public int getCount() { return fgLists.size(); }
        };
        mViewPager.setAdapter(mPagerAdapter);

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
