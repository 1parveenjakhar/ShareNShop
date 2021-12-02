package com.cometchat.pro.uikit.ui_components.cometchat_ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;
import androidx.fragment.app.Fragment;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Conversation;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.databinding.ActivityCometchatUnifiedBinding;
import com.cometchat.pro.uikit.ui_components.shared.CometChatSnackBar;
import com.cometchat.pro.uikit.ui_resources.utils.CometChatError;
import com.cometchat.pro.uikit.ui_resources.utils.EncryptionUtils;
import com.cometchat.pro.uikit.ui_settings.UIKitSettings;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.cometchat.pro.uikit.ui_components.calls.call_list.CometChatCallList;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.listener.CometChatCallListener;
import com.cometchat.pro.uikit.ui_components.chats.CometChatConversationList;
import com.cometchat.pro.uikit.ui_components.groups.group_list.CometChatGroupList;
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity;
import com.cometchat.pro.uikit.ui_components.userprofile.CometChatUserProfile;
import com.cometchat.pro.uikit.ui_components.users.user_list.CometChatUserList;
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;
import com.cometchat.pro.uikit.ui_resources.utils.custom_alertDialog.CustomAlertDialogHelper;
import com.cometchat.pro.uikit.ui_resources.utils.custom_alertDialog.OnAlertDialogButtonClickListener;
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener;
import com.cometchat.pro.uikit.ui_resources.utils.Utils;
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Purpose - CometChatUnified class is main class used to launch the fully working chat application.
 * It consist of BottomNavigationBar which helps to navigate between different screens like
 * ConversationListScreen, UserListScreen, GroupListScreen, MoreInfoScreen.
 * @link= "https://prodocs.cometchat.com/docs/android-ui-unified"
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 */
public class CometChatUI extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener,OnAlertDialogButtonClickListener {

    //Used to bind the layout with class
    private static ActivityCometchatUnifiedBinding activityCometChatUnifiedBinding;

    //Used to identify class in Log's
    private static final String TAG = CometChatUI.class.getSimpleName();

    //Stores the count of user whose messages are unread.
    private List<String> unreadCount = new ArrayList<>();

    private BadgeDrawable badgeDrawable;

    private Fragment fragment;

    private ProgressDialog progressDialog;

    private String groupPassword;

    private Group group;

    private Fragment active = new CometChatConversationList();

    private boolean isUserListVisible;
    private boolean isConversationVisible;
    private boolean isSettingsVisible;
    private boolean isCallsListVisible;
    private boolean isGroupsListVisible;

    @VisibleForTesting
    public static AppCompatActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        CometChatError.init(this);
        if (!CometChatCallListener.isInitialized)
            CometChatCallListener.addCallListener(TAG,this);

        EmojiCompat.Config config = new BundledEmojiCompatConfig(getApplicationContext());
        EmojiCompat.init(config);
        activityCometChatUnifiedBinding = DataBindingUtil.setContentView(this, R.layout.activity_cometchat_unified);
        initViewComponent();
        // It performs action on click of user item in CometChatUserListScreen.
        setUserClickListener();

        //It performs action on click of group item in CometChatGroupListScreen.
        //It checks whether the logged-In user is already a joined a group or not and based on it perform actions.
        setGroupClickListener();

        //It performs action on click of conversation item in CometChatConversationListScreen
        //Based on conversation item type it will perform the actions like open message screen for user and groups..
        setConversationClickListener();
    }

    private void setConversationClickListener() {
        CometChatConversationList.setItemClickListener(new OnItemClickListener<Conversation>() {
            @Override
            public void OnItemClick(Conversation conversation, int position) {
                if (conversation.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_GROUP))
                    startGroupIntent(((Group) conversation.getConversationWith()));
                else
                    startUserIntent(((User) conversation.getConversationWith()));
            }
        });
    }

    private void setGroupClickListener() {
        CometChatGroupList.setItemClickListener(new OnItemClickListener<Group>() {
            @Override
            public void OnItemClick(Group g, int position) {
                group = g;
                if (group.isJoined()) {
                    startGroupIntent(group);
                } else {
                    if (group.getGroupType().equals(CometChatConstants.GROUP_TYPE_PASSWORD)) {
                        View dialogview = getLayoutInflater().inflate(R.layout.cc_dialog, null);
                        TextView tvTitle = dialogview.findViewById(R.id.textViewDialogueTitle);
                        tvTitle.setText(String.format(getResources().getString(R.string.enter_password_to_join),group.getName()));
                        new CustomAlertDialogHelper(CometChatUI.this, getResources().getString(R.string.password), dialogview, getResources().getString(R.string.join),
                                "", getResources().getString(R.string.cancel), CometChatUI.this, 1, false);
                    } else if (group.getGroupType().equals(CometChatConstants.GROUP_TYPE_PUBLIC)) {
                        joinGroup(group);
                    }
                }
            }
        });
    }

    private void setUserClickListener() {
        CometChatUserList.setItemClickListener(new OnItemClickListener<User>() {
            @Override
            public void OnItemClick(User user, int position) {
                startUserIntent(user);
            }
        });
    }

    /**
     * This method initialize the BadgeDrawable which is used on conversation menu of BottomNavigationBar to display unread conversations.
     * It Loads <b>CometChatConversationScreen</b> at initial phase.
     * @see CometChatConversationList
     */
    private void initViewComponent() {

        if (!Utils.hasPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        UIKitConstants.RequestCode.RECORD);
            }
        }
        badgeDrawable = activityCometChatUnifiedBinding.bottomNavigation.getOrCreateBadge(R.id.menu_conversation);

        activityCometChatUnifiedBinding.bottomNavigation.setOnNavigationItemSelectedListener(this);

        if (UIKitSettings.getColor()!=null && !UIKitSettings.getColor().isEmpty()) {
            getWindow().setStatusBarColor(Color.parseColor(UIKitSettings.getColor()));
            int widgetColor = Color.parseColor(UIKitSettings.getColor());
            ColorStateList colorStateList = new ColorStateList(new int[][] {
                    { -android.R.attr.state_selected }, {} }, new int[] { Color.GRAY, widgetColor });

            activityCometChatUnifiedBinding.bottomNavigation.setItemIconTintList(colorStateList);

        }
//        activityCometChatUnifiedBinding.bottomNavigation.getMenu().add(Menu.NONE,12,Menu.NONE,"Test").setIcon(R.drawable.ic_security_24dp);
        FeatureRestriction.isConversationListEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isConversationVisible = booleanVal;
                activityCometChatUnifiedBinding.bottomNavigation.getMenu().findItem(R.id.menu_conversation)
                        .setVisible(booleanVal);
            }
        });

        FeatureRestriction.isUserListEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isUserListVisible = booleanVal;
                activityCometChatUnifiedBinding.bottomNavigation.getMenu().findItem(R.id.menu_users)
                        .setVisible(booleanVal);
            }
        });

        FeatureRestriction.isGroupListEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isGroupsListVisible = booleanVal;
                activityCometChatUnifiedBinding.bottomNavigation.getMenu().findItem(R.id.menu_group)
                        .setVisible(booleanVal);

            }
        });

        FeatureRestriction.isCallListEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isCallsListVisible = booleanVal;
                activityCometChatUnifiedBinding.bottomNavigation.getMenu().findItem(R.id.menu_call)
                        .setVisible(booleanVal);
            }
        });

        FeatureRestriction.isUserSettingsEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isSettingsVisible = booleanVal;
                activityCometChatUnifiedBinding.bottomNavigation.getMenu().findItem(R.id.menu_more)
                        .setVisible(booleanVal);
            }
        });
        
        badgeDrawable.setVisible(false);
        if (isConversationVisible)
            loadFragment(new CometChatConversationList());
        else if (isCallsListVisible)
            loadFragment(new CometChatCallList());
        else if (isUserListVisible)
            loadFragment(new CometChatUserList());
        else if (isGroupsListVisible)
            loadFragment(new CometChatGroupList());
        else if (isSettingsVisible)
            loadFragment(new CometChatUserProfile());
    }

    /**
     * This methods joins the logged-In user in a group.
     *
     * @param group  The Group user will join.
     * @see Group
     * @see CometChat#joinGroup(String, String, String, CometChat.CallbackListener)
     *
     */
    private void joinGroup(Group group) {
        FeatureRestriction.isJoinOrLeaveGroupEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (booleanVal) {
                    progressDialog = ProgressDialog.show(CometChatUI.this, "", getResources().getString(R.string.joining));
                    progressDialog.setCancelable(false);
                    CometChat.joinGroup(group.getGuid(), group.getGroupType(), groupPassword, new CometChat.CallbackListener<Group>() {
                        @Override
                        public void onSuccess(Group group) {
                            if (progressDialog != null)
                                progressDialog.dismiss();

                            if (group != null)
                                startGroupIntent(group);
                        }

                        @Override
                        public void onError(CometChatException e) {
                            if (progressDialog != null)
                                progressDialog.dismiss();

                            CometChatSnackBar.show(CometChatUI.this,
                                    activityCometChatUnifiedBinding.bottomNavigation,
                                    CometChatError.localized(e), CometChatSnackBar.ERROR);
                        }
                    });
                }
            }
        });
    }

    /**
     * Loads the fragment get from parameter.
     * @param fragment
     * @return true if fragment is not null
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
            return true;
        }
        return false;
    }

    /**
     * Get Unread Count of conversation using <code>CometChat.getUnreadMessageCount()</code>.
     * @see CometChat#getUnreadMessageCount(CometChat.CallbackListener)
     */
    public void getUnreadConversationCount() {
        FeatureRestriction.isUnreadCountEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (booleanVal) {
                    CometChat.getUnreadMessageCount(new CometChat.CallbackListener<HashMap<String, HashMap<String, Integer>>>() {
                        @Override
                        public void onSuccess(HashMap<String, HashMap<String, Integer>> stringHashMapHashMap) {
                            Log.e(TAG, "onSuccess: " + stringHashMapHashMap);
                            unreadCount.clear();
                            unreadCount.addAll(stringHashMapHashMap.get("user").keySet());    //Add users whose messages are unread.
                            unreadCount.addAll(stringHashMapHashMap.get("group").keySet());    //Add groups whose messages are unread.

                            if (unreadCount.size() == 0) {
                                badgeDrawable.setVisible(false);
                            } else {
                                badgeDrawable.setVisible(true);
                            }
                            if (unreadCount.size() != 0) {
                                badgeDrawable.setNumber(unreadCount.size());  //add total count of users and groups whose messages are unread in BadgeDrawable
                            }
                        }

                        @Override
                        public void onError(CometChatException e) {
                            Log.e("CometChatUI : onError: ", e.getMessage());     //Logs the error if the error occurs.
                        }
                    });
                }
            }
        });
    }

    /**
     * Set unread message count
     * @param message An object of <b>BaseMessage</b> class that is been used to set unread count in BadgeDrawable.
     * @see BaseMessage
     */
    private void setUnreadCount(BaseMessage message) {

        if (message.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
            if (!unreadCount.contains(message.getReceiverUid())) {
                unreadCount.add(message.getReceiverUid());
                setBadge();
            }
        } else {

            if (!unreadCount.contains(message.getSender().getUid())) {
                unreadCount.add(message.getSender().getUid());
                setBadge();
            }
        }
    }


    /**
     * Updating BadgeDrawable set on conversation menu in BottomNavigationBar
     */
    private void setBadge(){
        if (unreadCount.size()==0){
            badgeDrawable.setVisible(false);
        } else
            badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(badgeDrawable.getNumber() + 1);
    }

    /**
     * MessageListener to update unread count of conversations
     * @see CometChat#addMessageListener(String, CometChat.MessageListener)
     */
    public void addConversationListener() {
        CometChat.addMessageListener(TAG, new CometChat.MessageListener() {
            @Override
            public void onTextMessageReceived(TextMessage message) {
                setUnreadCount(message);
            }

            @Override
            public void onMediaMessageReceived(MediaMessage message) {
                setUnreadCount(message);
            }

            @Override
            public void onCustomMessageReceived(CustomMessage message) {
                setUnreadCount(message);
            }
        });
    }


    /**
     * Open Message Screen for user using <b>CometChatMessageListActivity.class</b>
     *
     * @param user
     * @see CometChatMessageListActivity
     */
    private void startUserIntent(User user) {
        Intent intent = new Intent(CometChatUI.this, CometChatMessageListActivity.class);
        intent.putExtra(UIKitConstants.IntentStrings.UID, user.getUid());
        intent.putExtra(UIKitConstants.IntentStrings.AVATAR, user.getAvatar());
        intent.putExtra(UIKitConstants.IntentStrings.STATUS, user.getStatus());
        intent.putExtra(UIKitConstants.IntentStrings.NAME, user.getName());
        intent.putExtra(UIKitConstants.IntentStrings.LINK,user.getLink());
        intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_USER);
        startActivity(intent);
    }

    /**
     * Open Message Screen for group using <b>CometChatMessageListActivity.class</b>
     *
     * @param group
     * @see CometChatMessageListActivity
     */
    private void startGroupIntent(Group group) {
        Intent intent = new Intent(CometChatUI.this, CometChatMessageListActivity.class);
        intent.putExtra(UIKitConstants.IntentStrings.GUID, group.getGuid());
        intent.putExtra(UIKitConstants.IntentStrings.AVATAR, group.getIcon());
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER,group.getOwner());
        intent.putExtra(UIKitConstants.IntentStrings.NAME, group.getName());
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_TYPE,group.getGroupType());
        intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_GROUP);
        intent.putExtra(UIKitConstants.IntentStrings.MEMBER_COUNT,group.getMembersCount());
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_DESC,group.getDescription());
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD,group.getPassword());
        startActivity(intent);
    }

    /**
     * Open various screen on fragment based on item selected from BottomNavigationBar
     * @param item
     * @return true if fragment is not null.
     * @see CometChatUserList
     * @see CometChatGroupList
     * @see CometChatConversationList
     * @see CometChatUserProfile
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Fragment fragment = null;
        if (itemId == R.id.menu_users) {
            fragment = new CometChatUserList();
        } else if (itemId == R.id.menu_group) {
            fragment = new CometChatGroupList();
        } else if (itemId == R.id.menu_conversation) {
          fragment = new CometChatConversationList();
        } else if (itemId == R.id.menu_more) {
            fragment = new CometChatUserProfile();
        } else if (itemId == R.id.menu_call) {
            fragment = new CometChatCallList();
        }

        return loadFragment(fragment);
    }

    @Override
    public void onButtonClick(AlertDialog alertDialog, View v, int which, int popupId) {
        EditText groupPasswordInput = (EditText) v.findViewById(R.id.edittextDialogueInput);
        if (which == DialogInterface.BUTTON_NEGATIVE) { // Cancel
            alertDialog.dismiss();
        } else if (which == DialogInterface.BUTTON_POSITIVE) { // Join
            try {
                groupPassword = groupPasswordInput.getText().toString();
                if (groupPassword.length() == 0) {
                    groupPasswordInput.setText("");
                    groupPasswordInput.setError(getResources().getString(R.string.incorrect));

                } else {
                    try {
                        alertDialog.dismiss();
                        joinGroup(group);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        addConversationListener();    //Enable Listener when app starts

    }

    @Override
    protected void onResume() {
        super.onResume();
        getUnreadConversationCount();    // To get unread conversations count
    }

    @Override
    protected void onPause() {
        super.onPause();
        badgeDrawable.clearNumber();
        unreadCount.clear();    //Clear conversation count when app pauses or goes background.
    }

    @VisibleForTesting
    public static ActivityCometchatUnifiedBinding getBinding() {
        return activityCometChatUnifiedBinding;
    }

    @VisibleForTesting
    public static AppCompatActivity getCometChatUIActivity() {
        return activity;
    }

    public static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

}