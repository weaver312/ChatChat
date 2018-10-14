package com.weaverhong.lesson.chatchat.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.weaverhong.lesson.chatchat.R;

public class MainFragment_Profile extends Fragment {

    private static View view;
    TextView mUsernameTextview;
    TextView mRegisttimeTextview;
    ListView mProfileListview;

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
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        mUsernameTextview = view.findViewById(R.id.profile_username);
        mRegisttimeTextview = view.findViewById(R.id.profile_registtime);
        mProfileListview = view.findViewById(R.id.profile_optionslist);

        return view;
    }
}
