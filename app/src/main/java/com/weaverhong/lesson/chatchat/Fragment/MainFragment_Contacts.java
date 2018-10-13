package com.weaverhong.lesson.chatchat.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weaverhong.lesson.chatchat.R;

public class MainFragment_Contacts extends Fragment {

    public static MainFragment_Contacts newInstance() {
        Bundle args = new Bundle();
        MainFragment_Contacts fragment = new MainFragment_Contacts();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, container, false);
        return v;
    }
}
