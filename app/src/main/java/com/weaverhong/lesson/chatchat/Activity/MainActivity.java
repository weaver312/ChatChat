package com.weaverhong.lesson.chatchat.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Chats;
import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Contacts;
import com.weaverhong.lesson.chatchat.Fragment.MainFragment_Profile;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;

import static com.weaverhong.lesson.chatchat.OpenfireConnector.DOMAIN;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView mBottomNavigationView;
    ViewPager mViewPager;
    private String name,password,response,acceptAdd,alertName,alertSubName;
    MainActivity.MyReceiver myReceiver = new MainActivity.MyReceiver();

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

        // 临时的intent监听器
        // 注册之后，就可以在MyReceiver中弹出Dialog，对intentFilter的Action及时做出反应，比较轻量
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.weaverhong.lesson.chatchat.addfriend");
        registerReceiver(myReceiver,intentFilter);

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
        StanzaFilter stanzaFilter = new AndFilter(new StanzaTypeFilter(Presence.class));

        StanzaListener sendlistener = new StanzaListener() {
            @Override
            public void processStanza(Stanza packet) {
                // System.out.println(packet.toXML());
                if(packet instanceof Presence){
                    Presence presence = (Presence)packet;
                    // 这里真的佛了，这个版本的smack发送presence时竟然不会加自己的from在packet里面，诡异。
                    // 那谁知道你是谁啊？还怎么回复好友申请啊？头大。
                    // 所以这里还得自己拦截一下包，看有没有from，如果没有就要自己加上from=username@192.168.191.1
                    // if condition是因为，如果是上面这种情况的包，处理完之后就不进行后面的处理了
                    SharedPreferences sp = getSharedPreferences("chatchat", Context.MODE_PRIVATE);
                    String username = sp.getString("username","admin");
                    Log.e("MYLOG-MAIN: ", presence.toString());
                    if (presence.getFrom()==null) {
                        try {
                            presence.setFrom((EntityBareJid) JidCreate.from(username + "@" + OpenfireConnector.IP));
                            Log.e("MYLOG-Change send message from: ", presence.getFrom().toString());
                        } catch (XmppStringprepException e) {
                            // e.printStackTrace();
                        }
                        ////////////////////////////////////////////////////////////////////////
                        // 只处理发给自己的信息，再判断一下
                        // } else if (presence.getTo().toString().equals(username+"@"+ OpenfireConnector.IP)) {
                    } else {
                        Log.e("MYLOG-receive message from: ", presence.toString());
                        String from = presence.getFrom().toString();
                        if (presence.getType().equals(Presence.Type.subscribe)) {
                            Log.e("MYLOG7", "received a friend request");
                            acceptAdd = "receivedrequest";
                            Intent intent = new Intent();
                            intent.putExtra("response", "requestfriend");
                            intent.putExtra("fromName", from);
                            // 说明请求种类
                            intent.putExtra("acceptAdd", acceptAdd);
                            intent.setAction("com.weaverhong.lesson.chatchat.addfriend");
                            sendBroadcast(intent);
                        } else if (presence.getType().equals(Presence.Type.subscribed)) {
                            //发送广播传递response字符串
                            response = "you are now a friend of his/her!";
                            Intent intent = new Intent();
                            intent.putExtra("response", response);
                            intent.setAction("com.weaverhong.lesson.chatchat.addfriend");
                            sendBroadcast(intent);
                        } else if (presence.getType().equals(Presence.Type.unsubscribe)) {
                            //发送广播传递response字符串
                            response = "request rejected... you are deleted from his/her friends list";
                            Intent intent = new Intent();
                            intent.putExtra("response", response);
                            intent.setAction("com.weaverhong.lesson.chatchat.addfriend");
                            sendBroadcast(intent);
                        } else if (presence.getType().equals(Presence.Type.unsubscribed)) {

                        } else if (presence.getType().equals(Presence.Type.unavailable)) {
                            // System.out.println("好友下线！");
                        } else {
                            // System.out.println("好友上线！");
                        }
                    }
                }
            }
        };

        // 这几行好仿佛好像似乎都没用，只有最后一个能拦截到包
        // OpenfireConnector.sAbstractXMPPConnection.addSyncStanzaListener(sendlistener, stanzaFilter);
        // OpenfireConnector.sAbstractXMPPConnection.addAsyncStanzaListener(sendlistener, stanzaFilter);
        // OpenfireConnector.sAbstractXMPPConnection.addPacketListener(sendlistener, stanzaFilter);
        OpenfireConnector.sAbstractXMPPConnection.addPacketInterceptor(sendlistener, stanzaFilter);

        OpenfireConnector.sAbstractXMPPConnection.addPacketSendingListener(sendlistener, stanzaFilter);


    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 下面是Receiver处理Connection唤醒的Listener传出的Intent请求的过程
            Bundle bundle = intent.getExtras();
            // 先处理response，看一下是哪种（那五种presence-xml信息里面的哪一种）
            response = bundle.getString("response");
            Log.e("MYLOG7", "broadcast received:"+response);
            // text_response.setText(response);
            // 这里非空才表示要处理的是加好友的presence信息
            // 因为response空表示这是普通的上下线行为
            if (response.equals("requestfriend")) {
                acceptAdd = bundle.getString("acceptAdd");
                alertName = bundle.getString("fromName");
                // 这里name是openfire的带domain形式，比如admin@weaver或admin@192.168.191.1
                if (alertName != null)
                    alertSubName = alertName.substring(0, alertName.indexOf("@"));
                if (acceptAdd.equals("receivedrequest")) {
                    Log.e("MYLOG10", "successssss");

                    // NEW METHOD: store friends who are waiting to be added in DB or SP
                    // show them when calling updateUI(), in the friend list.

                    // NOTIFICATION: a important rule,
                    // Alertdialog is not allowed using here, INSIDE a broadcast intent receiver.

                    // AlertDialog.Builder builder  = new AlertDialog.Builder(getApplicationContext());
                    // builder.setTitle("Friend Request");
                    // builder.setMessage(""+alertSubName+"want to add you as a friend" );
                    // builder.setPositiveButton("yes",new DialogInterface.OnClickListener() {
                    //     @Override
                    //     public void onClick(DialogInterface dialog, int arg1) {
                    //         try {
                    //             // 到这就结束了加好友的流程
                    //             // 相当于前面经历了A createFriend ----> B replyfriendapply ---(true)---> B createFriend这几步
                    //             boolean result = OpenfireConnector.replyfriendapply(alertName, false);
                    //             if (result) addFriend(OpenfireConnector.getRoster(), alertSubName, alertSubName);
                    //             else return;
                    //         } catch (Exception e) {
                    //             // e.printStackTrace();
                    //         }
                    //     }
                    // });
                    // builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                    //     @Override
                    //     public void onClick(DialogInterface dialog, int arg1) {
                    //         try {
                    //             boolean result = OpenfireConnector.replyfriendapply(alertName, true);
                    //         } catch (Exception e) {
                    //             // e.printStackTrace();
                    //         }
                    //     }
                    // });
                    // builder.show();

                }
            }
        }
    }

    public boolean addFriend(Roster roster, String friendName, String name) throws Exception {
        try {
            EntityBareJid jid = JidCreate.entityBareFrom(friendName.trim()+"@"+ DOMAIN);
            // 这里因为版本原因，网上原先的解决方法第三个参数用null也可，表示不分组的朋友
            // 通过管理员添加貌似也可null
            // 但是smack现在好像必须加分组了，null就添加不上
            roster.createEntry(jid, name, new String[] {"friends"});
            Log.e("MYLOG8","add friend success");
            return true;
        } catch (XMPPException e) {
            Log.e("MYLOG8",e.toString());
            Log.e("MYLOG8","add friend fail");
            return false;
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }

}
