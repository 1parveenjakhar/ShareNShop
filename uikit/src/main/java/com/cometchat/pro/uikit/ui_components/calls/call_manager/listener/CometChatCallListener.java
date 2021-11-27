package com.cometchat.pro.uikit.ui_components.calls.call_manager.listener;


import android.content.Context;
import android.widget.Toast;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;

import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatCallActivity;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatStartCallActivity;
import com.cometchat.pro.uikit.ui_resources.utils.CallUtils;

/**
 * CometChatCallListener.class is used to add and remove CallListener in app.
 * It also has method to make call to user passed in parameter;
 */
public class CometChatCallListener {

    public static boolean isInitialized;

    /**
     * This method is used to add CallListener in app
     * @param TAG is a unique Identifier
     * @param context is a object of Context.
     */
    public static void addCallListener(String TAG,Context context)
    {
        isInitialized = true;
        CometChat.addCallListener(TAG, new CometChat.CallListener() {
            @Override
            public void onIncomingCallReceived(Call call) {
                if (CometChat.getActiveCall()==null) {
                    if (call.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        CallUtils.startCallIntent(context, (User) call.getCallInitiator(), call.getType(),
                                false, call.getSessionId());
                    } else {
                        CallUtils.startGroupCallIntent(context, (Group) call.getReceiver(), call.getType(),
                                false, call.getSessionId());
                    }
                } else {
                    CometChat.rejectCall(call.getSessionId(), CometChatConstants.CALL_STATUS_BUSY, new CometChat.CallbackListener<Call>() {
                        @Override
                        public void onSuccess(Call call) {
                            Toast.makeText(context,call.getSender().getName()+" tried to call you",Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(CometChatException e) {
                            Toast.makeText(context,"Error:"+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onOutgoingCallAccepted(Call call) {
                if(CometChatStartCallActivity.activity==null) {
                    if (CometChatCallActivity.callActivity != null) {
                        CometChatCallActivity.cometChatAudioHelper.stop(false);
                        CallUtils.startCall(CometChatCallActivity.callActivity, call);
                    }
                } else {
                    CometChatStartCallActivity.activity.finish();
                    if (CometChatCallActivity.callActivity != null) {
                        CometChatCallActivity.cometChatAudioHelper.stop(false);
                        CallUtils.startCall(CometChatCallActivity.callActivity, call);
                    }
                }
            }

            @Override
            public void onOutgoingCallRejected(Call call) {
                if (CometChatCallActivity.callActivity!=null)
                    CometChatCallActivity.callActivity.finish();
            }

            @Override
            public void onIncomingCallCancelled(Call call){
                if (CometChatCallActivity.callActivity!=null)
                    CometChatCallActivity.callActivity.finish();
            }
        });
    }

    /**
     * It is used to remove call listener from app.
     * @param TAG is a unique Identifier
     */
    public static void removeCallListener(String TAG) {

        isInitialized = false;
        CometChat.removeCallListener(TAG);
    }

    /**
     * This method is used to make a initiate a call.
     * @param context is a object of Context.
     * @param receiverId is a String, It is unique receiverId. It can be either uid of user or
     *                   guid of group
     * @param receiverType is a String, It can be either CometChatConstant.RECEIVER_TYPE_USER or
     *                     CometChatConstant.RECEIVER_TYPE_GROUP
     * @param callType is a String, It is call type which can be either CometChatConstant.CALL_TYPE_AUDIO
     *                 or CometChatConstant.CALL_TYPE_VIDEO
     *
     * @see CometChat#initiateCall(Call, CometChat.CallbackListener)
     *
     */
    public static void makeCall(Context context, String receiverId, String receiverType, String callType) {
        CallUtils.initiateCall(context,receiverId,receiverType,callType);
    }
}
