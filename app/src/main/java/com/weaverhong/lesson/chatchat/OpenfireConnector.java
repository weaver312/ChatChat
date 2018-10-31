package com.weaverhong.lesson.chatchat;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;

import com.weaverhong.lesson.chatchat.DB.MessageDBManager;
import com.weaverhong.lesson.chatchat.Entity.MessageEntity;
import com.weaverhong.lesson.chatchat.Entity.UserEntity;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.StanzaCollector;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.id.StanzaIdUtil;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
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
    public static final String IP = "39.104.120.161";
    public static final String DOMAIN = "39.104.120.161";
    public static final String EXIT_ALL = "com.weaverhong.chatchat.EXIT_ALL";
    public static final String NEW_ADDFRIEND = "com.weaverhong.chatchat.NEW_ADDFRIEND";
    public static final int FRIENDTYPE_NONE = 0;
    public static final int FRIENDTYPE_BOTH = 1;
    public static final int FRIENDTYPE_FROM = 2;
    public static final int FRIENDTYPE_TO   = 3;
    public static final int FRIENDTYPE_ASK  = 4;
    public static Context currentContext;

    public static AbstractXMPPConnection sAbstractXMPPConnection;
    private static AccountManager accountManager;
    private static ChatManager sChatManager;
    private static Roster sRoster;
    private static UserSearchManager userSearchManger;

    public static String username = "";

    static {
        try {
            // buildConn();
        } catch (Exception e) {
            // Log.e("OpenfireConnector-static", e.toString());
        }
    }

    public static void buildConn(Context context) throws Exception {
        currentContext = context;
        try {
            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
            SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
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
            // 这句不能少，具体原因看此方法addIQProvider()的注释
            ProviderManager.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

            userSearchManger = new UserSearchManager(sAbstractXMPPConnection);

            accountManager = AccountManager.getInstance(sAbstractXMPPConnection);
            // SSL很麻烦，没弄明白怎么配置，先这么用上
            accountManager.sensitiveOperationOverInsecureConnection(true);

            // sAbstractXMPPConnection.addPacketSendingListener(new StanzaListener() {
            //     @Override
            //     public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
            //         if (packet instanceof Presence) {
            //             Presence presence = (Presence) packet;
            //             if (presence.getType().equals(Presence.Type.subscribe)) {
            //                 Log.e("Openfireconnector-sendpresence", "true");
            //                 // 如果是进行subscribe Presence回复，那么这条记录的发往者应该在数据库中有存留（依赖于调用它时前面有将这条记录写进本地sqlite）
            //                 // 其实这个逻辑依旧不太好看，还要查数据库
            //                 if (!presence.getTo().equals(username)
            //                         && new ContactDBManager(context).findByUsername(presence.getTo().toString().split("@")[0]) != null) {
            //                     Log.e("Openfireconnector-sendpresence", "positive");
            //                     presence.addExtension(new StandardExtensionElement("replyPositiveSubscribe","replyPositiveSubscribe"));
            //                     try {
            //                         Jid jid = JidCreate.from(username+"@"+DOMAIN);
            //                         // presence.setFrom(jid);
            //                     } catch (XmppStringprepException e) {
            //                         e.printStackTrace();
            //                     }
            //                     // presence.setPriority(120);
            //                     Log.e("Openfireconnector-sendpresence", presence.toXML().toString());
            //                 } else {
            //                     Log.e("Openfireconnector-sendpresence", "negative");
            //                 }
            //             }
            //         }
            //     }
            // }, new StanzaTypeFilter(Presence.class));

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
                        Long timestamp = date.getTime() + Long.valueOf(time_noformat.split("[\\.\\+Z]")[1]) + 8*60*60*1000;
                        item.setCreatetime(""+timestamp);
                    } catch (Exception e) {
                        // e.printStackTrace();
                        System.out.println("realtime message! no XML!");
                        // 说明不是delay的消息，属于即时的消息，需要取出time标签才行
                        try {
                            System.out.println(message.toXML().toString());
                            item.setCreatetime(message.toXML().toString().split("<name>mytimestamp</name><value type='string'>")[1].split("</value></property>")[0]);
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
                    currentContext.sendBroadcast(intent);
                    // Log.e("OpenfireConnector-MyReceiver", "BROADCAST SEND");
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
                    Log.e("message", message.toXML().toString());
                    item.setCreatetime(message.toXML().toString().split("<name>mytimestamp</name><value type='string'>")[1].split("</value>")[0]);
                    item.setContent(message.getBody());
                    item.setDirection(USER_RIGHT);
                    item.setMsgtranid(message.getStanzaId());

                    messageDBManager.add(item);
                    // 2. 通知ChatActivity更新视图
                    Intent intent = new Intent();
                    intent.putExtra("usernameleft", message.getTo().toString().split("/")[0]);
                    intent.putExtra("usernameright", username);
                    intent.setAction("com.weaverhong.lesson.chatchat.newmessage");
                    currentContext.sendBroadcast(intent);
                    // Log.e("OpenfireConnector-MyReceiver", "BROADCAST SEND");

                }
            });

            sRoster = Roster.getInstanceFor(sAbstractXMPPConnection);
            sRoster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
            if (sRoster.getGroup("friends") == null)
                sRoster.createGroup("friends");

            // sRoster.addSubscribeListener(new SubscribeListener() {
            //     @Override
            //     public SubscribeAnswer processSubscribe(Jid from, Presence subscribeRequest) {
            //         // 如果是取消订阅的话，最好处理一下。其实不处理也行
            //         if (subscribeRequest.getType().equals(Presence.Type.unsubscribe)) {
            //             try {
            //                 deleteFriend(from.toString().split("@")[0]);
            //                 return SubscribeAnswer.Approve;
            //             } catch (Exception e) {
            //                 e.printStackTrace();
            //             }
            //         }
            //         Log.e("OpenfireConnector-test", "pass");
            //         // if (subscribeRequest.getType().equals(Presence.Type.subscribe)
            //         //         && subscribeRequest.getTo().toString().split("@")[0].equals(username)) {
            //             // 对发给自己的好友请求才填到列表里，否则第一次发了请求，自己收到了又
            //             // 填进列表，岂不重复？
            //             // 如果是自己发出去的请求，自己收到了，应该不做处理，等待对方回应了答应或拒绝，
            //             // 就会有accept_all来处理，自动反向发起请求。
            //             // 这时对返来好友请求的处理方法，应该首先甄别是不是这个类型
            //             // 然后在sp里删除这条记录
            //             // Log.e("OpenfireConnector-new friend request, JID: ", from.toString());
            //             // Log.e("OpenfireConnector-new friend request, CONTENT: ", subscribeRequest.toXML().toString());
            //             // // 自动加好友功能：
            //             // // 如果前面设置accept_all，所以相当于已经完成了单向好友
            //             // // 1. 把好友加到好友数据库里，要记得防止重复数据项
            //             // // ContactDBManager contactDBManager = new ContactDBManager(context);
            //             // UserEntity item = new UserEntity();
            //             // item.setIfadded(0);
            //             // item.setJid(from.toString());
            //             // item.setUsername(from.toString().split("@")[0]);
            //             //
            //             // if (subscribeRequest.getExtension("replyPositiveSubscribe") != null) {
            //             //     // 说明是一条二次好友申请
            //             //     Log.e("Openfireconnector-reverse friend apply", "true");
            //             //     item.setIfadded(1);
            //             //     try {
            //             //         sendmessage(item.getUsername(), "We're already friends!");
            //             //     } catch (Exception e) {
            //             //         e.printStackTrace();
            //             //     }
            //             // }
            //
            //             // 发现还是把已添加用户和待通过用户分开存储比较好，如果放到一起很容易乱。
            //             // 这里暂存到SharedPreference里面，毕竟只有一个username就够了。
            //             // SharedPreferences sharedPreferences = context.getSharedPreferences("chatchat", Context.MODE_PRIVATE);
            //             // Set<String> users = sharedPreferences.getStringSet("notaddusers", new HashSet<>());
            //             // if (users.contains(item.getUsername()))
            //             //     // 说明这是一条返回过来的记录，应该直接添加，不需用户进行确认了
            //             //     item.setIfadded(1);
            //             // SharedPreferences.Editor editor = sharedPreferences.edit();
            //             // users.remove(item.getUsername());
            //             // editor.putStringSet("notaddusers", users);
            //             // editor.commit();
            //
            //             // 然后在DB里添加这条记录
            //             // new ContactDBManager(context).add(item);
            //
            //             // 最后在服务器端同意这个请求，让服务器变成both（return approve）
            //
            //             // 2. 更新数据库后通知更新视图，增加新好友的项目
            //             // 这个通过发广播让contactsFragment更新视图
            //             // Intent intent = new Intent();
            //             // intent.setAction(NEW_ADDFRIEND);
            //             // context.sendBroadcast(intent);
            //
            //             // 下面的工作理应在contactsFragment里执行
            //             // 3. 发一条问候消息给对方
            //             // sendmessage(from.toString().split("@")[0],"You're already friends. Let's chat!");
            //             // 4. 发起反向请求。
            //             // addFriend(from.toString().split("@")[0]);
            //             // 完成之后，只要对方一上线，就完成了双向好友了
            //         //     return SubscribeAnswer.Approve;
            //         // }
            //         // return null;
            //     }
            // });

            // 设置自动重连，暂时还没尝试
            // ReconnectionManager reconnectionManager= ReconnectionManager.getInstanceFor(sAbstractXMPPConnection);
            // reconnectionManager.enableAutomaticReconnection();

            sAbstractXMPPConnection.connect();
        } catch (Exception e) {
            e.printStackTrace();
            // Log.e("OpenfireConnector-buildconn", e.getLocalizedMessage());
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
        Map<String, String> attrs = new HashMap<>();
        attrs.put("email", email);
        // username暂时用来代理username，其实按理说username应该是qq号，因为在mysql表里有唯一性限制，而name才是昵称，可以随意命名。
        // 另外其他字段设置还没找到，以后再说
        attrs.put("name", username);
        accountManager.createAccount(Localpart.from(username), password,attrs);
    }
    public static void editPasword(String newpassword) throws Exception {
        accountManager.changePassword(newpassword);
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
    // 来自MainFragment_Contacts，准备放进OpenfireConnector
    public static List<String> searchUser(final String username) {
        List<String> result = new ArrayList<>();
        try {
            // 这里本来是用domain的，前面改来改去怀疑这是因为domain不在DNS里，所以改成硬编码的IP了
            // 后来发现是因为smack版本4.3.0有一点改动，gradle里换成smack 4.2.3就能用了
            DomainBareJid jid = JidCreate.domainBareFrom("search."+IP);
            Form searchForm = userSearchManger.getSearchForm(jid);
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Name", true);
            answerForm.setAnswer("search", username);

            ReportedData resData = userSearchManger.getSearchResults(answerForm, jid);
            List<ReportedData.Row> list = resData.getRows();

            for (ReportedData.Row row : list) {
                result.add(row.getValues("Name") == null ? "" : row.getValues("Name").iterator().next().toString());
                // Log.e("MYLOG6", row.getValues("Name").iterator().next().toString());
            }
            // Log.e("MYLOG6", ""+OpenfireConnector.sAbstractXMPPConnection.isConnected());
            // Log.e("MYLOG6", ""+ OpenfireConnector.sAbstractXMPPConnection.isAuthenticated());
            // Log.e("MYLOG6", "" + list.size());
            // Log.e("MYLOG6", username);
            // Log.e("MYLOG6", list.toString());
        } catch (Exception e) {
            // Log.e("MYLOG5", e.getMessage());
        }
        return result;
    }

    public static List<UserEntity> getContactsFromServerByRoster() {
        List<UserEntity> entrieslist = new ArrayList<UserEntity>();
        RosterGroup rosterGroup = getRoster().getGroup("friends");
        Collection<RosterEntry> rosterEntry;
        if (rosterGroup!=null) {
            rosterEntry = rosterGroup.getEntries();

            Iterator<RosterEntry> i = rosterEntry.iterator();
            while (i.hasNext()) {
                RosterEntry re = i.next();
                // if (!re.isApproved()) break;
                UserEntity item = new UserEntity();
                item.setJid(re.getJid().toString());
                item.setUsername(re.getJid().toString().split("@")[0]);
                item.setIfadded(re.isApproved() ? 1 : 0);
                entrieslist.add(item);
            }
        }
        Log.e("entrieslist", entrieslist.size()+"");
        return null;
    }

    public static List<UserEntity> getContactsFromServer() {
        List<UserEntity> allcontacts = new ArrayList<>();
        try {
            String sst = "<iq id='" + StanzaIdUtil.newStanzaId() + "' type='get'><query xmlns='jabber:iq:roster'></query></iq>";;
            Stanza stanza = new Stanza() {
                @Override
                public String toString() {
                    return sst;
                }
                @Override
                public CharSequence toXML() {
                    return sst;
                }
            };
            StanzaCollector collector = sAbstractXMPPConnection.createStanzaCollectorAndSend(new StanzaTypeFilter(IQ.class), stanza);
            String result = collector.nextResult().toXML().toString();
            Log.e("Openfireconnector-getContactsFromServer by IQ", result);
            // Example:
            // <iq to='user2@192.168.191.1/2irl9gbtiv' id='ZisH7-5' type='result'>
            // <query xmlns='jabber:iq:roster' ver='360964585'>
            // <item jid='user1@192.168.191.1' subscription='from'></item>
            // <item jid='user2@192.168.191.1' name='user2' subscription='none' ask='subscribe'>
            // <item jid='user4@192.168.191.1' name='user4' subscription='to'><group>friends</group></item>
            // <item jid='user3@192.168.191.1' name='user3' subscription='both'></item>
            // </query></iq>
            if (!result.contains("item")) return new ArrayList<>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(result));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("item".equals(parser.getName())) {
                            int count = parser.getAttributeCount();
                            String jid = "";
                            String name = "";
                            String subscription = "";
                            String ask = "";
                            for (int i = 0; i < count; i++) {
                                String key = parser.getAttributeName(i);
                                if ("jid".equals(key)) {
                                    jid = parser.getAttributeValue(i);
                                } else if ("name".equals(key)) {
                                    name = parser.getAttributeValue(i);
                                } else if ("subscription".equals(key)) {
                                    subscription = parser.getAttributeValue(i);
                                } else if ("ask".equals(key)) {
                                    ask = parser.getAttributeValue(i);
                                }
                            }
                            UserEntity userEntity = new UserEntity();
                            userEntity.setJid(jid);
                            userEntity.setUsername(jid.split("@")[0]);
                            // 此法不通，在接收申请，没有昵称的情况下，有些项还不能设置name，可通过jid先显示
                            // 等完全通过好友了，再通过其它接口进行修改即可！
                            // userEntity.setUsername(name);
                            switch (subscription) {
                                case "both":
                                    userEntity.setIfadded(FRIENDTYPE_BOTH);
                                    break;
                                case "from":
                                    userEntity.setIfadded(FRIENDTYPE_FROM);
                                    break;
                                case "to":
                                    userEntity.setIfadded(FRIENDTYPE_TO);
                                    break;
                                case "none":
                                    userEntity.setIfadded(FRIENDTYPE_NONE);
                                    break;
                            }
                            if (ask.equals("subscribe")) {
                                if (subscription.equals("from") ) {
                                    userEntity.setIfadded(FRIENDTYPE_BOTH);
                                } else {
                                    userEntity.setIfadded(FRIENDTYPE_ASK);
                                }
                            }
                            allcontacts.add(userEntity);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allcontacts;
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

    public static String sendGet(String url) throws Exception{
        StringBuilder result = new StringBuilder();
        URL urlObj = new URL(url);
        URLConnection connection = urlObj.openConnection();
        connection.connect();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append("\r\n").append(line);
        }
        return result.toString();
    }

    public static void setCurrentContext(Context context) {
        currentContext = context;
    }
}
