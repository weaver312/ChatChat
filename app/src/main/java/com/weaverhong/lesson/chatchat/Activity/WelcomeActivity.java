package com.weaverhong.lesson.chatchat.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.weaverhong.lesson.chatchat.OpenfireConnector.DOMAIN;

public class WelcomeActivity extends Activity {

    Handler mHandler;
    private String name,password,response,acceptAdd,alertName,alertSubName;
    MyReceiver myReceiver = new MyReceiver();

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_welcome);
        mHandler = new Handler();
        // 这里检查是否登录过，登录状态可通过String的Token存放在SharedPreference中

        // 如果登录状态，则直接去MainActivity

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sp = getSharedPreferences("chatchat",MODE_PRIVATE);
                    Long lastlogintime = sp.getLong("lastlogintime", 0);
                    Long currenttime = System.currentTimeMillis();
                    // 持续登录60秒
                    if (currenttime-lastlogintime>(1000*60)) {
                        // Toast.makeText(WelcomeActivity.this,"login out-dated!", Toast.LENGTH_SHORT);
                        Intent intent = LoginActivity.newIntent(WelcomeActivity.this);
                        startActivity(intent);
                    } else {
                        String username = sp.getString("username", "admin");
                        String password = sp.getString("password", "null");
                        boolean result = OpenfireConnector.login(username, password);
                        if (!result) {
                            // login fail, jump to loginactivity
                            Intent intent = LoginActivity.newIntent(WelcomeActivity.this);
                            startActivity(intent);
                        } else {
                            // login success, jump to main activity
                            Intent intent = MainActivity.newIntent(WelcomeActivity.this);
                            startActivity(intent);
                        }
                    }
                } catch (Exception e) {
                    Log.e("MYLOG", e.toString());
                }
            }
        }).start();


        // 临时的intent监听器
        // 注册之后，就可以在MyReceiver中弹出Dialog，对intentFilter的Action及时做出反应，比较轻量
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.weaverhong.lesson.chatchat.addfriend");
        registerReceiver(myReceiver,intentFilter);

        // 过滤器和监听器，让XMPPConnextion可以在收到packet/stanza时及时反馈召唤出Intent
        StanzaFilter stanzaFilter = new AndFilter(new StanzaTypeFilter(Presence.class));
        StanzaListener recvlistener = new StanzaListener() {
            @Override
            public void processStanza(Stanza packet) {
                // System.out.println("PresenceService-"+packet.toXML());
                if(packet instanceof Presence){
                    Presence presence = (Presence)packet;
                    // 这里真的佛了，这个版本的smack发送presence时竟然不会加自己的from在packet里面，诡异。
                    // 那谁知道你是谁啊？还怎么回复好友申请啊？头大。
                    // 所以这里还得自己拦截一下包，看有没有from，如果没有就要自己加上from=username@192.168.191.1
                    // if condition是因为，如果是上面这种情况的包，处理完之后就不进行后面的处理了
                    SharedPreferences sp = getSharedPreferences("chatchat", Context.MODE_PRIVATE);
                    String username = sp.getString("username","admin");
                    if (presence.getFrom()==null) {
                        // try {
                        //     presence.setFrom((EntityBareJid) JidCreate.from(username+"@"+OpenfireConnector.IP));
                        // } catch (XmppStringprepException e) {
                        //     // e.printStackTrace();
                        // }
                        ////////////////////////////////////////////////////////////////////////
                    } else {
                        Log.e("MYLOG0", presence.toString());
                        String from = presence.getFrom().toString();
                        // String to = presence.getTo().toString();
                        // 这里是首先被执行的逻辑
                        // 发送广播说明好友的一些情况，根据官网文档，这里有五种presence包
                        // 根据包的种类，下面会发出一些intent，携带不同的信息
                        // Receiver会收到并进行处理
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
                    if (presence.getFrom()==null) {
                        try {
                            presence.setFrom((EntityBareJid) JidCreate.from(username+"@"+OpenfireConnector.IP));
                        } catch (XmppStringprepException e) {
                            // e.printStackTrace();
                        }
                        ////////////////////////////////////////////////////////////////////////
                    } else {
                    }
                }
            }
        };

        OpenfireConnector.sAbstractXMPPConnection.addPacketInterceptor(recvlistener, stanzaFilter);
        OpenfireConnector.sAbstractXMPPConnection.addPacketSendingListener(sendlistener, stanzaFilter);

    }

    private static String fromInputStreamtoString(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        String state = os.toString();
        os.close();
        return state;
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
                    AlertDialog.Builder builder  = new AlertDialog.Builder(getApplicationContext());
                    builder.setTitle("Friend Request");
                    builder.setMessage(""+alertSubName+"want to add you as a friend" );
                    builder.setPositiveButton("yes",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            try {
                                // 到这就结束了加好友的流程
                                // 相当于前面经历了A createFriend ----> B replyfriendapply ---(true)---> B createFriend这几步
                                boolean result = OpenfireConnector.replyfriendapply(alertName, false);
                                if (result) addFriend(OpenfireConnector.getRoster(), alertSubName, alertSubName);
                                else return;
                            } catch (Exception e) {
                                // e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            try {
                                boolean result = OpenfireConnector.replyfriendapply(alertName, true);
                            } catch (Exception e) {
                                // e.printStackTrace();
                            }
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    // 这个逻辑应该写到OpenfireConnector里面的
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

}
