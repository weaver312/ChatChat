package com.weaverhong.lesson.chatchat.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.weaverhong.lesson.chatchat.Activity_Autoshutdown.BaseAppCompatActivity;
import com.weaverhong.lesson.chatchat.DB.ContactDBManager;
import com.weaverhong.lesson.chatchat.ListItem.ChatListItem;
import com.weaverhong.lesson.chatchat.OpenfireConnector;
import com.weaverhong.lesson.chatchat.R;

public class UserActivity extends BaseAppCompatActivity {

    TextView mUsername;
    TextView mChatwith;
    TextView mDeleteuser;
    Context mContext;

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, UserActivity.class);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.toolbar_logo);

        mUsername = findViewById(R.id.user_username);
        mChatwith = findViewById(R.id.user_chatwith);
        mDeleteuser = findViewById(R.id.user_delete);
        mContext = this;

        String username = getIntent().getStringExtra("username");
        mUsername.setText(username==null?"":username);
        mChatwith.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ChatActivity.newIntent(mContext, new ChatListItem(username,""), OpenfireConnector.username);
                startActivity(intent);
            }
        });
        mDeleteuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(UserActivity.this)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // delete from local DB
                                new ContactDBManager(mContext).delete(mUsername.getText().toString());
                                // delete from server
                                try {
                                    OpenfireConnector.deleteFriend(mUsername.getText().toString());
                                    Toast.makeText(mContext,"Delete completed!", Toast.LENGTH_SHORT);
                                } catch (Exception e) {
                                    Log.e("UserActivity-deleteuser", e.toString());
                                }
                                finish();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) { return; }
                        })
                        .setTitle("Delete '" + mUsername.getText().toString() + "' from your roster?")
                        .setView(R.layout.alertdialog_deleteuser)
                        .create();
                alertDialog.show();
            }
        });

    }
}
