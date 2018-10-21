package com.weaverhong.lesson.chatchat.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.weaverhong.lesson.chatchat.DB.MessageDBManager;
import com.weaverhong.lesson.chatchat.Entity.MessageEntity;
import com.weaverhong.lesson.chatchat.ListItem.ChatListItem;
import com.weaverhong.lesson.chatchat.R;

import java.util.Comparator;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String EXTRA_CHATID = "com.weaverhong.chatchat.chatid";
    RecyclerView mRecyclerView;
    EditText mEditText;
    Button mButton;
    private ChatAdapter mChatAdapter;

    public static Intent newIntent(Context packageContext, ChatListItem item) {
        Intent intent = new Intent(packageContext, ChatActivity.class);
        intent.putExtra(EXTRA_CHATID, item.getUser());
        return intent;
    }

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_chat);

        mRecyclerView = findViewById(R.id.chat_list);
        mEditText = findViewById(R.id.chat_edittext);
        mButton = findViewById(R.id.chat_send);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateUI();
    }

    public void updateUI() {

        String leftuser = getIntent().getStringExtra("username");
        SharedPreferences sp = getSharedPreferences("chatchat", Context.MODE_PRIVATE);
        String rightuser = sp.getString("username", "null");

        // important:
        MessageDBManager messageDBManager = new MessageDBManager(this);
        List<MessageEntity> list = messageDBManager.findByRecvAndSender(leftuser, rightuser, 0);
        list.addAll(messageDBManager.findByRecvAndSender(rightuser, leftuser, 1));
        list.sort(new Comparator<MessageEntity>() {
            @Override
            public int compare(MessageEntity o1, MessageEntity o2) {
                return o1.getCreatetime().compareTo(o2.getCreatetime());
            }
        });


        if (mChatAdapter == null) {
            mChatAdapter = new ChatActivity.ChatAdapter(list);
            mRecyclerView.setAdapter(mChatAdapter);
        } else {
            mChatAdapter.setList(list);

            mChatAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
            // 这个方法是ListView的，对于Recylcler，只能把绑定的list倒序来实现视图倒序
            // mRecyclerView.setStackFromBottom(true);
        }
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
            return null;
        }

        @Override
        public void onBindViewHolder(ChatHolder holder, int position) {

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

        public void bind(MessageEntity item) {
            mItem = item;
            // right is sender
            if (item.getDirection() == 0) {
                itemView.findViewById(R.id.chat_left).setVisibility(View.INVISIBLE);
                itemView.findViewById(R.id.chat_right).setVisibility(View.VISIBLE);
                TextView t = (TextView) itemView.findViewById(R.id.right_message);
                t.setText(item.getContent());
                TextView timet = (TextView) itemView.findViewById(R.id.right_time);
                timet.setText(item.getCreatetime());
            } else {
                itemView.findViewById(R.id.chat_right).setVisibility(View.INVISIBLE);
                itemView.findViewById(R.id.chat_left).setVisibility(View.VISIBLE);
                TextView t = (TextView) itemView.findViewById(R.id.left_message);
                t.setText(item.getContent());
                TextView timet = (TextView) itemView.findViewById(R.id.left_time);
                timet.setText(item.getCreatetime());
            }
        }
    }
}
