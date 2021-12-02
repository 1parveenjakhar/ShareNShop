package com.cometchat.pro.uikit.ui_components.shared.cometchatComposeBox;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.fragment.app.FragmentManager;

import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.uikit.R;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;
import com.cometchat.pro.uikit.ui_components.shared.cometchatComposeBox.listener.ComposeActionListener;
import com.cometchat.pro.uikit.ui_resources.utils.audio_visualizer.AudioRecordView;
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction;
import com.cometchat.pro.uikit.ui_resources.utils.Utils;
import com.cometchat.pro.uikit.ui_settings.UIKitSettings;

public class CometChatComposeBox extends RelativeLayout implements View.OnClickListener {

    private static final String TAG = CometChatComposeBox.class.getName();

    private AudioRecordView audioRecordView;

    private MediaRecorder mediaRecorder;

    private MediaPlayer mediaPlayer;

    private Runnable timerRunnable;

    private Handler seekHandler = new Handler(Looper.getMainLooper());

    private Timer timer = new Timer();

    private CometChatComposeBoxActions composeBoxActionFragment;

    private String audioFileNameWithPath;

    private boolean isOpen,isRecording,isPlaying,voiceMessage;

    public ImageView ivSend,ivArrow,ivMic,ivDelete;

    private SeekBar voiceSeekbar;

    private Chronometer recordTime;

    public CometChatEditText etComposeBox;

    private RelativeLayout composeBox;

    private RelativeLayout flBox;

    private RelativeLayout voiceMessageLayout;

    private RelativeLayout rlActionContainer;

    private boolean hasFocus;

    private ComposeActionListener composeActionListener;

    private Context context;

    private int color;

    public ImageView liveReactionBtn;

    private Bundle bundle = new Bundle();

    public boolean isGalleryVisible = true,isAudioVisible = true,isCameraVisible = true,
            isFileVisible = true,isLocationVisible = true,isPollVisible = true,isStickerVisible = true,
            isWhiteBoardVisible = true, isWriteBoardVisible = true, isGroupCallVisible = true;

    public CometChatComposeBox(Context context) {
        super(context);
        initViewComponent(context,null,-1,-1);
    }

    public CometChatComposeBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViewComponent(context,attrs,-1,-1);
    }

    public CometChatComposeBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViewComponent(context,attrs,defStyleAttr,-1);
    }

    private void initViewComponent(Context context,AttributeSet attributeSet,int defStyleAttr,int defStyleRes){

        View view =View.inflate(context, R.layout.cometchat_compose_box,null);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(attributeSet, R.styleable.ComposeBox, 0, 0);
        color = a.getColor(R.styleable.ComposeBox_color,getResources().getColor(R.color.colorPrimary));
        addView(view);

        this.context=context;

        ViewGroup viewGroup=(ViewGroup)view.getParent();
        viewGroup.setClipChildren(false);

        mediaPlayer = new MediaPlayer();
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(audioManager.isMusicActive())
        {
            audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        stopRecording(true);
                    }
                }
            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        liveReactionBtn = view.findViewById(R.id.live_reaction_btn);
        composeBox=this.findViewById(R.id.message_box);
        flBox=this.findViewById(R.id.flBox);
        ivMic=this.findViewById(R.id.ivMic);
        ivDelete=this.findViewById(R.id.ivDelete);
        audioRecordView=this.findViewById(R.id.record_audio_visualizer);
        voiceMessageLayout=this.findViewById(R.id.voiceMessageLayout);
        recordTime=this.findViewById(R.id.record_time);
        voiceSeekbar=this.findViewById(R.id.voice_message_seekbar);
        ivArrow=this.findViewById(R.id.ivArrow);
        etComposeBox=this.findViewById(R.id.etComposeBox);
        ivSend=this.findViewById(R.id.ivSend);

        ivArrow.setImageTintList(ColorStateList.valueOf(color));
        ivSend.setImageTintList(ColorStateList.valueOf(color));

        ivArrow.setOnClickListener(this);
        ivSend.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
        ivMic.setOnClickListener(this);


        composeBoxActionFragment = new CometChatComposeBoxActions();
        composeBoxActionFragment.setComposeBoxActionListener(new CometChatComposeBoxActions.ComposeBoxActionListener() {
            @Override
            public void onGalleryClick() {
                composeActionListener.onGalleryActionClicked();
            }

            @Override
            public void onCameraClick() {
                composeActionListener.onCameraActionClicked();
            }

            @Override
            public void onFileClick() {
                composeActionListener.onFileActionClicked();
            }

            @Override
            public void onAudioClick() {
                composeActionListener.onAudioActionClicked();
            }

            @Override
            public void onLocationClick() {
                composeActionListener.onLocationActionClicked();
            }

            @Override
            public void onPollClick() { composeActionListener.onPollActionClicked(); }

            @Override
            public void onStickerClick() { composeActionListener.onStickerClicked(); }

            @Override
            public void onWhiteBoardClick() {
                composeActionListener.onWhiteboardClicked();
            }

            @Override
            public void onWriteBoardClick() {
                composeActionListener.onWriteboardClicked();
            }

        });
        etComposeBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (composeActionListener!=null){
                    composeActionListener.beforeTextChanged(charSequence,i,i1,i2);
                }

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (composeActionListener!=null){
                    composeActionListener.onTextChanged(charSequence,i,i1,i2);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (composeActionListener!=null){
                    composeActionListener.afterTextChanged(editable);
                }
            }
        });
        etComposeBox.setMediaSelected(new CometChatEditText.OnEditTextMediaListener() {
            @Override
            public void OnMediaSelected(InputContentInfoCompat i) {
                composeActionListener.onEditTextMediaSelected(i);
            }
        });
//        InputConnection ic = etComposeBox.onCreateInputConnection(new EditorInfo());
//        EditorInfoCompat.setContentMimeTypes(new EditorInfo(),
//                new String [] {"image/png","image/gif"});


        if (Utils.isDarkMode(context)) {
            composeBox.setBackgroundColor(getResources().getColor(R.color.darkModeBackground));
            ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_microphone_white_selected));
            flBox.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            etComposeBox.setTextColor(getResources().getColor(R.color.textColorWhite));
            ivArrow.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
            ivSend.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
        } else {
            composeBox.setBackgroundColor(getResources().getColor(R.color.textColorWhite));
            ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_microphone_grey_selected));
            etComposeBox.setTextColor(getResources().getColor(R.color.primaryTextColor));
            ivSend.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            flBox.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
            ivArrow.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
        }
        if (UIKitSettings.getColor()!=null) {
            int settingsColor = Color.parseColor(UIKitSettings.getColor());
            ivSend.setImageTintList(ColorStateList.valueOf(settingsColor));
        }
        fetchSettings();

        if (!isGroupCallVisible &&
                !isPollVisible &&
                !isFileVisible &&
                !isGalleryVisible &&
                !isAudioVisible &&
                !isLocationVisible &&
                !isStickerVisible &&
                !isWhiteBoardVisible && !isWriteBoardVisible) {
            ivArrow.setVisibility(GONE);
        }
        a.recycle();
    }

    private void fetchSettings() {
        FeatureRestriction.isPollsEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isPollVisible = booleanVal;
            }
        });
        FeatureRestriction.isFilesEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isFileVisible = booleanVal;
            }
        });
        FeatureRestriction.isPhotosVideoEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isGalleryVisible = booleanVal;
                isCameraVisible = booleanVal;
            }
        });
        FeatureRestriction.isVoiceNotesEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isAudioVisible = booleanVal;
            }
        });
        FeatureRestriction.isLocationSharingEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isLocationVisible = booleanVal;
            }
        });
        FeatureRestriction.isStickersEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isStickerVisible = booleanVal;
            }
        });
        FeatureRestriction.isCollaborativeWhiteBoardEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isWhiteBoardVisible = booleanVal;
            }
        });
        FeatureRestriction.isCollaborativeDocumentEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isWriteBoardVisible = booleanVal;
            }
        });
        FeatureRestriction.isGroupVideoCallEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                isGroupCallVisible = booleanVal;
            }
        });
    }

    public void setText(String text)
    {
        etComposeBox.setText(text);
    }
    public void setColor(int color)
    {

        ivSend.setImageTintList(ColorStateList.valueOf(color));
        ivArrow.setImageTintList(ColorStateList.valueOf(color));
    }
    public void setComposeBoxListener(ComposeActionListener composeActionListener){
        this.composeActionListener=composeActionListener;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ivDelete) {
            stopRecording(true);
            stopPlayingAudio();
            voiceMessageLayout.setVisibility(GONE);
            etComposeBox.setVisibility(View.VISIBLE);
            ivArrow.setVisibility(View.VISIBLE);
            ivMic.setVisibility(View.VISIBLE);
            ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_microphone_grey_selected));
            isPlaying = false;
            isRecording = false;
            voiceMessage = false;
            ivDelete.setVisibility(GONE);
            ivSend.setVisibility(View.GONE);
        }
        if (view.getId()==R.id.ivSend){
            if (!voiceMessage) {
                composeActionListener.onSendActionClicked(etComposeBox);
            } else {
                composeActionListener.onVoiceNoteComplete(audioFileNameWithPath);
                audioFileNameWithPath = "";
                voiceMessageLayout.setVisibility(GONE);
                etComposeBox.setVisibility(View.VISIBLE);
                ivDelete.setVisibility(GONE);
                ivSend.setVisibility(GONE);
                ivArrow.setVisibility(View.VISIBLE);
                ivMic.setVisibility(View.VISIBLE);
                isRecording = false;
                isPlaying = false;
                voiceMessage = false;
                ivMic.setImageResource(R.drawable.ic_microphone_grey_selected);
            }

        }
        if(view.getId()==R.id.ivArrow) {
//            if (isOpen) {
//               closeActionContainer();
//            } else {
//                openActionContainer();
//            }
            FragmentManager fm = ((AppCompatActivity)getContext()).getSupportFragmentManager();
            bundle.putBoolean("isGalleryVisible",isGalleryVisible);
            bundle.putBoolean("isCameraVisible",isCameraVisible);
            bundle.putBoolean("isFileVisible",isFileVisible);
            bundle.putBoolean("isAudioVisible",isAudioVisible);
            bundle.putBoolean("isLocationVisible",isLocationVisible);
            bundle.putBoolean("isGroupCallVisible",isGroupCallVisible);
            if (CometChat.isExtensionEnabled("document")) {
                bundle.putBoolean("isWriteBoardVisible",isWriteBoardVisible);
            }

            if (CometChat.isExtensionEnabled("whiteboard")) {
                bundle.putBoolean("isWhiteBoardVisible",isWhiteBoardVisible);
            }

            if (CometChat.isExtensionEnabled("stickers")) {
                bundle.putBoolean("isStickerVisible",isStickerVisible);
            }

            if (CometChat.isExtensionEnabled("polls")) {
                bundle.putBoolean("isPollsVisible",isPollVisible);
            }

            composeBoxActionFragment.setArguments(bundle);
            composeBoxActionFragment.show(fm,composeBoxActionFragment.getTag());
        }
        if (view.getId()==R.id.ivMic) {
            if (Utils.hasPermissions(context, new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE})) {

                if (isOpen) {
//                    closeActionContainer();
                }
                if (!isRecording) {
                    startRecord();
                    ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_24dp));
                    isRecording = true;
                    isPlaying = false;
                } else {
                    if (isRecording && !isPlaying) {
                        isPlaying = true;
                        stopRecording(false);
                        recordTime.stop();
                    }
                    ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
                    audioRecordView.setVisibility(GONE);
                    ivSend.setVisibility(View.VISIBLE);
                    ivDelete.setVisibility(View.VISIBLE);
                    voiceSeekbar.setVisibility(View.VISIBLE);
                    voiceMessage = true;
                    if (audioFileNameWithPath != null)
                        startPlayingAudio(audioFileNameWithPath);
                    else
                        Toast.makeText(getContext(), "No File Found. Please", Toast.LENGTH_LONG).show();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((Activity)context).requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            UIKitConstants.RequestCode.RECORD);
                }
            }
        }
    }

    public void usedIn(String className) {
        bundle.putString("type",className);
    }

//    public void openActionContainer() {
//        ivArrow.setRotation(45f);
//        isOpen = true;
//        Animation rightAnimate = AnimationUtils.loadAnimation(getContext(), R.anim.animate_right_slide);
//        rlActionContainer.startAnimation(rightAnimate);
//        rlActionContainer.setVisibility(View.VISIBLE);
//    }
//
//    public void closeActionContainer() {
//        ivArrow.setRotation(0);
//        isOpen = false;
//        Animation leftAnim = AnimationUtils.loadAnimation(getContext(), R.anim.animate_left_slide);
//        rlActionContainer.startAnimation(leftAnim);
//        rlActionContainer.setVisibility(GONE);
//    }

    public void startRecord() {
            etComposeBox.setVisibility(GONE);
            recordTime.setBase(SystemClock.elapsedRealtime());
            recordTime.start();
            ivArrow.setVisibility(GONE);
            voiceSeekbar.setVisibility(GONE);
            voiceMessageLayout.setVisibility(View.VISIBLE);
            audioRecordView.recreate();
            audioRecordView.setVisibility(View.VISIBLE);
            startRecording();
    }

    private void startPlayingAudio(String path) {
        try {

            if (timerRunnable != null) {
                seekHandler.removeCallbacks(timerRunnable);
                timerRunnable = null;
            }

            mediaPlayer.reset();
            if (Utils.hasPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } else {
                ((Activity)context).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        UIKitConstants.RequestCode.READ_STORAGE);
            }

            final int duration = mediaPlayer.getDuration();
            voiceSeekbar.setMax(duration);
            recordTime.setBase(SystemClock.elapsedRealtime());
            recordTime.start();
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    int pos = mediaPlayer.getCurrentPosition();
                    voiceSeekbar.setProgress(pos);

                    if (mediaPlayer.isPlaying() && pos < duration) {
//                        audioLength.setText(Utils.convertTimeStampToDurationTime(player.getCurrentPosition()));
                        seekHandler.postDelayed(this, 100);
                    } else {
                        seekHandler
                                .removeCallbacks(timerRunnable);
                        timerRunnable = null;
                    }
                }

            };
            seekHandler.postDelayed(timerRunnable, 100);
            mediaPlayer.setOnCompletionListener(mp -> {
                seekHandler
                        .removeCallbacks(timerRunnable);
                timerRunnable = null;
                mp.stop();
                recordTime.stop();
//                audioLength.setText(Utils.convertTimeStampToDurationTime(duration));
                voiceSeekbar.setProgress(0);
//                playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            });

        } catch (Exception e) {
            Log.e( "playAudioError: ",e.getMessage());
            stopPlayingAudio();;
        }
    }


    private void stopPlayingAudio() {
        if (mediaPlayer != null)
            mediaPlayer.stop();
    }
    private void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            audioFileNameWithPath = Utils.getOutputMediaFile(getContext());
            mediaRecorder.setOutputFile(audioFileNameWithPath);
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int currentMaxAmp = 0;
                    try {
                        currentMaxAmp = mediaRecorder != null ? mediaRecorder.getMaxAmplitude() : 0;
                        audioRecordView.update(currentMaxAmp);
                        if (mediaRecorder==null)
                            timer = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }}, 0, 100);
            mediaRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording(boolean isCancel) {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                if (isCancel) {
                    new File(audioFileNameWithPath).delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAudioButtonVisible(boolean result) { isAudioVisible = result; }

    public void setGalleryButtonVisible(boolean result) { isGalleryVisible = result; }

    public void setCameraButtonVisible(boolean result) { isCameraVisible = result; }

    public void setFileButtonVisible(boolean result) { isFileVisible = result; }

    public void setLocationButtonVisible(boolean result) { isLocationVisible = result; }

    public void hideGroupCallOption(boolean b) {
        isGroupCallVisible = !b;
    }

    public void hidePollOption(boolean b) {
        isPollVisible = !b;
    }

    public void hideStickerOption(boolean b) {
        isStickerVisible = !b;
    }

    public void hideWriteBoardOption(boolean b) {
        isWriteBoardVisible = !b;
    }

    public void hideWhiteBoardOption(boolean b) {
        isWhiteBoardVisible = !b;
    }

    public void hideRecordOption(boolean b) {
        if (b) {
            ivMic.setVisibility(GONE);
        } else {
            ivMic.setVisibility(VISIBLE);
        }
    }

    public void hideSendButton(boolean b) {
        if (b) {
            ivSend.setVisibility(GONE);
        } else
            ivSend.setVisibility(VISIBLE);
    }
}
