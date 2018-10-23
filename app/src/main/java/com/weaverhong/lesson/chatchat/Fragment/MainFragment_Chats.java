package com.weaverhong.lesson.chatchat.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.weaverhong.lesson.chatchat.Activity.ChatActivity;
import com.weaverhong.lesson.chatchat.ListItem.ChatListItem;
import com.weaverhong.lesson.chatchat.Datalabs.ChatsLab;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

import java.util.List;

public class MainFragment_Chats extends Fragment {

    private RecyclerView mRecyclerView;
    private ChatAdapter mChatAdapter;
    private View view;

    public static MainFragment_Chats newInstance() {
        Bundle args = new Bundle();
        MainFragment_Chats fragment = new MainFragment_Chats();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chats, container, false);

        mRecyclerView = view.findViewById(R.id.chat_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    List<ChatListItem> list = ChatsLab.mChatitems;
    private void updateUI() {

        if (list.size() == 0) {
            view.findViewById(R.id.norecentchats).setVisibility(View.VISIBLE);
            view.findViewById(R.id.chat_list).setVisibility(View.GONE);
            return;
        } else {
            view.findViewById(R.id.norecentchats).setVisibility(View.GONE);
            view.findViewById(R.id.chat_list).setVisibility(View.VISIBLE);
        }
        if (mChatAdapter == null) {
            mChatAdapter = new ChatAdapter(list);
            mRecyclerView.setAdapter(mChatAdapter);
        } else {
            mChatAdapter.setList(list);

            // notify data change, important
            mChatAdapter.notifyDataSetChanged();
        }
    }

    private class ChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mUserTextView;
        private TextView mContentTextView;

        private ChatListItem mItem;

        public ChatHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_chatlist, parent, false));
            itemView.setOnClickListener(this);

            mUserTextView = itemView.findViewById(R.id.chatlist_user);
            mContentTextView = itemView.findViewById(R.id.chatlist_content);
        }

        @Override
        public void onClick(View v) {
            Intent intent = ChatActivity.newIntent(getActivity(), mItem, OpenfireConnector.username);
            startActivity(intent);
        }

        public void bind(ChatListItem item) {
            mItem = item;
            mUserTextView.setText(mItem.getUser());
            mContentTextView.setText(mItem.getContent());
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatHolder> {
        private List<ChatListItem> list;
        public ChatAdapter(List<ChatListItem> list) { this.list = list; }

        @Override
        public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ChatHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ChatHolder holder, int position) {
            ChatListItem item = list.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void setList(List<ChatListItem> list) {
            this.list = list;
        }
    }
}
