package com.weaverhong.lesson.chatchat;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.net.InetAddress;

import javax.net.ssl.HostnameVerifier;

public class OpenfireConnector {

    private static AbstractXMPPConnection sAbstractXMPPConnection;
    private static final String IP = "192.168.191.1";
    private static final String DOMAIN = "weaver";
    private static ChatManager sChatManager;

    static {
        try {
            buildConn();
        } catch (Exception e) { }
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
                .build();
        sAbstractXMPPConnection = new XMPPTCPConnection(config);
        sAbstractXMPPConnection.connect();
    }

    public static void breakConn() {
        sAbstractXMPPConnection.disconnect();
    }

    public static void login(String username, String password) throws Exception {
        if (!sAbstractXMPPConnection.isConnected())
            sAbstractXMPPConnection.connect();
        sAbstractXMPPConnection.login(username, password);
        sChatManager = ChatManager.getInstanceFor(sAbstractXMPPConnection);
        sChatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {

            }
        });
    }

    public static void sendmessage(String address, String message) throws Exception {
        EntityBareJid jid = JidCreate.entityBareFrom(address);
        Chat chat = sChatManager.chatWith(jid);
        // 比较简单的默认发送方式
        chat.send(message);
        // 自定义的，比较复杂的方式
        // Message msg = new Message();
        // msg.setBody("Hello world again!");
        // JivePropertiesManager.addProperty(msg, "favoriteColor", "red");
        // chat.send(msg);
    }

    public static boolean isAuthenticated() {
        return sAbstractXMPPConnection.isAuthenticated();
    }

    public static boolean isConnected() {
        return sAbstractXMPPConnection.isConnected();
    }
}
