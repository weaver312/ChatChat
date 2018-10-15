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

import com.weaverhong.lesson.chatchat.Activity.UserActivity;
import com.weaverhong.lesson.chatchat.Datalabs.ContactLab;
import com.weaverhong.lesson.chatchat.ListItem.ContactListItem;
import com.weaverhong.lesson.chatchat.R;

import java.util.List;

public class MainFragment_Contacts extends Fragment {

    private RecyclerView mRecyclerView;
    private ContactAdapter mContactAdapter;
    private View view;

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
}

