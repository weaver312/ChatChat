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
import com.weaverhong.lesson.chatchat.Datalabs.ContactLab;
import com.weaverhong.lesson.chatchat.ListItem.ContactListItem;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

import org.jivesoftware.smack.XMPPException;
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
import static com.weaverhong.lesson.chatchat.OpenfireConnector.getRoster;
import static com.weaverhong.lesson.chatchat.OpenfireConnector.sAbstractXMPPConnection;

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

                new AlertDialog.Builder(getActivity()).setTitle("Search a friend")
                    .setView(editText)
                    .setPositiveButton("search", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String input = editText.getText().toString();
                            if (input.equals("")) {
                                Toast.makeText(getActivity(), "empty input! no search executed" + input, Toast.LENGTH_LONG).show();
                            } else {
                                try {
                                    List<String> result = searchUser(editText.getText().toString());
                                    String [] resultarray = new String[result.size()];
                                    result.toArray(resultarray);
                                    AlertDialog alertDialog = new AlertDialog
                                            .Builder(getActivity())
                                            .setItems(resultarray, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // 注意这里的下标从0开始，index = [0 ~ (size-1)]
                                                    try {
                                                        Toast.makeText(getActivity(), "try to send a friend apply to " + resultarray[which] + ".", Toast.LENGTH_SHORT).show();
                                                        addFriend(OpenfireConnector.getRoster(), resultarray[which], resultarray[which]);
                                                        Toast.makeText(getActivity(), "have sent a friend apply to " + resultarray[which] + ".", Toast.LENGTH_SHORT).show();
                                                        Thread.sleep(300);
                                                        ContactLab.refreshdata();
                                                        updateUI();
                                                    } catch (Exception e) {
                                                        // 这里试一下这个getLocalizedMessage
                                                        Log.e("MYLOG9",e.toString());
                                                    }
                                                }
                                            }).create();
                                    alertDialog.show();
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

        ContactLab.refreshdata();
        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ContactLab.refreshdata();
        updateUI();
    }

    public void updateUI() {
        // NOTICE:
        // always call this method before call updateUI():
        // ContactLab.refreshdata();

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
        private Button mButton;

        private ContactListItem mItem;

        public ContactHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_contactlist, parent, false));
            itemView.setOnClickListener(this);
            mUserTextView = itemView.findViewById(R.id.contactlist_user);
            mButton = itemView.findViewById(R.id.contactlist_botton);
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
            mButton.setVisibility(mItem.isIffriend()?View.INVISIBLE:View.VISIBLE);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // balabala
                    try {
                        // 添加好友行为，调用addFriend函数
                        addFriend(getRoster(),mItem.getUsername(),mItem.getUsername());
                        // 检查是否添加完了，或者updateUI并传一个值，使仅update这个Holder的UI
                        // if (getRoster().getEntry((EntityBareJid) JidCreate.from(mItem.getUsername()+"@"+IP)).getName()==null)
                        //     // 添加失败
                        //     ;
                        // else {
                        //     // 添加成功
                        ContactLab.refreshdata();
                        updateUI();
                        // }
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }


                }
            });
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
            Log.e("MYLOG6", ""+list.size());
            Log.e("MYLOG6", username);
            // Log.e("MYLOG6", list.toString());
        } catch (Exception e) {
            Log.e("MYLOG5", e.getMessage());
        }
        return result;
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
}

