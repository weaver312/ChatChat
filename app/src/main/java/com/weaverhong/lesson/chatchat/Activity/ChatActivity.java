package com.weaverhong.lesson.chatchat.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.weaverhong.lesson.chatchat.Activity_Autoshutdown.BaseAppCompatActivity;
import com.weaverhong.lesson.chatchat.DB.MessageDBManager;
import com.weaverhong.lesson.chatchat.Entity.MessageEntity;
import com.weaverhong.lesson.chatchat.KeyboardShowandHideListener;
import com.weaverhong.lesson.chatchat.ListItem.ChatListItem;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.weaverhong.lesson.chatchat.OpenfireConnector.sendGet;

public class ChatActivity extends BaseAppCompatActivity {

    private Context mContext;
    String leftuser;
    String rightuser;
    MessageDBManager messageDBManager;
    MyReceiver myReceiver;
    Handler mHandler;

    private static final String EXTRA_CHATID = "com.weaverhong.chatchat.chatid";
    private static final String EXTRA_USERNAME = "com.weaverhong.chatchat.username_right";

    RecyclerView mRecyclerView;
    EditText mEditText;
    Button mButton;
    private ChatAdapter mChatAdapter;

    public static Intent newIntent(Context packageContext, ChatListItem item, String username2) {
        Intent intent = new Intent(packageContext, ChatActivity.class);
        intent.putExtra(EXTRA_CHATID, item.getUser());
        intent.putExtra(EXTRA_USERNAME, username2);
        return intent;
    }

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        this.mContext = this;
        OpenfireConnector.setCurrentContext(this);
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_chat);
        mHandler = new Handler();

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_CHATID));
        // getActionBar().setTitle(getIntent().getStringExtra(EXTRA_USERNAME));
        // getActionBar().setHomeButtonEnabled(true);

        messageDBManager = new MessageDBManager(this);
        mRecyclerView = findViewById(R.id.chat_recyclerview);
        mEditText = findViewById(R.id.chat_edittext);
        mButton = findViewById(R.id.chat_send);

        leftuser = getIntent().getStringExtra(EXTRA_CHATID);
        rightuser = getIntent().getStringExtra(EXTRA_USERNAME);

        // Log.e("LEFTUSER", leftuser==null?"":leftuser);
        // Log.e("RIGHTUSER", rightuser==null?"":rightuser);

        // 发送消息的逻辑
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable edittext = mEditText.getText();
                if (edittext != null && edittext.length() > 0 && leftuser != null && rightuser != null
                        && !leftuser.equals("") && !rightuser.equals("")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                // Long time = System.currentTimeMillis();
                                // Map<String, String> map = new HashMap<>();
                                // map.put(OpenfireConnector.XMLTAG_TIME, "" + time);

                                // Log.e("ChatActivity", "trying to send message");

                                try {
                                    String text = edittext.toString();
                                    Map<String, String> args = new HashMap<>();
                                    Long nettime = Long.valueOf(sendGet("http://api.m.taobao.com/rest/api3.do?api=mtop.common.getTimestamp").substring(83, 96));
                                    args.put("mytimestamp", nettime+"");
                                    OpenfireConnector.sendmessage(leftuser, text, args);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                // Log.e("ChatActivity", "trying to store message");
                                // have to set MSGTRANSID to null, trying to use other method:
                                // MessageEntity item = new MessageEntity();
                                // item.setDirection(0);
                                // item.setReceivername(leftuser);
                                // item.setSendername(rightuser);
                                // item.setContent(text);
                                // item.setCreatetime("" + time);
                                // item.setMsgtranid("null");
                                // messageDBManager.add(item);

                                Log.e("ChatActivity", "trying to update UI and make input empty");
                            }
                        }).start();
                    mEditText.setText("");
                } else {
                    // Toast.makeText(mContext, "Empty input!", Toast.LENGTH_SHORT);
                }
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
            }
        });

        KeyboardShowandHideListener.setListener(ChatActivity.this, new KeyboardShowandHideListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                // Toast.makeText(ChatActivity.this, "键盘显示 高度" + height, Toast.LENGTH_SHORT).show();
                mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
            }

            @Override
            public void keyBoardHide(int height) {
                // Toast.makeText(ChatActivity.this, "键盘隐藏 高度" + height, Toast.LENGTH_SHORT).show();
                mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
            }
        });

        myReceiver = new MyReceiver();

        // IntentFilter intentFilter = new IntentFilter();
        // intentFilter.addAction("com.weaverhong.lesson.chatchat.newmessage");
        // registerReceiver(myReceiver,intentFilter);
        // Log.e("Chatactivity","receiver received.");

        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.weaverhong.lesson.chatchat.newmessage");
            ChatActivity.this.registerReceiver(myReceiver, intentFilter);
        } catch (Exception e ) {}
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            ChatActivity.this.unregisterReceiver(myReceiver);
        } catch (Exception e ) {}
    }

    @Override
    public void onDestroy() {
        Log.e("ChatActivity","Chat Activity onDestroy!");
        // unregisterReceiver(myReceiver);
        super.onDestroy();
    }

    public void updateUI() {

        // get new messagelist from DB
        List<MessageEntity> list = messageDBManager.findByRecvAndSender(leftuser, rightuser);
        list.addAll(messageDBManager.findByRecvAndSender(rightuser, leftuser));
        // sort by send time
        // list.sort(new Comparator<MessageEntity>() {
        //     @Override
        //     public int compare(MessageEntity o1, MessageEntity o2) {
        //         return o1.getCreatetime().compareTo(o2.getCreatetime());
        //     }
        // });
        for (MessageEntity m : list) {
            String format = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            m.setCreatetime(sdf.format(new Date(Long.valueOf(m.getCreatetime()))));
            // 其实还可以加判定条件，如果是最近几天的消息，就显示为“今天”、“昨天”，等等
        }

        if (mChatAdapter == null) {
            mChatAdapter = new ChatActivity.ChatAdapter(list);
            mRecyclerView.setAdapter(mChatAdapter);
            mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
        } else {
            mChatAdapter.setList(list);
            mChatAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
            // 这个方法是ListView的，对于Recylcler，只能把绑定的list倒序来实现视图倒序
            // mRecyclerView.setStackFromBottom(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatHolder> {

        List<MessageEntity> list;

        public void setList(List<MessageEntity> list) {
            this.list = list;
        }

        ChatAdapter(List<MessageEntity> list) {
            this.list = list;
        }

        @Override
        public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            return new ChatHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ChatHolder holder, int position) {
            MessageEntity item = list.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }


    private class ChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private MessageEntity mItem;

        public ChatHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_chat, parent, false));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // 暂时没有行为，以后可以添加长按滑动等手势
        }

        // 核心逻辑
        public void bind(MessageEntity item) {
            mItem = item;
            // right is sender
            if (mItem.getSendername().equals(OpenfireConnector.username)) {
                itemView.findViewById(R.id.chat_left).setVisibility(View.INVISIBLE);
                itemView.findViewById(R.id.chat_right).setVisibility(View.VISIBLE);
                TextView t = (TextView) itemView.findViewById(R.id.right_message);
                TextView timet = (TextView) itemView.findViewById(R.id.right_time);
                t.setText(mItem.getContent());
                timet.setText(mItem.getCreatetime());
            } else {
                itemView.findViewById(R.id.chat_left).setVisibility(View.VISIBLE);
                itemView.findViewById(R.id.chat_right).setVisibility(View.INVISIBLE);
                TextView t = (TextView) itemView.findViewById(R.id.left_message);
                TextView timet = (TextView) itemView.findViewById(R.id.left_time);
                t.setText(mItem.getContent());
                timet.setText(mItem.getCreatetime());
            }
        }
    }


    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Bundle bundle = intent.getExtras();
            // Only message received are processed. Message sent is not listened
            // String msgtranid = bundle.getString("MSGTRANID");
            // String sendername= bundle.getString("SENDERNAME");
            // String receivername = bundle.getString("RECEIVERNAME");
            // String createtime = bundle.getString("CREATETIME");
            // String content = bundle.getString("CONTENT");

            // MessageEntity item = new MessageEntity();
            // item.setMsgtranid(msgtranid);
            // item.setSendername(sendername);
            // item.setReceivername(receivername);
            // item.setCreatetime(createtime);
            // item.setContent(content);
            // 1 is receive message
            // item.setDirection(1);

            // insert into database
            // MessageDBManager messageDBManager = new MessageDBManager(context);
            // messageDBManager.add(item);

            // // send a intent to chatactivity to refresh chat data
            // Intent messageintent = new Intent();
            // messageintent.putExtra("messagefrom", sendername);
            // messageintent.setAction("com.weaverhong.lesson.chatchat.newmessage");
            // sendBroadcast(messageintent);

            // maybe send a intent to mainactivity to refresh chat data
            updateUI();
        }
    }

}
