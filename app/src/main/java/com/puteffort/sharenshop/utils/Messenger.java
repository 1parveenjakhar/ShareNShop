package com.puteffort.sharenshop.utils;

import static com.puteffort.sharenshop.utils.UtilFunctions.showToast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI;
import com.cometchat.pro.uikit.ui_components.groups.add_members.CometChatAddMembers;
import com.cometchat.pro.uikit.ui_components.groups.group_list.CometChatGroupList;
import com.cometchat.pro.uikit.ui_settings.UIKitSettings;
import com.cometchat.pro.uikit.ui_settings.enums.ConversationMode;
import com.cometchat.pro.uikit.ui_settings.enums.GroupMode;
import com.cometchat.pro.uikit.ui_settings.enums.UserMode;
import com.puteffort.sharenshop.LoginActivity;
import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.fragments.AddedRecyclerView;
import com.puteffort.sharenshop.models.PostDetailInfo;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.UserStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Messenger {

    public static void init(Context context) {
        AppSettings appSettings=new AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(Constants.region).build();
        CometChat.init(context, Constants.appId,appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
                init_settings();
            }
            @Override
            public void onError(CometChatException e) {

            }
        });
    }

    public static void init_settings() {
        //Tab bar setting
        UIKitSettings.conversations(true); // recent conversations
        UIKitSettings.calls(false);
        UIKitSettings.users(false);
        UIKitSettings.userSettings(true);

        //Call settings
        UIKitSettings.userAudioCall(true);
        UIKitSettings.userVideoCall(true);
        UIKitSettings.groupVideoCall(true);
        UIKitSettings.enableSoundForCalls(true);
        UIKitSettings.callNotification(true);

        //User Settings
        UIKitSettings.setConversationsMode(ConversationMode.GROUP);
        UIKitSettings.setUsersMode(UserMode.ALL_USER);
        UIKitSettings.setGroupsMode(GroupMode.ALL_GROUP);
        UIKitSettings.showUserPresence(true);
        UIKitSettings. blockUser(true);

        //Group settings
        UIKitSettings.groupCreate(false); // we will create group programmatically
        UIKitSettings. joinOrLeaveGroup(true);
        UIKitSettings.allowDeleteGroups(true);
        UIKitSettings.viewGroupMembers(true);
        UIKitSettings.allowAddMembers(true);
        UIKitSettings.allowModeratorToDeleteMemberMessages(true);
        UIKitSettings.kickMember(true);
        UIKitSettings.banMember(true);
        UIKitSettings.groupNotifications(true);

        //Message Settings
        UIKitSettings.sendPhotosVideo(true);
        UIKitSettings.sendFiles(true);
        UIKitSettings.sendVoiceNotes(true);
        UIKitSettings.sendEmojis(true);
        UIKitSettings.sendEmojisInLargeSize(true);
        UIKitSettings.sendTypingIndicators(true);
        UIKitSettings.editMessage(false);
        UIKitSettings.deleteMessage(true);
        UIKitSettings.shareCopyForwardMessage(true);
        UIKitSettings.replyingToMessage(true);
        UIKitSettings.threadedChats(true);
        UIKitSettings.sendLiveReaction(true);
        UIKitSettings.shareLocation(true);
        UIKitSettings.viewSharedMedia(true);
        UIKitSettings.showReadDeliveryReceipts(true);
        UIKitSettings.sendPolls(true);
        UIKitSettings.sendMessageReaction(true);
        UIKitSettings.collaborativeWhiteBoard(true);
        UIKitSettings.collaborativeDocument(true);

        UIKitSettings.messageHistory(true);
    }

    public static void login(String UID, Activity activity) {

        CometChat.login(UID, Constants.authKey, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
//                showToast(activity, "Chat login succesful: "+user.getName());
                //Log.d(TAG, "Login Successful : " + user.toString());
            }

            @Override
            public void onError(CometChatException e) {
                //showToast(activity, "Chat login failed: "+e.getMessage());
            }
        });
    }


    public static void logout() {
        CometChat.logout(new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
                //Log.d(TAG, "Logout completed successfully");
            }
            @Override
            public void onError(CometChatException e) {
                //Log.d(TAG, "Logout failed with exception: " + e.getMessage());
            }
        });
    }

}
