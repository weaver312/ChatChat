package com.weaverhong.lesson.chatchat.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.weaverhong.lesson.chatchat.Activity_Autoshutdown.BaseActivity;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

public class LoginActivity extends BaseActivity {

    EditText mEditTextusername;
    EditText mEditTextpassword;
    Button mButtonlogin;
    Button mButtongotoregist;
    Context mContext;

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, LoginActivity.class);
        return intent;
    }

    public static Intent newIntent(Context packageContext, String username) {
        Intent intent = new Intent(packageContext, LoginActivity.class);
        intent.putExtra("username", username);
        return intent;
    }

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;

        mEditTextusername = findViewById(R.id.login_username);
        mEditTextpassword = findViewById(R.id.login_password);
        mButtonlogin = findViewById(R.id.loginbutton);
        mButtongotoregist = findViewById(R.id.gotoregistbutton);
        mEditTextusername.setText(getIntent().getStringExtra("username"));

        mButtonlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mEditTextusername.getText().toString();
                String password = mEditTextpassword.getText().toString();
                // 登录，如果登录成功则跳转，失败就提示是没有用户名还是密码不对，return
                // 新版本，好像不用这些了，在OpenfireConnector里写两个listener就可以了
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 新版本，终于不用下面的一堆监听器了，都在buildConn方法里解决了，记录到静态区
                            // 方便各个类公用，也省得到处发intent了
                            OpenfireConnector.buildConn(mContext);
                            // 这些方法放到login之前和buildConn之后，是因为一旦login成功，就会开始接受消息，所以必须先注册监听器才能拦截所有消息
                            // 那个preference获取用户名，可能会导致一些错误的包被发出去，这些包被我标记为字符串null
                            // 但是没关系，也不影响消息。因为我加字符串null信息都是这些消息原本是空的情况下加的，这些信息原本就没有用，我加完了最差情况也只是冗余信息。
                            // 不会对消息的理解产生歧义。
                            // StanzaFilter stanzaFilter = new AndFilter(new StanzaTypeFilter(Presence.class));
                            // StanzaFilter stanzaFilter_msg = new AndFilter(new StanzaTypeFilter(Message.class));
                            // // PacketFilter packetFilter = (PacketFilter) new AndFilter(new PacketTypeFilter(Presence.class));
                            // StanzaListener sendlisteners[] = new StanzaListener[10];
                            // for (int i = 0; i < 10; i++) {
                            //     int finalI = i;
                            //     sendlisteners[i] = new StanzaListener() {
                            //         @Override
                            //         public void processStanza(Stanza packet) {
                            //             Log.e("LoginActivity", "this is listener " + finalI);
                            //             if (packet instanceof Presence) {
                            //                 Presence presence = (Presence) packet;
                            //                 // 这里真的佛了，这个版本的smack发送presence时竟然不会加自己的from在packet里面，诡异。
                            //                 // 那谁知道你是谁啊？还怎么回复好友申请啊？头大。
                            //                 // 所以这里还得自己拦截一下包，看有没有from，如果没有就要自己加上from=username@192.168.191.1
                            //                 // if condition是因为，如果是上面这种情况的包，处理完之后就不进行后面的处理了
                            //                 SharedPreferences sp = getSharedPreferences("chatchat", Context.MODE_PRIVATE);
                            //                 // 下面这行这就是冗余的信息
                            //                 // 而且我是每次都从sp里获取，毕竟加好友的次数又不多，从sp里读对系统影响也不大。
                            //                 // 这样能保证null在后面可以变成正确的信息。
                            //                 String username = sp.getString("username", "null");
                            //
                            //                 // Log.e("MYLOG-MAIN: ", presence.toString());
                            //                 String acceptAdd, response;
                            //                 if (presence.getFrom() == null) {
                            //                     try {
                            //                         presence.setFrom((EntityBareJid) JidCreate.from(username + "@" + OpenfireConnector.IP));
                            //                         // Log.e("MYLOG-Send from: ", presence.getFrom().toString());
                            //                     } catch (XmppStringprepException e) {
                            //                         // e.printStackTrace();
                            //                     }
                            //                     ////////////////////////////////////////////////////////////////////////
                            //                     // 只处理发给自己的信息，再判断一下
                            //                     // } else if (presence.getTo().toString().equals(username+"@"+ OpenfireConnector.IP)) {
                            //                 } else {
                            //                     Log.e("LoginActivity-receive presence: ", presence.toString());
                            //                     String from = presence.getFrom().toString();
                            //                     if (presence.getType().equals(Presence.Type.subscribe)) {
                            //                         // Log.e("MYLOG7", "received a friend request");
                            //                         acceptAdd = "receivedrequest";
                            //                         Intent intent = new Intent();
                            //                         intent.putExtra("response", "requestfriend");
                            //                         intent.putExtra("fromName", from);
                            //                         // 说明请求种类
                            //                         intent.putExtra("acceptAdd", acceptAdd);
                            //                         intent.setAction("com.weaverhong.lesson.chatchat.addfriend");
                            //                         sendBroadcast(intent);
                            //                     } else if (presence.getType().equals(Presence.Type.subscribed)) {
                            //                         //发送广播传递response字符串
                            //                         response = "you are now a friend of his/her!";
                            //                         Intent intent = new Intent();
                            //                         intent.putExtra("response", response);
                            //                         intent.setAction("com.weaverhong.lesson.chatchat.addfriend");
                            //                         sendBroadcast(intent);
                            //
                            //                     } else if (presence.getType().equals(Presence.Type.unsubscribe)) {
                            //                         //发送广播传递response字符串
                            //                         response = "request rejected... you are deleted from his/her friends list";
                            //                         Intent intent = new Intent();
                            //                         intent.putExtra("response", response);
                            //                         intent.setAction("com.weaverhong.lesson.chatchat.addfriend");
                            //                         sendBroadcast(intent);
                            //                     } else if (presence.getType().equals(Presence.Type.unsubscribed)) {
                            //
                            //                     } else if (presence.getType().equals(Presence.Type.unavailable)) {
                            //                         // System.out.println("好友下线！");
                            //                     } else {
                            //                         // System.out.println("好友上线！");
                            //                     }
                            //                 }
                            //             } else if (packet instanceof Message) {
                            //                 Message message = (Message) packet;
                            //                 Log.e("LoginActivity-receive message: ", message.toXML().toString());
                            //                 // 和Presence不同，这里不处理发送出去的消息，毕竟登录完了才能发消息
                            //                 // 所以这里不判断from是不是空的（不处理发出去的Message，根据IDEA里的test，Message自带正确的to和from）
                            //                 // 而且这里username也是从内存里获取，比较快。
                            //                 String username = OpenfireConnector.username;
                            //                 String from = message.getFrom().toString();
                            //
                            //                 Intent intent = new Intent();
                            //                 intent.putExtra("response", "message");
                            //
                            //                 // 注意这里的时间，因为依赖发送方的时间，所以不是System.currenttimemillis()
                            //                 // JivePropertiesExtension extension = (JivePropertiesExtension) packet.getExtension(JivePropertiesExtension.NAMESPACE);
                            //                 // String createtime = (String) extension.getProperty("CREATETIME");
                            //                 // Log.e("MSGLOG-CREATETIME", createtime);
                            //                 // 上面获时间的方法不太好用，观察了下xmpp包，发现它附带时间，也是Text格式的
                            //                 Log.e("MEG:", packet.toXML().toString());
                            //                 // ExtensionElement ee = message.getExtension("urn:xmpp:delay");
                            //                 // Log.e("MEG:", ee.toXML().toString());
                            //                 String createtime = "" + System.currentTimeMillis();
                            //
                            //                 intent.putExtra("MSGTRANID", message.getStanzaId());
                            //                 intent.putExtra("SENDERNAME", from.split("@")[0]);
                            //                 intent.putExtra("RECEIVERNAME", username);
                            //                 intent.putExtra("CREATETIME", createtime);
                            //                 intent.putExtra("CONTENT", message.getBody());
                            //
                            //                 // 说明请求种类
                            //                 intent.setAction("com.weaverhong.lesson.chatchat.newmessage");
                            //                 // send a intent to chatactivity to refresh chat data
                            //                 // maybe send a intent to mainactivity to refresh chat data
                            //                 // & notify the chatlistfragment in mainactivity that data has been updated
                            //                 sendBroadcast(intent);
                            //             }
                            //         }
                            //     };
                            // }
                            // // 这几行非常讨厌，不知道哪个能拦截到包，只好全装上。这回就是收发所有的包都会被拦截了
                            // OpenfireConnector.sAbstractXMPPConnection.addSyncStanzaListener(sendlisteners[0], stanzaFilter);
                            // OpenfireConnector.sAbstractXMPPConnection.addAsyncStanzaListener(sendlisteners[1], stanzaFilter);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketListener(sendlisteners[2], stanzaFilter);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketInterceptor(sendlisteners[3], stanzaFilter);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketSendingListener(sendlisteners[4], stanzaFilter);
                            //
                            // OpenfireConnector.sAbstractXMPPConnection.addSyncStanzaListener(sendlisteners[5], stanzaFilter_msg);
                            // OpenfireConnector.sAbstractXMPPConnection.addAsyncStanzaListener(sendlisteners[6], stanzaFilter_msg);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketListener(sendlisteners[7], stanzaFilter_msg);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketInterceptor(sendlisteners[8], stanzaFilter_msg);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketSendingListener(sendlisteners[9], stanzaFilter_msg);
                            // ///////////////////////////////////////////////////////////////////////////////////////////////////////
                            OpenfireConnector.login(username, password);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("LoginActivity-login", e.toString());
                        }
                        try {
                            if (OpenfireConnector.isAuthenticated()) {
                                Looper.prepare();
                                OpenfireConnector.username = username;
                                Toast.makeText(LoginActivity.this, "Yeah, login success!", Toast.LENGTH_LONG).show();
                                SharedPreferences sp = getSharedPreferences("chatchat", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("username", username);
                                editor.putString("password", password);
                                editor.putLong("lastlogintime", System.currentTimeMillis());
                                editor.apply();
                                Intent intent = MainActivity.newIntent(LoginActivity.this);
                                startActivity(intent);
                                Looper.loop();
                            } else {
                                if (OpenfireConnector.isConnected()) {
                                    Looper.prepare();
                                    Toast.makeText(LoginActivity.this, "login fail!", Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                } else {
                                    Looper.prepare();
                                    Toast.makeText(LoginActivity.this, "God, connect fail", Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                }
                            }
                        } catch (Exception e) {
                            // Log.e("LoginActivity-authenticate", e.toString());
                        }
                    }
                }).start();
            }
        });
        mButtongotoregist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转去RegistActivity，获取注册的用户名返回到loginActivity
                Intent intent = RegistActivity.newIntent(LoginActivity.this);
                startActivityForResult(intent, 1);
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
                    "press to exit", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            Intent intent = new Intent();
            intent.setAction(OpenfireConnector.EXIT_ALL);
            sendBroadcast(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1:
                mEditTextusername.setText(data.getStringExtra("username") == null ? "" : data.getStringExtra("username"));
                break;
            default:
                return;
        }
    }
}
