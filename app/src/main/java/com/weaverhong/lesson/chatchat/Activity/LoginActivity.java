package com.weaverhong.lesson.chatchat.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class LoginActivity extends Activity {

    EditText mEditTextusername;
    EditText mEditTextpassword;
    Button mButtonlogin;
    Button mButtongotoregist;
    Handler mHandler;

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
        String username = null;

        try {username = onSavedInstanceState.getString("username"); } catch (Exception e) {}

        mHandler = new Handler();

        mEditTextusername = findViewById(R.id.login_username);
        mEditTextpassword = findViewById(R.id.login_password);
        mButtonlogin = findViewById(R.id.loginbutton);
        mButtongotoregist = findViewById(R.id.gotoregistbutton);



        if (username!=null) mEditTextusername.setText(username);

        mButtonlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mEditTextusername.getText().toString();
                String password = mEditTextpassword.getText().toString();
                // 登录，如果登录成功则跳转，失败就提示是没有用户名还是密码不对，return
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // OpenfireConnector.breakConn();
                            OpenfireConnector.buildConn();

                            // 这些方法放到login之前和buildConn之后，是因为一旦login成功，就会开始接受消息，所以必须先注册监听器才能拦截所有消息
                            // 那个preference获取用户名，可能会导致一些错误的包被发出去，这些包被我标记为字符串null
                            // 但是没关系，也不影响消息。因为我加字符串null信息都是这些消息原本是空的情况下加的，这些信息原本就没有用，我加完了最差情况也只是冗余信息。
                            // 不会对消息的理解产生歧义。
                            StanzaFilter stanzaFilter = new AndFilter(new StanzaTypeFilter(Presence.class));
                            StanzaFilter stanzaFilter_msg = new AndFilter(new StanzaTypeFilter(Message.class));
                            // PacketFilter packetFilter = (PacketFilter) new AndFilter(new PacketTypeFilter(Presence.class));
                            StanzaListener sendlistener = new StanzaListener() {
                                @Override
                                public void processStanza(Stanza packet) {
                                    if(packet instanceof Presence) {
                                        Presence presence = (Presence)packet;
                                        // 这里真的佛了，这个版本的smack发送presence时竟然不会加自己的from在packet里面，诡异。
                                        // 那谁知道你是谁啊？还怎么回复好友申请啊？头大。
                                        // 所以这里还得自己拦截一下包，看有没有from，如果没有就要自己加上from=username@192.168.191.1
                                        // if condition是因为，如果是上面这种情况的包，处理完之后就不进行后面的处理了
                                        SharedPreferences sp = getSharedPreferences("chatchat", Context.MODE_PRIVATE);
                                        // 下面这行这就是冗余的信息
                                        // 而且我是每次都从sp里获取，毕竟加好友的次数又不多，从sp里读对系统影响也不大。
                                        // 这样能保证null在后面可以变成正确的信息。
                                        String username = sp.getString("username","null");

                                        // Log.e("MYLOG-MAIN: ", presence.toString());
                                        String acceptAdd, response;
                                        if (presence.getFrom()==null) {
                                            try {
                                                presence.setFrom((EntityBareJid) JidCreate.from(username + "@" + OpenfireConnector.IP));
                                                // Log.e("MYLOG-Send from: ", presence.getFrom().toString());
                                            } catch (XmppStringprepException e) {
                                                // e.printStackTrace();
                                            }
                                            ////////////////////////////////////////////////////////////////////////
                                            // 只处理发给自己的信息，再判断一下
                                            // } else if (presence.getTo().toString().equals(username+"@"+ OpenfireConnector.IP)) {
                                        } else {
                                            Log.e("MYLOG-receive presence: ", presence.toString());
                                            String from = presence.getFrom().toString();
                                            if (presence.getType().equals(Presence.Type.subscribe)) {
                                                // Log.e("MYLOG7", "received a friend request");
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
                                    } else if (packet instanceof Message) {
                                        Message message = (Message)packet;
                                        Log.e("MYLOG-receive message: ", message.toXML().toString());
                                        // 和Presence不同，这里不处理发送出去的消息，毕竟登录完了才能发消息
                                        // 所以这里不判断from是不是空的（不处理发出去的Message，根据IDEA里的test，Message自带正确的to和from）
                                        // 而且这里username也是从内存里获取，比较快。
                                        String username = OpenfireConnector.username;
                                        String from = message.getFrom().toString();

                                        Intent intent = new Intent();
                                        intent.putExtra("response", "message");

                                        // 注意这里的时间，因为依赖发送方的时间，所以不是System.currenttimemillis()
                                        // JivePropertiesExtension extension = (JivePropertiesExtension) packet.getExtension(JivePropertiesExtension.NAMESPACE);
                                        // String createtime = (String) extension.getProperty("CREATETIME");
                                        // Log.e("MSGLOG-CREATETIME", createtime);
                                        // 上面获时间的方法不太好用，观察了下xmpp包，发现它附带时间，也是Text格式的
                                        Log.e("MEG:", packet.toXML().toString());
                                        // ExtensionElement ee = message.getExtension("urn:xmpp:delay");
                                        // Log.e("MEG:", ee.toXML().toString());
                                        String createtime = ""+System.currentTimeMillis();

                                        intent.putExtra("MSGTRANID", message.getStanzaId());
                                        intent.putExtra("SENDERNAME", from.split("@")[0]);
                                        intent.putExtra("RECEIVERNAME", username);
                                        intent.putExtra("CREATETIME", createtime);
                                        intent.putExtra("CONTENT", message.getBody());

                                        // 说明请求种类
                                        intent.setAction("com.weaverhong.lesson.chatchat.newmessage");
                                        // send a intent to chatactivity to refresh chat data
                                        // maybe send a intent to mainactivity to refresh chat data
                                        // & notify the chatlistfragment in mainactivity that data has been updated
                                        sendBroadcast(intent);
                                    }
                                }
                            };
                            // 这几行非常讨厌，不知道哪个能拦截到包，只好全装上。这回就是收发所有的包都会被拦截了
                            OpenfireConnector.sAbstractXMPPConnection.addSyncStanzaListener(sendlistener, stanzaFilter);
                            // OpenfireConnector.sAbstractXMPPConnection.addAsyncStanzaListener(sendlistener, stanzaFilter);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketListener(sendlistener, stanzaFilter);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketInterceptor(sendlistener, stanzaFilter);
                            OpenfireConnector.sAbstractXMPPConnection.addPacketSendingListener(sendlistener, stanzaFilter);

                            OpenfireConnector.sAbstractXMPPConnection.addSyncStanzaListener(sendlistener, stanzaFilter_msg);
                            // OpenfireConnector.sAbstractXMPPConnection.addAsyncStanzaListener(sendlistener, stanzaFilter_msg);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketListener(sendlistener, stanzaFilter_msg);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketInterceptor(sendlistener, stanzaFilter_msg);
                            // OpenfireConnector.sAbstractXMPPConnection.addPacketSendingListener(sendlistener, stanzaFilter_msg);
                            ///////////////////////////////////////////////////////////////////////////////////////////////////////

                            OpenfireConnector.login(username, password);
                        } catch (Exception e) {
                            Log.e("MYLOG1", e.toString());
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
                                editor.commit();
                                Intent intent = MainActivity.newIntent(LoginActivity.this);
                                startActivity(intent);
                                Looper.loop();
                            } else {
                                if (OpenfireConnector.isConnected()) {
                                    Looper.prepare();
                                    Toast.makeText(LoginActivity.this, "login fail!", Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                }
                                else {
                                    Looper.prepare();
                                    Toast.makeText(LoginActivity.this, "God, connect fail", Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("MYLOG2", e.toString());
                        }
                    }
                }).start();
            }
        });
        mButtongotoregist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转去RegistActivity，
                Intent intent = RegistActivity.newIntent(LoginActivity.this);
                startActivityForResult(intent, 1);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode){
            case 1:
                mEditTextusername.setText(data.getStringExtra("username")==null?"":data.getStringExtra("username"));
                break;
            default:
                return;
        }
    }


}
