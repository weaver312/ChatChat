package com.weaverhong.lesson.chatchat.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weaverhong.lesson.chatchat.R;

public class MainFragment_Profile extends Fragment {

    public static MainFragment_Profile newInstance() {
        Bundle args = new Bundle();
        MainFragment_Profile fragment = new MainFragment_Profile();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        return v;
    }
}
