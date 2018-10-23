package com.weaverhong.lesson.chatchat;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;

public class OpenfireConnector {

    public static AbstractXMPPConnection sAbstractXMPPConnection;
    public static final String IP = "192.168.191.1";
    public static final String DOMAIN = "192.168.191.1";
    // private static ChatManager sChatManager;
    private static AccountManager accountManager;
    public static String username = "";

    static {
        try {
            buildConn();
        } catch (Exception e) {
            Log.e("MYLOG", e.toString());
        }
    }

    public static void buildConn() throws Exception {
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
        sAbstractXMPPConnection.connect();
    }

    public static void breakConn() {
        sAbstractXMPPConnection.disconnect();
    }

    public static boolean login(String username1, String password) throws Exception {
        if (!sAbstractXMPPConnection.isConnected())
            buildConn();
        sAbstractXMPPConnection.login(username1, password);
        // sChatManager.addIncomingListener(new IncomingChatMessageListener() {
        //     @Override
        //     public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        //
        //     }
        // });
        boolean you = sAbstractXMPPConnection.isAuthenticated();
        if (you) {
            username = username1;
            // getRoster().setSubscriptionMode(Roster.SubscriptionMode.reject_all);
        }
        return you;
    }

    public static void sendmessage(String address, String message) throws Exception {
        EntityBareJid jid = JidCreate.entityBareFrom(address+"@"+DOMAIN);
        Chat chat = getChatManager().chatWith(jid);
        // 比较简单的默认发送方式
        chat.send(message);
    }

    public static void sendmessage(String address, String message, Map<String, String> args) throws Exception {
        EntityBareJid jid = JidCreate.entityBareFrom(address);
        Chat chat = getChatManager().chatWith(jid);
        // 自定义的，比较复杂的方式
        Message msg = new Message();
        msg.setBody(message);
        for (Map.Entry<String, String> e: args.entrySet()) {
            JivePropertiesManager.addProperty(msg, e.getKey(), e.getValue());
        }
        chat.send(msg);
    }

    public static boolean isAuthenticated() {
        return sAbstractXMPPConnection.isAuthenticated();
    }

    public static boolean isConnected() {
        return sAbstractXMPPConnection.isConnected();
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

    public static boolean replyfriendapply(String alertName, boolean refused) throws SmackException.NotConnectedException, InterruptedException {
        Presence presenceRes;
        if (refused)
            presenceRes = new Presence(Presence.Type.unsubscribe);
        else
            presenceRes = new Presence(Presence.Type.subscribed);
        try {
            presenceRes.setTo(JidCreate.entityBareFrom(alertName+"@"+OpenfireConnector.DOMAIN));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        sAbstractXMPPConnection.sendStanza(presenceRes);
        return true;
    }

    public static Roster getRoster() {
        return Roster.getInstanceFor(sAbstractXMPPConnection);
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

    private static ChatManager getChatManager() {
        return ChatManager.getInstanceFor(sAbstractXMPPConnection);
    }
}
