package com.cometchat.pro.uikit.ui_components.calls.call_list;

/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.UsersRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.ui_components.shared.CometChatSnackBar;
import com.cometchat.pro.uikit.ui_resources.utils.CometChatError;
import com.cometchat.pro.uikit.ui_settings.UIKitSettings;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

import com.cometchat.pro.uikit.ui_components.shared.cometchatUsers.CometChatUsers;
import com.cometchat.pro.uikit.ui_resources.utils.CallUtils;
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils;
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener;
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction;

/**

* Purpose - CometChatUserCallListScreenActivity class is a activity used to display list of users
 *          and perform call operation on click of item.It also provide search bar to search user
 *          from the list.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 *
*/

public class CometChatNewCallList extends AppCompatActivity {

    private static final String TAG = "CometChatUserCallList";

    private int LIMIT = 30;

    private UsersRequest usersRequest;    // Use to fetch users

    private CometChatUsers rvUserList;

    private EditText etSearch;    // Use to perform search operation on list of users.

    private ImageView clearSearch;   //Use to clear the search operation performed on list.

    private ShimmerFrameLayout shimmerFrameLayout;

    private TextView title;

    private RelativeLayout rlSearchBox;

    private boolean audioCallEnabled;
    private boolean videoCallEnabled;

    public CometChatNewCallList() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cometchat_userlist);
        fetchSettings();
        title = findViewById(R.id.tv_title);
        CometChatError.init(this);
        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_back_arrow_selected));
        if (UIKitSettings.getColor()!=null) {
            getWindow().setStatusBarColor(Color.parseColor(UIKitSettings.getColor()));
            imageView.setImageTintList(ColorStateList.valueOf(
                    Color.parseColor(UIKitSettings.getColor())));
        } else
            imageView.setImageTintList(
                    ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));

        imageView.setClickable(true);
        imageView.setPadding(8,8,8,8);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_START);
        layoutParams.setMargins(16,32,16,16);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        imageView.setLayoutParams(layoutParams);
        addContentView(imageView,layoutParams);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLayoutParams.setMargins(16,32,16,48);
        titleLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        title.setLayoutParams(titleLayoutParams);
        title.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        title.setText(getResources().getString(R.string.new_call));
        rvUserList = findViewById(R.id.rv_user_list);
        etSearch = findViewById(R.id.search_bar);
        clearSearch = findViewById(R.id.clear_search);
        rlSearchBox=findViewById(R.id.rl_search_box);

        shimmerFrameLayout=findViewById(R.id.shimmer_layout);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()==0) {
                    // if etSearch is empty then fetch all users.
                    usersRequest=null;
                    fetchUsers();
                }
                else {
                    // Search users based on text in etSearch field.
                    searchUser(editable.toString());
                }
            }
        });


        etSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchUser(textView.getText().toString());
                    clearSearch.setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etSearch.setText("");
                clearSearch.setVisibility(View.GONE);
                searchUser(etSearch.getText().toString());
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                // Hide the soft keyboard
                inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        });


        // Uses to fetch next list of user if rvUserList (RecyclerView) is scrolled in upward direction.
        rvUserList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (!recyclerView.canScrollVertically(1)) {
                    fetchUsers();
                }

            }
        });

        rvUserList.setItemClickListener(new OnItemClickListener<User>() {
                @Override
                public void OnItemClick(User var, int position) {
                    User user = var;
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(CometChatNewCallList.this);
                    alertDialog.setMessage(getString(R.string.initiate_a_call));
                    if (audioCallEnabled) {
                        alertDialog.setPositiveButton(getString(R.string.audio_call), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                initiateCall(user.getUid(), CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_AUDIO);
                            }
                        });
                    }
                    if (videoCallEnabled) {
                        alertDialog.setNegativeButton(getString(R.string.video_call), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                initiateCall(user.getUid(), CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_VIDEO);
                            }
                        });
                    }
                    alertDialog.create();
                    alertDialog.show();
                }
            });

        fetchUsers();
    }

    private void stopHideShimmer() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        title.setVisibility(View.VISIBLE);
        rlSearchBox.setVisibility(View.VISIBLE);
    }

    /**
     * This method is used to retrieve list of users present in your App_ID.
     * For more detail please visit our official documentation {@link "https://prodocs.cometchat.com/docs/android-users-retrieve-users#section-retrieve-list-of-users" }
     *
     * @see UsersRequest
     */
    private void fetchUsers() {

        if (usersRequest == null) {
            Log.e(TAG, "newfetchUsers: " );
            usersRequest = new UsersRequest.UsersRequestBuilder().setLimit(30).build();
        }
        usersRequest.fetchNext(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                Log.e(TAG, "onfetchSuccess: "+users.size() );
                stopHideShimmer();
                rvUserList.setUserList(users);
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: " + e.getMessage());
                stopHideShimmer();
                CometChatSnackBar.show(CometChatNewCallList.this,rvUserList,
                        CometChatError.localized(e),CometChatSnackBar.ERROR);
            }
        });
    }

    /**
     * This method is used to search users present in your App_ID.
     * For more detail please visit our official documentation {@link "https://prodocs.cometchat.com/docs/android-users-retrieve-users#section-retrieve-list-of-users" }
     *
     * @param s is a string used to get users matches with this string.
     * @see UsersRequest
     */
    private void searchUser(String s) {
        UsersRequest usersRequest = new UsersRequest.UsersRequestBuilder().setSearchKeyword(s).setLimit(100).build();
        usersRequest.fetchNext(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                rvUserList.searchUserList(users);
            }

            @Override
            public void onError(CometChatException e) {
                if (rvUserList!=null)
                    CometChatSnackBar.show(CometChatNewCallList.this,rvUserList,
                            CometChatError.localized(e),CometChatSnackBar.ERROR);
            }
        });
    }

    public void initiateCall(String receiverID, String receiverType, String callType)
    {
        CallUtils.initiateCall(CometChatNewCallList.this,receiverID,receiverType,callType);
    }

    private void fetchSettings() {
        FeatureRestriction.isOneOnOneVideoCallEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                videoCallEnabled = booleanVal;
            }
        });
        FeatureRestriction.isOneOnOneAudioCallEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                audioCallEnabled = booleanVal;
            }
        });
    }
}
