package com.weaverhong.lesson.chatchat.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.weaverhong.lesson.chatchat.DB.ContactDBManager;
import com.weaverhong.lesson.chatchat.DB.MessageDBManager;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

public class MainFragment_Profile extends Fragment {

    private static View view;
    TextView mUsernameTextview;
    TextView mRegisttimeTextview;
    ListView mProfileListview;
    private static String[] liststr = {"Edit password","About","Quit APP","Logout","Delete All Data and Switch User"};

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
        // mRegisttimeTextview = view.findViewById(R.id.profile_registtime);
        mProfileListview = view.findViewById(R.id.profile_optionslist);

        SharedPreferences sp = getActivity().getSharedPreferences("chatchat", Context.MODE_PRIVATE);
        mUsernameTextview.setText(sp.getString("username", "null"));
        ArrayAdapter<String> tempAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, liststr);
        mProfileListview.setAdapter(tempAdapter);
        mProfileListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // 修改密码
                        final EditText editText = new EditText(getActivity());
                        new AlertDialog.Builder(getActivity())
                                .setView(editText)
                                .setTitle("New password")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            // 检查合法性
                                            if (editText.getText().length()==0) {
                                                return;
                                            }
                                            // 服务器发申请
                                            OpenfireConnector.editPasword(editText.getText().toString());
                                            // 修改本地
                                            SharedPreferences sp = getActivity().getSharedPreferences("chatchat", Context.MODE_PRIVATE);
                                            sp.edit().putString("password", editText.getText().toString()).commit();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                })
                                .setNegativeButton("CANCEL", null)
                                .create().show();
                        break;
                    case 1:
                        // 关于和分享
                        new AlertDialog.Builder(getActivity())
                                .setView(R.layout.alertdialog_about)
                                .setPositiveButton("OK", null)
                                .setNeutralButton("SHARE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(Intent.ACTION_SEND);
                                        i.setType("text/plain");
                                        i.putExtra(Intent.EXTRA_TEXT, "chatchat, IM based on Openfire\nhttp://swufe.edu.cn");
                                        i = Intent.createChooser(i, "SHARE");
                                        startActivity(i);
                                    }
                                })
                                .create().show();
                        break;
                    case 2:
                        OpenfireConnector.breakConn();
                        getActivity().sendBroadcast(new Intent().setAction(OpenfireConnector.EXIT_ALL));
                        break;
                    case 3:
                        OpenfireConnector.breakConn();
                        sp.edit().remove("username").remove("password").commit();
                        // 强行重启APP，不太好看，能凑活用
                        // 0表示正常退出，1表示非正常退出
                        System.exit(0);
                        break;
                    case 4:
                        // 彻底退出
                        // disconnect connection to server
                        // & Quit login
                        OpenfireConnector.breakConn();
                        // delete information in SharedPreference
                        SharedPreferences sp = getActivity().getSharedPreferences("chatchat", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.remove("username");
                        editor.remove("lastlogintime");
                        editor.remove("password");
                        editor.clear();
                        editor.commit();
                        // clear database
                        ContactDBManager contactDBManager = new ContactDBManager(getActivity());
                        contactDBManager.deleteAll();
                        MessageDBManager messageDBManager = new MessageDBManager(getActivity());
                        messageDBManager.deleteAll();

                        getActivity().sendBroadcast(new Intent().setAction(OpenfireConnector.EXIT_ALL));
                        break;
                }
            }
        });

        return view;
    }
}
