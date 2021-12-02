package com.cometchat.pro.uikit.ui_components.users.block_users;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cometchat.pro.uikit.R;

import com.cometchat.pro.uikit.ui_settings.FeatureRestriction;
import com.cometchat.pro.uikit.ui_settings.UIKitSettings;

public class CometChatBlockUserListActivity extends AppCompatActivity {

    private Fragment fragment;

    private String guid;

    private String loggedInUserScope;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        Fragment fragment = new CometChatBlockUserList();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragment,fragment).commit();
        if (UIKitSettings.getColor()!=null)
            getWindow().setStatusBarColor(Color.parseColor(UIKitSettings.getColor()));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


}
