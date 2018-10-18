package com.weaverhong.lesson.chatchat.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.weaverhong.lesson.chatchat.Activity.UserActivity;
import com.weaverhong.lesson.chatchat.Datalabs.ContactLab;
import com.weaverhong.lesson.chatchat.ListItem.ContactListItem;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.weaverhong.lesson.chatchat.OpenfireConnector.DOMAIN;
import static com.weaverhong.lesson.chatchat.OpenfireConnector.sAbstractXMPPConnection;

public class MainFragment_Contacts extends Fragment {

    private RecyclerView mRecyclerView;
    private ContactAdapter mContactAdapter;
    private View view;
    private String name,password,response,acceptAdd,alertName,alertSubName;
    FloatingActionButton mFloatingActionButton;

    public static MainFragment_Contacts newInstance() {
        MainFragment_Contacts fragment = new MainFragment_Contacts();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 注册监听器
        MyReceiver myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.weaverhong.lesson.chatchat.addfriend");
        getActivity().registerReceiver(myReceiver,intentFilter);


        //条件过滤器
        StanzaFilter stanzaFilter = new AndFilter(new StanzaTypeFilter(Presence.class));
        //packet监听器
        StanzaListener listener = new StanzaListener() {
            @Override
            public void processStanza(Stanza packet) {
                System.out.println("PresenceService-"+packet.toXML());
                if(packet instanceof Presence){
                    Presence presence = (Presence)packet;
                    String from = presence.getFrom().toString();//发送方
                    String to = presence.getTo().toString();//接收方
                    if (presence.getType().equals(Presence.Type.subscribe)) {
                        System.out.println("收到添加请求！");
                        //发送广播传递发送方的JIDfrom及字符串
                        acceptAdd = "收到添加请求！";
                        Intent intent = new Intent();
                        intent.putExtra("fromName", from);
                        intent.putExtra("acceptAdd", acceptAdd);
                        intent.setAction("com.weaverhong.lesson.chatchat.addfriend");
                        getActivity().sendBroadcast(intent);
                        startActivity(intent);
                    } else if (presence.getType().equals(
                            Presence.Type.subscribed)) {
                        //发送广播传递response字符串
                        response = "恭喜，对方同意添加好友！";
                        Intent intent = new Intent();
                        intent.putExtra("response", response);
                        intent.setAction("com.weaverhong.lesson.chatchat.addfriend");
                        getActivity().sendBroadcast(intent);
                    } else if (presence.getType().equals(
                            Presence.Type.unsubscribe)) {
                        //发送广播传递response字符串
                        response = "抱歉，对方拒绝添加好友，将你从好友列表移除！";
                        Intent intent = new Intent();
                        intent.putExtra("response", response);
                        intent.setAction("com.weaverhong.lesson.chatchat.addfriend");
                        getActivity().sendBroadcast(intent);
                    } else if (presence.getType().equals(
                            Presence.Type.unsubscribed)){
                    } else if (presence.getType().equals(
                            Presence.Type.unavailable)) {
                        System.out.println("好友下线！");
                    } else {
                        System.out.println("好友上线！");
                    }
                }
            }
        };
        OpenfireConnector.sAbstractXMPPConnection.addAsyncStanzaListener(listener, stanzaFilter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contacts, container, false);

        mRecyclerView = view.findViewById(R.id.contacts_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFloatingActionButton = view.findViewById(R.id.floatingActionButton);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(getActivity());

                new AlertDialog.Builder(getActivity()).setTitle("Search a friend")
                    .setView(editText)
                    .setPositiveButton("search", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String input = editText.getText().toString();
                            if (input.equals("")) {
                                Toast.makeText(getActivity(), "empty input! no search executed" + input, Toast.LENGTH_LONG).show();
                            } else {
                                try {
                                    searchUser(editText.getText().toString());
                                } catch (Exception e) {
                                    Log.e("MYLOG4", e.toString());
                                }
                            }
                        }
                    })
                    .setNegativeButton("cancel", null)
                    .show();
            }
        });

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        List<ContactListItem> list = ContactLab.mContactitems;

        if (list.size() == 0) {
            view.findViewById(R.id.nocontacts).setVisibility(View.VISIBLE);
            view.findViewById(R.id.contacts_list).setVisibility(View.GONE);
            return;
        } else {
            view.findViewById(R.id.nocontacts).setVisibility(View.GONE);
            view.findViewById(R.id.contacts_list).setVisibility(View.VISIBLE);
        }

        if (mContactAdapter == null) {
            mContactAdapter = new MainFragment_Contacts.ContactAdapter(list);
            mRecyclerView.setAdapter(mContactAdapter);
        } else {
            mContactAdapter.setList(list);

            // notify data change, important
            mContactAdapter.notifyDataSetChanged();
        }
    }

    private class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mUserTextView;

        private ContactListItem mItem;

        public ContactHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_contactlist, parent, false));
            itemView.setOnClickListener(this);
            mUserTextView = itemView.findViewById(R.id.contactlist_user);
        }

        @Override
        public void onClick(View v) {
            Intent intent = UserActivity.newInstance(getActivity());
            intent.putExtra("username", mItem.getUsername());
            startActivity(intent);
        }

        public void bind(ContactListItem item) {
            mItem = item;
            mUserTextView.setText(mItem.getUsername());
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<MainFragment_Contacts.ContactHolder> {
        private List<ContactListItem> list;
        public ContactAdapter(List<ContactListItem> list) { this.list = list; }

        @Override
        public MainFragment_Contacts.ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new MainFragment_Contacts.ContactHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(MainFragment_Contacts.ContactHolder holder, int position) {
            ContactListItem item = list.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void setList(List<ContactListItem> list) {
            this.list = list;
        }
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //接收传递的字符串response
            Bundle bundle = intent.getExtras();
            response = bundle.getString("response");
            System.out.println("广播收到"+response);
            // text_response.setText(response);
            if(response==null){
                //获取传递的字符串及发送方JID
                acceptAdd = bundle.getString("acceptAdd");
                alertName = bundle.getString("fromName");
                if(alertName!=null){
                    //裁剪JID得到对方用户名
                    alertSubName = alertName.substring(0,alertName.indexOf("@"));
                }
                if(acceptAdd.equals("收到添加请求")){
                    //弹出一个对话框，包含同意和拒绝按钮
                    AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity());
                    builder.setTitle("添加好友请求");
                    builder.setMessage("用户"+alertSubName+"请求添加你为好友" );
                    builder.setPositiveButton("同意",new DialogInterface.OnClickListener() {
                        //同意按钮监听事件，发送同意Presence包及添加对方为好友的申请
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            try {
                                boolean result = OpenfireConnector.replyfriendapply(alertName, false);
                                if (result) addFriend(OpenfireConnector.getRoster(), alertSubName, alertSubName);
                                else return;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            try {
                                boolean result = OpenfireConnector.replyfriendapply(alertName, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    public  boolean addFriend(Roster roster, String friendName, String name) throws Exception{

        UserSearchManager userSearchManger;
        try {
            EntityBareJid jid = JidCreate.entityBareFrom(friendName.trim()+ DOMAIN);
            roster.createEntry(jid, name, new String[]{"Friends"});
            System.out.println("添加好友成功！！");
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("失败！！"+e);
            return false;
        }
    }

    public List<String> searchUser(final String username) throws Exception{
        List<String> result = new ArrayList<>();
        try {
            UserSearchManager userSearchManger = new UserSearchManager(sAbstractXMPPConnection);
            ProviderManager.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

            DomainBareJid jid = JidCreate.domainBareFrom("search.192.168.191.1");
            Form searchForm = userSearchManger.getSearchForm(jid);
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Name", true);
            answerForm.setAnswer("search", username);

            ReportedData resData = userSearchManger.getSearchResults(answerForm, jid);
            List<ReportedData.Row> list = resData.getRows();
            Iterator<ReportedData.Row> it = list.iterator();

            while (it.hasNext()) {
                ReportedData.Row row = it.next();
                result.add(row.getValues("Name")==null?"":row.getValues("Name").iterator().next().toString());
                // Log.e("MYLOG6", row.getValues("Name").iterator().next().toString());
            }
            // Log.e("MYLOG6", ""+OpenfireConnector.sAbstractXMPPConnection.isConnected());
            // Log.e("MYLOG6", ""+ OpenfireConnector.sAbstractXMPPConnection.isAuthenticated());
            Log.e("MYLOG6", ""+list.size());
            Log.e("MYLOG6", username);
            // Log.e("MYLOG6", list.toString());
        } catch (Exception e) {
            Log.e("MYLOG5", e.getMessage());
        }
        return result;
    }
}

