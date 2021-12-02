package com.cometchat.pro.uikit.ui_components.users.user_list;

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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.UsersRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.ui_components.shared.CometChatSnackBar;
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;
import com.cometchat.pro.uikit.ui_resources.utils.CometChatError;
import com.cometchat.pro.uikit.ui_settings.UIKitSettings;
import com.cometchat.pro.uikit.ui_settings.enums.UserMode;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import com.cometchat.pro.uikit.ui_components.shared.cometchatUsers.CometChatUsers;
import com.cometchat.pro.uikit.ui_components.shared.cometchatUsers.CometChatUsersAdapter;
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener;
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils;
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction;
import com.cometchat.pro.uikit.ui_resources.utils.Utils;

/*

* Purpose - CometChatUserList class is a fragment used to display list of users and perform certain action on click of item.
            It also provide search bar to search user from the list.

* @author - CometChat

* @version - v1.0

* Created on - 20th December 2019

* Modified on  - 23rd March 2020

*/

public class CometChatUserList extends Fragment {

    private static final String TAG = "CometChatUserListScreen";

    private static OnItemClickListener events;

    private int LIMIT = 30;

    private Context context;

    private boolean isSearching;

    private CometChatUsersAdapter userListAdapter;

    private UsersRequest usersRequest;    // Use to fetch users

    private CometChatUsers rvUserList;  // Use to display list of users

    private EditText etSearch;    // Use to perform search operation on list of users.

    private ImageView clearSearch;   //Use to clear the search operation performed on list.

    private ShimmerFrameLayout shimmerFrameLayout;

    private TextView title;

    private RelativeLayout rlSearchBox;

    private LinearLayout noUserLayout;

    private List<User> userList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    public CometChatUserList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cometchat_userlist, container, false);
        title = view.findViewById(R.id.tv_title);
        title.setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium));
        rvUserList = view.findViewById(R.id.rv_user_list);
        noUserLayout = view.findViewById(R.id.no_user_layout);
        etSearch = view.findViewById(R.id.search_bar);
        clearSearch = view.findViewById(R.id.clear_search);
        rlSearchBox=view.findViewById(R.id.rl_search_box);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                usersRequest= null;
                rvUserList.clear();
                fetchUsers();
            }
        });
        CometChatError.init(getContext());
        shimmerFrameLayout=view.findViewById(R.id.shimmer_layout);

        if(Utils.isDarkMode(getContext())) {
            title.setTextColor(getResources().getColor(R.color.textColorWhite));
        } else {
            title.setTextColor(getResources().getColor(R.color.primaryTextColor));
        }

        FeatureRestriction.isUserSearchEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (booleanVal)
                    etSearch.setVisibility(View.VISIBLE);
                else
                    etSearch.setVisibility(View.GONE);
            }
        });

        isTitleVisible();
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
                    rvUserList.clear();
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
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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

        // Used to trigger event on click of user item in rvUserList (RecyclerView)
        rvUserList.setItemClickListener(new OnItemClickListener<User>() {
            @Override
            public void OnItemClick(User user, int position) {
                if (events!=null)
                    events.OnItemClick(user,position);
            }
        });
        return view;
    }

    private void isTitleVisible() {
        if (getArguments()!=null) {
            boolean isVisible = getArguments().getBoolean(UIKitConstants.IntentStrings.IS_TITLE_VISIBLE,true);
            if (isVisible)
                title.setVisibility(View.VISIBLE);
            else
                title.setVisibility(View.GONE);
        }
    }

    private void stopHideShimmer() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        rlSearchBox.setVisibility(View.VISIBLE);

    }
    public void setTitleVisible(boolean isVisible) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(UIKitConstants.IntentStrings.IS_TITLE_VISIBLE,isVisible);
        setArguments(bundle);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchUsers();
    }


    /**
     * This method is used to retrieve list of users present in your App_ID.
     * For more detail please visit our official documentation {@link "https://prodocs.cometchat.com/docs/android-users-retrieve-users#section-retrieve-list-of-users" }
     *
     * @see UsersRequest
     */
    private void fetchUsers() {

        if (usersRequest == null) {
            if (UIKitSettings.getUsersMode()==UserMode.FRIENDS)
                usersRequest = new UsersRequest.UsersRequestBuilder().setLimit(30)
                        .friendsOnly(true).build();
            else if (UIKitSettings.getUsersMode()==UserMode.ALL_USER)
                usersRequest = new UsersRequest.UsersRequestBuilder().setLimit(30).build();
        }
        usersRequest.fetchNext(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                Log.e(TAG, "onfetchSuccess: "+users.size() );
                userList.addAll(users);
                stopHideShimmer();
                rvUserList.setUserList(users); // set the users to rvUserList i.e CometChatUserList Component.
                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);

                if (userList.size()==0) {
                    noUserLayout.setVisibility(View.VISIBLE);
                    rvUserList.setVisibility(View.GONE);
                } else {
                    rvUserList.setVisibility(View.VISIBLE);
                    noUserLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: " + e.getMessage());
                stopHideShimmer();
                if (getActivity()!=null)
                    CometChatSnackBar.show(context,rvUserList,CometChatError.localized(e),
                            CometChatSnackBar.ERROR);
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
                rvUserList.searchUserList(users); // set the users to rvUserList i.e CometChatUserList Component.
            }

            @Override
            public void onError(CometChatException e) {
                CometChatSnackBar.show(context,rlSearchBox, CometChatError.localized(e)
                        , CometChatSnackBar.ERROR);
            }
        });
    }


    /**
     *
     * @param onItemClickListener An object of <code>OnItemClickListener&lt;T&gt;</code> abstract class helps to initialize with events
     *                            to perform onItemClick & onItemLongClick,
     * @see OnItemClickListener
     */
    public static void setItemClickListener(OnItemClickListener<User> onItemClickListener) {
        events = onItemClickListener;
    }


    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
