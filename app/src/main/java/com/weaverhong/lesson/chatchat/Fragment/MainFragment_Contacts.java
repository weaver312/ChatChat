package com.weaverhong.lesson.chatchat.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.weaverhong.lesson.chatchat.Activity.UserActivity;
import com.weaverhong.lesson.chatchat.DB.ContactDBManager;
import com.weaverhong.lesson.chatchat.Datalabs.ContactLab;
import com.weaverhong.lesson.chatchat.Entity.UserEntity;
import com.weaverhong.lesson.chatchat.ListItem.ContactListItem;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
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
import java.util.List;

import static com.weaverhong.lesson.chatchat.OpenfireConnector.DOMAIN;
import static com.weaverhong.lesson.chatchat.OpenfireConnector.FRIENDTYPE_ASK;
import static com.weaverhong.lesson.chatchat.OpenfireConnector.FRIENDTYPE_BOTH;
import static com.weaverhong.lesson.chatchat.OpenfireConnector.FRIENDTYPE_FROM;
import static com.weaverhong.lesson.chatchat.OpenfireConnector.FRIENDTYPE_NONE;
import static com.weaverhong.lesson.chatchat.OpenfireConnector.FRIENDTYPE_TO;
import static com.weaverhong.lesson.chatchat.OpenfireConnector.sAbstractXMPPConnection;
import static com.weaverhong.lesson.chatchat.OpenfireConnector.username;

public class MainFragment_Contacts extends Fragment {

    private RecyclerView mRecyclerView;
    private ContactAdapter mContactAdapter;
    private View view;
    FloatingActionButton mFloatingActionButton;

    public static MainFragment_Contacts newInstance() {
        MainFragment_Contacts fragment = new MainFragment_Contacts();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                new AlertDialog.Builder(getActivity())
                        .setTitle("Search a friend")
                        .setView(editText)
                        .setPositiveButton("search", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String input = editText.getText().toString();
                                if (input.equals("")) {
                                    Toast.makeText(getActivity(), "empty input! no search executed" + input, Toast.LENGTH_LONG).show();
                                } else {
                                    try {
                                        List<String> result = searchUser(editText.getText().toString());
                                        String[] resultarray = new String[result.size()];
                                        result.toArray(resultarray);
                                        AlertDialog alertDialog = new AlertDialog
                                                .Builder(getActivity())
                                                .setItems(resultarray, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // 注意这里的下标从0开始，index = [0 ~ (size-1)]
                                                        try {
                                                            // 避免把自己添加为好友
                                                            if (resultarray[which].equals(username)) return;
                                                            // 单向发起好友请求
                                                            OpenfireConnector.addFriend(resultarray[which]);
                                                            // 并不添加到数据库里，也不更新视图，只告诉用户你发过了。相当于发了之后就等着就好
                                                            Toast.makeText(getActivity(), "have sent a friend apply to " + resultarray[which] + ".", Toast.LENGTH_SHORT).show();
                                                            // 自己更新一下，确实也没什么用，就不更新了
                                                            // ContactLab.refreshdatalocal(getActivity());
                                                            updateUI();
                                                        } catch (Exception e) {
                                                            // 这里试一下这个getLocalizedMessage
                                                            Log.e("Fragment-contacts", e.toString());
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("cancel", null)
                                                .setTitle("Search Result:")
                                                .create();
                                        alertDialog.show();
                                    } catch (Exception e) {
                                        Log.e("Fragment-contacts", e.toString());
                                    }
                                }
                            }
                        })
                        .setNegativeButton("cancel", null)
                        .show();
            }
        });

        ContactLab.refreshdatalocal(getContext());
        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ContactLab.refreshdatalocal(getContext());
        updateUI();
    }

    public void updateUI() {
        // NOTICE:
        // always call this method before call updateUI():
        // ContactLab.refreshdatalocal();

        if (ContactLab.list.size() == 0) {
            view.findViewById(R.id.nocontacts).setVisibility(View.VISIBLE);
            view.findViewById(R.id.contacts_list).setVisibility(View.INVISIBLE);
            return;
        } else {
            view.findViewById(R.id.nocontacts).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.contacts_list).setVisibility(View.VISIBLE);
        }

        if (mContactAdapter == null) {
            mContactAdapter = new MainFragment_Contacts.ContactAdapter(ContactLab.list);
            mRecyclerView.setAdapter(mContactAdapter);
        } else {
            mContactAdapter.setList(ContactLab.list);
            mContactAdapter.notifyDataSetChanged();
        }
    }

    private class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mUserTextView;
        private Button mButton;
        private Button mButton_refuse;

        private ContactListItem mItem;

        public ContactHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_contactlist, parent, false));
            itemView.setOnClickListener(this);
            mUserTextView = itemView.findViewById(R.id.contactlist_user);
            mButton = itemView.findViewById(R.id.contactlist_botton);
            mButton_refuse = itemView.findViewById(R.id.contactlist_botton_refuse);
        }

        @Override
        public void onClick(View v) {
            // 只有friend才能chat
            if (mItem.isIffriend()) {
                Intent intent = UserActivity.newInstance(getActivity());
                intent.putExtra("username", mItem.getUsername());
                startActivity(intent);
            }
        }

        public void bind(ContactListItem item) {
            mItem = item;
            mUserTextView.setText(mItem.getUsername());
            // mButton.setVisibility(mItem.isIffriend() ? View.INVISIBLE : View.VISIBLE);
            switch (mItem.getFriendtype()) {
                case FRIENDTYPE_BOTH:
                    // 均是好友，不需要显示按钮，可以点击
                    mButton.setVisibility(View.INVISIBLE);
                    break;
                case FRIENDTYPE_FROM:
                    // 是待同意者，需要按钮，不可点击
                    mButton.setText("ADD");
                    mButton.setClickable(true);
                    mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                UserEntity entity = new ContactDBManager(getContext()).findByUsername(mItem.getUsername());
                                if (entity!=null) {
                                    // 更改好友状态为已添加
                                    entity.setIfadded(1);
                                    new ContactDBManager(getContext()).update(entity);
                                } else {
                                    // 或者：添加好友到本地数据库
                                    entity = new UserEntity();
                                    entity.setIfadded(1);
                                    entity.setUsername(mItem.getUsername());
                                    entity.setJid(mItem.getUsername()+"@"+OpenfireConnector.DOMAIN);
                                    new ContactDBManager(getContext()).add(entity);
                                    // 并从SP里删掉这一条
                                    // SharedPreferences sharedPreferences = getActivity().getSharedPreferences("chatchat", Context.MODE_PRIVATE);
                                    // Set<String> set = sharedPreferences.getStringSet("notaddusers", new HashSet<>());
                                    // set.remove(mItem.getUsername());
                                    // sharedPreferences.edit().putStringSet("notaddusers", set).commit();

                                }
                                // 回复好友表示已同意
                                OpenfireConnector.replyfriendapply(mItem.getUsername(), false);
                                // 反向添加添加好友
                                OpenfireConnector.addFriend(mItem.getUsername());
                                // 刷新视图
                                ContactLab.refreshdatalocal(getContext());
                                // 因为前面已经改为已添加，所以此时已经可以给对方发消息了，对方也确实收的到尝试发一条问候语给对方
                                OpenfireConnector.sendmessage(mItem.getUsername(), "We're already friends!");
                                updateUI();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    mButton.setVisibility(View.VISIBLE);
                    break;
                case FRIENDTYPE_TO:
                    // 是发送，被收到了，但还未获同意者
                    mButton.setText("WAITING");
                    mButton.setClickable(false);
                    mButton.setOnClickListener(null);
                    mButton.setVisibility(View.VISIBLE);
                    break;
                case FRIENDTYPE_NONE:
                    mButton.setText("?");
                    mButton.setClickable(false);
                    mButton.setOnClickListener(null);
                    mButton.setVisibility(View.VISIBLE);
                    break;
                case FRIENDTYPE_ASK:
                    // 是发送，被收到了，但还未获同意者
                    mButton.setText("WAITING");
                    mButton.setClickable(false);
                    mButton.setOnClickListener(null);
                    mButton.setVisibility(View.VISIBLE);
                    break;
            }
            mButton_refuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // 删除待添加好友
                        new ContactDBManager(getContext()).delete(item.getUsername());
                        // 回复说unsubscribe
                        OpenfireConnector.replyfriendapply(item.getUsername(), true);
                        ContactLab.refreshdatalocal(getContext());
                        updateUI();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<MainFragment_Contacts.ContactHolder> {
        private List<ContactListItem> list;

        public ContactAdapter(List<ContactListItem> list) {
            this.list = list;
        }

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

    // 准备放进OpenfireConnector
    public List<String> searchUser(final String username) {
        List<String> result = new ArrayList<>();
        try {
            UserSearchManager userSearchManger = new UserSearchManager(sAbstractXMPPConnection);
            // 这句不能少，具体原因看此方法addIQProvider()的注释
            ProviderManager.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

            // 这里本来是用domain的，前面改来改去怀疑这是因为domain不在DNS里，所以改成硬编码的IP了
            // 后来发现是因为smack版本4.3.0有一点改动，gradle里换成smack 4.2.3就能用了
            DomainBareJid jid = JidCreate.domainBareFrom("search.192.168.191.1");
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
            Log.e("MYLOG6", "" + list.size());
            Log.e("MYLOG6", username);
            // Log.e("MYLOG6", list.toString());
        } catch (Exception e) {
            Log.e("MYLOG5", e.getMessage());
        }
        return result;
    }

    @Deprecated
    public boolean addFriend(Roster roster, String friendName, String name) throws Exception {
        try {
            // 本部分修改自：https://blog.csdn.net/EricFantastic/article/details/48311871
            // 首先，回复对方的好友请求
            Presence presenceRes = new Presence(Presence.Type.subscribed);
            presenceRes.setTo(name + "@" + DOMAIN);
            OpenfireConnector.sAbstractXMPPConnection.sendStanza(presenceRes);

            // 这里因为版本原因，网上原先的解决方法第三个参数用null也可，表示不分组的朋友
            // 通过管理员添加貌似也可null
            // 但是smack现在好像必须加分组了，null就添加不行了

            // 第二步，将对面添加到自己的朋友分组里面
            EntityBareJid jid = JidCreate.entityBareFrom(friendName.trim() + "@" + DOMAIN);
            roster.createEntry(jid, name, new String[]{"friends"});
            Log.e("MYLOG8", "add friend success");
            return true;
        } catch (XMPPException e) {
            Log.e("MYLOG8", e.toString());
            Log.e("MYLOG8", "add friend fail");
            return false;
        }
    }
    @Deprecated
    public boolean refuseFriend(Roster roster, String friendName, String name) {
        try {
            Presence presenceRes = new Presence(Presence.Type.unsubscribe);
            presenceRes.setTo(friendName + "@" + DOMAIN);
            OpenfireConnector.sAbstractXMPPConnection.sendStanza(presenceRes);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

