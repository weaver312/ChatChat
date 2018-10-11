package com.weaverhong.lesson.chatchat;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.net.InetAddress;

import javax.net.ssl.HostnameVerifier;

public class OpenfireConnector {

    private static final OpenfireConnector mOpenfireConnector = new OpenfireConnector();
    private static AbstractXMPPConnection sAbstractXMPPConnection;
    private static final String IP = "127.0.0.1";
    private static final String DOMAIN = "weaver";
    private static ChatManager sChatManager;

    private OpenfireConnector() {
        try {
            buildConn();
        } catch (Exception e) { }
    }

    public void buildConn() throws Exception{
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
    }

    public void breakConn() {
        sAbstractXMPPConnection.disconnect();
    }

    public void login(String username, String password) throws Exception{
        sAbstractXMPPConnection.connect();
        sAbstractXMPPConnection.login(username, password);
        sChatManager = ChatManager.getInstanceFor(sAbstractXMPPConnection);

    }
}
