package com.cometchat.pro.uikit.ui_components.calls.call_manager;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar;
import com.cometchat.pro.uikit.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;
import com.cometchat.pro.uikit.ui_resources.utils.AnimUtil;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.helper.CometChatAudioHelper;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.helper.OutgoingAudioHelper;
import com.cometchat.pro.uikit.ui_resources.utils.CallUtils;
import com.cometchat.pro.uikit.ui_resources.utils.camera_preview.CameraPreview;
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction;
import com.cometchat.pro.uikit.ui_resources.utils.Utils;

/**
 * CometChatCallActivity.class is a activity class which is used to load the incoming and outgoing
 * call screens. It is used to handle the audio and video call.
 *
 * Created At : 29th March 2020
 *
 * Modified On : 07th October 2020
 *
 */
public class CometChatCallActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 1;

    private String TAG = CometChatCallActivity.class.getSimpleName();

    private TextView callTv;


    //Incoming Call Screen
    private TextView callerName;

    private TextView callMessage;

    private CometChatAvatar callerAvatar;

    private MaterialButton acceptCall;

    private MaterialButton declineCall;

    private MaterialCardView incomingCallView;
    //

    //Outgoing call
    private RelativeLayout outgoingCallView;

    private TextView userTv;

    private TextView tvDots;

    private CometChatAvatar userAv;

    public RelativeLayout mainView;

    private FloatingActionButton hangUp;

    private FrameLayout cameraFrame;

    private CameraPreview cameraPreview;

    //

    private String sessionId;

    private String avatar;

    private String name;

    private boolean isVideo;

    private boolean isIncoming;

    private boolean isOngoing;

    private Uri notification;

    public static CometChatAudioHelper cometChatAudioHelper;

    public static CometChatCallActivity callActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callActivity = this;
        handleIntent();
        setContentView(R.layout.activity_cometchat_callmanager);
        handleIntent();
        initView();
        setValues();
    }

    /**
     * This method is used to handle the intent values received to this class. Based on this intent
     * value it handles call.
     */
    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(UIKitConstants.IntentStrings.JOIN_ONGOING))
        {
            isOngoing = intent.getBooleanExtra(UIKitConstants.IntentStrings.JOIN_ONGOING,false);
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.ID)) {
            String id = intent.getStringExtra(UIKitConstants.IntentStrings.ID);
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.SESSION_ID)) {
           sessionId = intent.getStringExtra(UIKitConstants.IntentStrings.SESSION_ID);
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.AVATAR)) {
            avatar = intent.getStringExtra(UIKitConstants.IntentStrings.AVATAR);
        }
        if (intent.hasExtra(UIKitConstants.IntentStrings.NAME)) {
            name = intent.getStringExtra(UIKitConstants.IntentStrings.NAME);
        }
        if(!isOngoing) {
            try {
                isVideo = intent.getAction().equals(CometChatConstants.CALL_TYPE_VIDEO);

                isIncoming = intent.getType().equals(UIKitConstants.IntentStrings.INCOMING);

                if (isIncoming)
                    setTheme(R.style.TransparentCompat);
                else
                    setTheme(R.style.AppTheme);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is used to initialize the view of this activity class.
     */
    private void initView() {
        callerName = findViewById(R.id.caller_name);
        callMessage = findViewById(R.id.call_type);
        callerAvatar = findViewById(R.id.caller_av);
        acceptCall = findViewById(R.id.accept_incoming);
        acceptCall.setOnClickListener(this);
        declineCall = findViewById(R.id.decline_incoming);
        declineCall.setOnClickListener(this);
        incomingCallView = findViewById(R.id.incoming_call_view);
        outgoingCallView = findViewById(R.id.outgoing_call_view);
        callTv = findViewById(R.id.calling_tv);
        cameraFrame = findViewById(R.id.camera_frame);
        userTv = findViewById(R.id.user_tv);
        userAv = findViewById(R.id.user_av);
        hangUp = findViewById(R.id.call_hang_btn);
        tvDots = findViewById(R.id.tv_dots);
        hangUp.setOnClickListener(this);
        hangUp.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red_600)));
        mainView = findViewById(R.id.main_view);
        cometChatAudioHelper = new CometChatAudioHelper(this);
        FeatureRestriction.isCallsSoundEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (booleanVal) {
                    cometChatAudioHelper.initAudio();
                    String packageName = getPackageName();
                    notification = Uri.parse("android.resource://" + packageName + "/" + R.raw.incoming_call);
                }
            }
        });
        setCallType(isVideo, isIncoming);
        if (!Utils.hasPermissions(this, Manifest.permission.RECORD_AUDIO) && !Utils.hasPermissions(this,Manifest.permission.CAMERA)) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA},REQUEST_PERMISSION);
        }
    }

    /**
     * This method is used to set the values recieve from <code>handleIntent()</code>.
     */
    private void setValues() {
        if (isOngoing)
        {
            cometChatAudioHelper.stop(false);
            if (CometChat.getActiveCall()!=null)
                CallUtils.startCall(this,CometChat.getActiveCall());
            else
                onBackPressed();
        }
        if (name!=null) {
            userTv.setText(name);
            callerName.setText(name);
            userAv.setInitials(name);
            callerAvatar.setInitials(name);
        }
        if (avatar!=null) {
            userAv.setAvatar(avatar);
            callerAvatar.setAvatar(avatar);
        }
    }

    /**
     * This method is used to set the call type by checking the parameter passed in this method.
     * It also sets the ringtone or calltone based on call type.
     *
     * @param isVideoCall is a boolean, It helps to identify whether call is Audio call or Video Call
     * @param isIncoming is a boolean, It helps to identify whether call is incoming or outgoing.
     *
     * @see CometChatAudioHelper
     */
    public void setCallType(boolean isVideoCall, boolean isIncoming) {

        AnimUtil.blinkAnimation(tvDots);

        if (isIncoming) {
            cometChatAudioHelper.startIncomingAudio(notification, true);
            incomingCallView.setVisibility(View.VISIBLE);
            outgoingCallView.setVisibility(View.GONE);
            if (isVideoCall) {
                callMessage.setText(getResources().getString(R.string.incoming_video_call));
                callMessage.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_incoming_video_call),null,null,null);
            } else {
                callMessage.setText(getResources().getString(R.string.incoming_audio_call));
                callMessage.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_incoming_call),null,null,null);
            }
        } else {
            callTv.setText(getString(R.string.calling));
            cometChatAudioHelper.startOutgoingAudio(OutgoingAudioHelper.Type.IN_COMMUNICATION);
            incomingCallView.setVisibility(View.GONE);
            outgoingCallView.setVisibility(View.VISIBLE);
            hangUp.setVisibility(View.VISIBLE);
            if (isVideoCall) {
                cameraPreview = new CameraPreview(this);
                cameraFrame.addView(cameraPreview);
                hangUp.setImageDrawable(getResources().getDrawable(R.drawable.ic_videocall));

            } else {
                hangUp.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_end_white_24dp));
            }
        }
        if (getSupportActionBar()!=null)
            getSupportActionBar().hide();
    }

    /**
     * This method is used to handle the click events of the views present in this activity.
     *
     * @param v is object of View, It is used to identify the view which is clicked and based on it
     *          perform certain actions.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.call_hang_btn) {
            cometChatAudioHelper.stop(false);
            AnimUtil.stopBlinkAnimation(tvDots);
            rejectCall(sessionId, CometChatConstants.CALL_STATUS_CANCELLED);
        } else if (id == R.id.accept_incoming) {
            cometChatAudioHelper.stop(false);
            incomingCallView.setVisibility(View.GONE);
            answerCall(mainView,sessionId);
        } else if (id == R.id.decline_incoming) {
            cometChatAudioHelper.stop(true);
            rejectCall(sessionId, CometChatConstants.CALL_STATUS_REJECTED);
            finish();
        }
    }

    /**
     * This method is used to reject the call.
     *
     * @param sessionId is a String, It is call session Id.
     * @param callStatus is a String, It the reason for call being rejected.
     *
     * @see CometChat#rejectCall(String, String, CometChat.CallbackListener)
     * @see Call
     */
    private void rejectCall(String sessionId, String callStatus) {
        CometChat.rejectCall(sessionId,callStatus, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                finish();
            }

            @Override
            public void onError(CometChatException e) {
                finish();
                Log.e(TAG, "onErrorReject: "+e.getMessage()+" "+e.getCode());
                Toast.makeText(CometChatCallActivity.this,"Unable to end call",Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This method is used to accept the incoming call receievd.
     *
     * @param mainView is a object of Relativelayout, It is used to load the CallingComponent after
     *                 the call is accepted.
     * @param sessionId is a String, It is sessionId of call.
     *
     * @see CometChat#acceptCall(String, CometChat.CallbackListener)
     * @see Call
     */
    private void answerCall(RelativeLayout mainView, String sessionId) {
        CometChat.acceptCall(sessionId, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                Log.e("CallMeta",call.toString());
                startCall(mainView,call);
            }

            @Override
            public void onError(CometChatException e) {
                finish();
                Log.e(TAG, "onErrorAccept: "+e.getMessage()+" "+e.getCode());
            }

        });
    }

    /**
     * This method is used to start the call after the call is accepted from both the end.
     * Here we are calling <code>Utils.startCall()</code> as it is being used for other purpose
     * also.
     * @param mainView is a object of RelativeLayout where the Calling Component will be loaded.
     * @param call is a object of Call.
     *
     * @see CometChat#startCall(Activity, String, RelativeLayout, CometChat.OngoingCallListener)
     */
    private void startCall(RelativeLayout mainView,Call call) {
        hangUp.setVisibility(View.GONE);
        CallUtils.startCall(CometChatCallActivity.this,call);
    }

    public void startOnGoingCall(Call call) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cometChatAudioHelper.stop(false);
    }

}
