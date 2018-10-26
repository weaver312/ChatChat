package com.weaverhong.lesson.chatchat;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.weaverhong.lesson.chatchat.DB.ContactDBManager;
import com.weaverhong.lesson.chatchat.DB.MessageDBManager;
import com.weaverhong.lesson.chatchat.Entity.MessageEntity;
import com.weaverhong.lesson.chatchat.Entity.UserEntity;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;

public class OpenfireConnector {

    public static final int USER_LEFT = 0;
    public static final int USER_RIGHT = 1;
    public static final String XMLTAG_TIME = "TIMESTAMP";
    public static final String IP = "192.168.191.1";
    public static final String DOMAIN = "192.168.191.1";
    public static final String EXIT_ALL = "com.weaverhong.chatchat.EXIT_ALL";

    public static AbstractXMPPConnection sAbstractXMPPConnection;
    private static AccountManager accountManager;
    private static ChatManager sChatManager;
    private static Roster sRoster;

    public static String username = "";

    static {
        try {
            // buildConn();
        } catch (Exception e) {
            Log.e("OpenfireConnector-static", e.toString());
        }
    }

    public static void buildConn(Context context) throws Exception {
        try {
            InetAddress addr = InetAddress.getByName(IP);
            HostnameVerifier verifier = (arg0, arg1) -> false;
            DomainBareJid serviceName = JidCreate.domainBareFrom(DOMAIN);
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setPort(5222)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setXmppDomain(serviceName)
                    .setHostnameVerifier(verifier)
                    .setHostAddress(addr)
                    .setDebuggerEnabled(true)
                    .build();
            sAbstractXMPPConnection = new XMPPTCPConnection(config);

            sChatManager = ChatManager.getInstanceFor(sAbstractXMPPConnection);
            sChatManager.addIncomingListener(new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                    // 对Message的处理过程：
                    // 1. 写数据库
                    MessageDBManager messageDBManager = new MessageDBManager(context);
                    MessageEntity item = new MessageEntity();

                    try {
                        ExtensionElement ee = message.getExtension("urn:xmpp:delay");
                        item.setSendername(message.getFrom().toString().split("[/@]")[0]);
                        item.setReceivername(message.getTo().toString().split("[/@]")[0]);
                        // System.out.println(ee.toXML().toString());
                        String time_noformat = ee.toXML().toString().split("'")[3];
                        String timestamp_str = time_noformat.split("T")[0] + " " + time_noformat.split("[T\\.]")[1];
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date;
                        date = df.parse(timestamp_str);
                        // 因为是东八区，所以得手动加八个小时的毫秒时间
                        Long timestamp = Long.valueOf(0);
                        timestamp = date.getTime() + Long.valueOf(time_noformat.split("[\\.\\+Z]")[1]) + 8*60*60*1000;
                        item.setCreatetime(""+timestamp);
                    } catch (Exception e) {
                        // e.printStackTrace();
                        System.out.println("realtime message! no XML!");
                        // 说明不是delay的消息，属于即时的消息，需要取出time标签才行
                        try {
                            System.out.println(message.toXML().toString());
                            item.setCreatetime(message.toXML().toString().split("<name>mytimestamp</name><value type='long'>")[1].split("</value></property>")[0]);
                        } catch (Exception ee) {
                            ee.printStackTrace();
                            System.out.println("FATAL MISTAKE! ");
                        }
                    }
                    item.setContent(message.getBody());
                    item.setDirection(USER_LEFT);
                    item.setMsgtranid(message.getStanzaId());

                    messageDBManager.add(item);
                    // 2. 通知更新视图
                    Intent intent = new Intent();
                    intent.setAction("com.weaverhong.lesson.chatchat.newmessage");
                }
            });
            sChatManager.addOutgoingListener(new OutgoingChatMessageListener() {
                @Override
                public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
                    // 对Message的处理过程：
                    // 1. 写数据库
                    MessageDBManager messageDBManager = new MessageDBManager(context);
                    MessageEntity item = new MessageEntity();
                    item.setSendername(username);
                    item.setReceivername(message.getTo().toString().split("[/@]")[0]);
                    item.setCreatetime(System.currentTimeMillis() + "");
                    item.setContent(message.getBody());
                    item.setDirection(USER_RIGHT);
                    item.setMsgtranid(message.getStanzaId());

                    messageDBManager.add(item);
                    // 2. 通知ChatActivity更新视图
                    Intent intent = new Intent();
                    intent.putExtra("usernameleft", message.getTo().toString().split("/")[0]);
                    intent.putExtra("usernameright", username);
                    intent.setAction("com.weaverhong.lesson.chatchat.newmessage");
                }
            });

            sRoster = Roster.getInstanceFor(sAbstractXMPPConnection);
            sRoster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
            sRoster.addSubscribeListener(new SubscribeListener() {
                @Override
                public SubscribeAnswer processSubscribe(Jid from, Presence subscribeRequest) {
                    Log.e("OpenfireConnector-new friend request, JID: ", from.toString());
                    Log.e("OpenfireConnector-new friend request, CONTENT: ", subscribeRequest.toXML().toString());
                    // 自动加好友功能：
                    // 如果前面设置accept_all，所以相当于已经完成了单向好友
                    // 1. 把好友加到好友数据库里，要记得防止重复数据项
                    ContactDBManager contactDBManager = new ContactDBManager(context);
                    UserEntity item = new UserEntity();
                    item.setIfadded(0);
                    item.setJid(from.toString());
                    item.setUsername(from.toString().split("@")[0]);
                    contactDBManager.add(item);

                    // 2. 更新数据库后通知更新视图，增加新好友的项目
                    // 这个通过发广播让contactsFragment更新视图
                    Intent intent = new Intent();
                    intent.setAction("com.weaverhong.lesson.chatchat.addfriend");
                    context.sendBroadcast(intent);

                    // 下面的工作理应在contactsFragment里执行
                    // 3. 发一条问候消息给对方
                    // sendmessage(from.toString().split("@")[0],"You're already friends. Let's chat!");
                    // 4. 发起反向请求。
                    // addFriend(from.toString().split("@")[0]);
                    // 完成之后，只要对方一上线，就完成了双向好友了
                    return SubscribeAnswer.ApproveAndAlsoRequestIfRequired;
                }
            });

            // 设置自动重连，暂时还没尝试
            // ReconnectionManager reconnectionManager= ReconnectionManager.getInstanceFor(sAbstractXMPPConnection);
            // reconnectionManager.enableAutomaticReconnection();

            sAbstractXMPPConnection.connect();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OpenfireConnector-buildconn", e.getLocalizedMessage());
        }
    }
    public static void breakConn() {
        sRoster = null;
        sChatManager = null;
        username = null;
        accountManager = null;
        sAbstractXMPPConnection.disconnect();
    }

    public static boolean login(String username1, String password) throws Exception {
        // if (!sAbstractXMPPConnection.isConnected())
        //     buildConn();
        sAbstractXMPPConnection.login(username1, password);
        boolean you = sAbstractXMPPConnection.isAuthenticated();
        if (you) {
            username = username1;
            Log.e("Openfireconnector-login", "SUCCESS");
            // getRoster().setSubscriptionMode(Roster.SubscriptionMode.reject_all);
        }
        return you;
    }
    public static void sendmessage(String username, String message) throws Exception {
        EntityBareJid jid = JidCreate.entityBareFrom(username+"@"+DOMAIN);
        Chat chat = sChatManager.chatWith(jid);
        // 比较简单的默认发送方式
        chat.send(message);
    }
    public static void sendmessage(String username, String message, Map<String, String> args) throws Exception {
        EntityBareJid jid = JidCreate.entityBareFrom(username+"@"+DOMAIN);
        Chat chat = sChatManager.chatWith(jid);
        // 自定义的，比较复杂的方式
        Message msg = new Message();
        msg.setBody(message);
        for (Map.Entry<String, String> e: args.entrySet()) {
            JivePropertiesManager.addProperty(msg, e.getKey(), e.getValue());
        }
        chat.send(msg);
    }
    public static boolean addFriend(String friendName) throws Exception {
        try {
            EntityBareJid jid = JidCreate.entityBareFrom(friendName.trim()+"@"+ DOMAIN);
            // 这里因为版本原因，网上原先的解决方法第三个参数用null也可，表示不分组的朋友
            // 通过管理员添加貌似也可null
            // 但是smack现在好像必须加分组了，null就添加不上
            sRoster.createEntry(jid, friendName, new String[] {"friends"});
            return true;
        } catch (XMPPException e) {
            Log.e("Openfireconnector-addFriend",e.toString());
            return false;
        }
    }
    public static void deleteFriend(String friendName) throws Exception {
        try {
            EntityBareJid jid = JidCreate.entityBareFrom(friendName.trim()+"@"+ DOMAIN);
            sRoster.removeEntry(sRoster.getEntry(jid));
        } catch (XMPPException e) {
            Log.e("Openfireconnector-deleteFriend",e.toString());
        }
    }
    public static void regist(String username, String password, String email, String name) throws Exception{
        accountManager = AccountManager.getInstance(sAbstractXMPPConnection);
        // SSL很麻烦，没弄明白怎么配置，先这么用上
        accountManager.sensitiveOperationOverInsecureConnection(true);
        Map<String, String> attrs = new HashMap<>();
        attrs.put("email", email);
        // username暂时用来代理username，其实按理说username应该是qq号，因为在mysql表里有唯一性限制，而name才是昵称，可以随意命名。
        // 另外其他字段设置还没找到，以后再说
        attrs.put("name", username);
        accountManager.createAccount(Localpart.from(username), password,attrs);
    }

    public static boolean isAuthenticated() {
        return sAbstractXMPPConnection.isAuthenticated();
    }
    public static boolean isConnected() {
        return sAbstractXMPPConnection.isConnected();
    }
    public static Roster getRoster() {
        return Roster.getInstanceFor(sAbstractXMPPConnection);
    }
    public static List<UserEntity> getContactsFromServer() {
        List<UserEntity> entrieslist = new ArrayList<UserEntity>();
        RosterGroup rosterGroup = getRoster().getGroup("friends");
        Collection<RosterEntry> rosterEntry;
        if (rosterGroup!=null) {
            rosterEntry = rosterGroup.getEntries();

            Iterator<RosterEntry> i = rosterEntry.iterator();
            while (i.hasNext()) {
                RosterEntry re = i.next();
                UserEntity item = new UserEntity();
                item.setJid(re.getJid().toString());
                item.setUsername(re.getJid().toString().split("@")[0]);
                item.setIfadded(1);
                entrieslist.add(item);
            }
        }
        return entrieslist;
    }

    public static void replyfriendapply(String username, boolean refused) throws Exception {
        Presence presenceRes;
        if (refused)
            presenceRes = new Presence(Presence.Type.unsubscribe);
        else
            presenceRes = new Presence(Presence.Type.subscribed);
        try {
            presenceRes.setTo(JidCreate.entityBareFrom(username+"@"+OpenfireConnector.DOMAIN));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        sAbstractXMPPConnection.sendStanza(presenceRes);
    }
}
