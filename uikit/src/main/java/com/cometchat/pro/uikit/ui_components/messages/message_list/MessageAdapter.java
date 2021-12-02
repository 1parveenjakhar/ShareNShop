package com.cometchat.pro.uikit.ui_components.messages.message_list;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.EmojiSpan;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.helpers.CometChatHelper;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.Attachment;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.uikit.R;

import com.cometchat.pro.uikit.ui_components.messages.extensions.Extensions;
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.User;

import com.cometchat.pro.uikit.ui_resources.utils.pattern_utils.PatternUtils;
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction;
import com.cometchat.pro.uikit.ui_settings.UIKitSettings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;
import com.cometchat.pro.uikit.ui_resources.utils.sticker_header.StickyHeaderAdapter;
import com.cometchat.pro.uikit.ui_components.messages.media_view.CometChatMediaViewActivity;
import com.cometchat.pro.uikit.ui_components.messages.extensions.Reactions.CometChatReactionInfoActivity;
import com.cometchat.pro.uikit.ui_components.messages.thread_message_list.CometChatThreadMessageListActivity;
import com.cometchat.pro.uikit.ui_resources.utils.CallUtils;
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils;
import com.cometchat.pro.uikit.ui_resources.utils.MediaUtils;
import com.cometchat.pro.uikit.ui_resources.utils.Utils;

/**
 * Purpose - MessageAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of messages. It helps to organize the messages based on its type i.e TextMessage,
 * MediaMessage, Actions. It also helps to manage whether message is sent or recieved and organizes
 * view based on it. It is single adapter used to manage group messages and user messages.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */


public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyHeaderAdapter<MessageAdapter.DateItemHolder> {

    private static final int RIGHT_IMAGE_MESSAGE = 56;

    private static final int LEFT_IMAGE_MESSAGE = 89;

    private static final int RIGHT_STICKER_MESSAGE = 21;

    private static final int LEFT_STICKER_MESSAGE = 22;

    private static final int RIGHT_VIDEO_MESSAGE = 78;

    private static final int LEFT_VIDEO_MESSAGE = 87;

    private static final int RIGHT_AUDIO_MESSAGE = 39;

    private static final int LEFT_AUDIO_MESSAGE = 93;

    private static final int CALL_MESSAGE = 234;

    private List<BaseMessage> messageList = new ArrayList<>();

    private static final int LEFT_TEXT_MESSAGE = 1;

    private static final int RIGHT_TEXT_MESSAGE = 2;

    private static final int LEFT_WHITEBOARD_MESSAGE = 7;

    private static final int RIGHT_WHITEBOARD_MESSAGE = 8;

    private static final int LEFT_WRITEBOARD_MESSAGE = 9;

    private static final int RIGHT_WRITEBOARD_MESSAGE = 10;

    private static final int RIGHT_REPLY_TEXT_MESSAGE = 987;

    private static final int LEFT_REPLY_TEXT_MESSAGE = 789;

    private static final int RIGHT_FILE_MESSAGE = 23;

    private static final int LEFT_FILE_MESSAGE = 25;

    private static final int ACTION_MESSAGE = 99;

    private static final int RIGHT_LINK_MESSAGE = 12;

    private static final int LEFT_LINK_MESSAGE = 13;

    private static final int LEFT_DELETE_MESSAGE = 551;

    private static final int RIGHT_DELETE_MESSAGE = 552;

    private static final int RIGHT_CUSTOM_MESSAGE = 432;

    private static final int LEFT_CUSTOM_MESSAGE = 431;

    private static final int LEFT_LOCATION_CUSTOM_MESSAGE = 31;

    private static final int RIGHT_LOCATION_CUSTOM_MESSAGE = 32;

    private static final int RIGHT_POLLS_CUSTOM_MESSAGE = 41;

    private static final int LEFT_POLLS_CUSTOM_MESSAGE = 42;

    private static final int LEFT_GROUP_CALL_MESSAGE = 313;

    private static final int RIGHT_GROUP_CALL_MESSAGE = 314;

    public static double LATITUDE;
    public static double LONGITUDE;

    public Context context;

    private User loggedInUser = CometChat.getLoggedInUser();

    private boolean isLongClickEnabled;

    private List<Integer> selectedItemList = new ArrayList<>();

    public List<BaseMessage> longselectedItemList = new ArrayList<>();

    private FontUtils fontUtils;

    private MediaPlayer mediaPlayer;

    private int messagePosition=0;

    private OnMessageLongClick messageLongClick;

    private boolean isUserDetailVisible;

    private String TAG = "MessageAdapter";

    private boolean isSent;

    private boolean isTextMessageClick;

    private boolean isImageMessageClick;

    private boolean isLocationMessageClick;

    private String selectedOption;

    /**
     * It is used to initialize the adapter wherever we needed. It has parameter like messageList
     * which contains list of messages and it will be used in adapter and paramter type is a String
     * whose values will be either CometChatConstants.RECEIVER_TYPE_USER
     * CometChatConstants.RECEIVER_TYPE_GROUP.
     *
     *
     * @param context is a object of Context.
     * @param messageList is a list of messages used in this adapter.
     * @param type is a String which identifies whether it is a user messages or a group messages.
     *
     */
    public MessageAdapter(Context context, List<BaseMessage> messageList, String type) {
        setMessageList(messageList);
        this.context = context;
        try {
            messageLongClick = (CometChatMessageListActivity) context;
        }catch (Exception e) {
            e.printStackTrace();
        }

        if (null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
        }

        fontUtils=FontUtils.getInstance(context);
    }

    /**
     * This method is used to return the different view types to adapter based on item position.
     * It uses getItemViewTypes() method to identify the view type of item.
     * @see MessageAdapter#getItemViewTypes(int)
     *      *
     * @param position is a position of item in recyclerView.
     * @return It returns int which is value of view type of item.
     * @see MessageAdapter#onCreateViewHolder(ViewGroup, int)
     */
    @Override
    public int getItemViewType(int position) {
        return getItemViewTypes(position);
    }

    private void setMessageList(List<BaseMessage> messageList) {
        this.messageList.addAll(0, messageList);
        notifyItemRangeInserted(0,messageList.size());

    }

    /**
     * This method is used to inflate the view for item based on its viewtype.
     * It helps to differentiate view for different type of messages.
     * Based on view type it returns various ViewHolder
     * Ex :- For MediaMessage it will return ImageMessageViewHolder,
     *       For TextMessage it will return TextMessageViewHolder,etc.
     *
     * @param parent is a object of ViewGroup.
     * @param i is viewType based on it various view will be inflated by adapter for various type
     *          of messages.
     * @return It return different ViewHolder for different viewType.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view;
        switch (i) {
            case LEFT_DELETE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_text_item,parent,false);
                view.setTag(LEFT_DELETE_MESSAGE);
                return new DeleteMessageViewHolder(view);
            case RIGHT_DELETE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_text_item,parent,false);
                view.setTag(RIGHT_DELETE_MESSAGE);
                return new DeleteMessageViewHolder(view);
            case LEFT_TEXT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_text_item, parent, false);
                view.setTag(LEFT_TEXT_MESSAGE);
                return new TextMessageViewHolder(view);

            case RIGHT_TEXT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_text_item, parent, false);
                view.setTag(RIGHT_TEXT_MESSAGE);
                return new TextMessageViewHolder(view);

            case LEFT_REPLY_TEXT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_text_item, parent, false);
                view.setTag(LEFT_REPLY_TEXT_MESSAGE);
                return new TextMessageViewHolder(view);

            case RIGHT_REPLY_TEXT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_text_item, parent, false);
                view.setTag(RIGHT_REPLY_TEXT_MESSAGE);
                return new TextMessageViewHolder(view);

            case RIGHT_LINK_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_link_item,parent,false);
                view.setTag(RIGHT_LINK_MESSAGE);
                return new LinkMessageViewHolder(view);

            case LEFT_LINK_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_link_item,parent,false);
                view.setTag(LEFT_LINK_MESSAGE);
                return new LinkMessageViewHolder(view);

            case RIGHT_AUDIO_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_audio_item,parent,false);
                view.setTag(RIGHT_AUDIO_MESSAGE);
                return new AudioMessageViewHolder(view);

            case LEFT_AUDIO_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_audio_item,parent,false);
                view.setTag(LEFT_AUDIO_MESSAGE);
                return new AudioMessageViewHolder(view);

            case LEFT_STICKER_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_sticker_item, parent, false);
                view.setTag(LEFT_STICKER_MESSAGE);
                return new StickerMessageViewHolder(view);

            case RIGHT_STICKER_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_sticker_item, parent, false);
                view.setTag(RIGHT_STICKER_MESSAGE);
                return new StickerMessageViewHolder(view);

            case LEFT_IMAGE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_list_image_item, parent, false);
                view.setTag(LEFT_IMAGE_MESSAGE);
                return new ImageMessageViewHolder(view);


            case RIGHT_IMAGE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_list_image_item, parent, false);
                view.setTag(RIGHT_IMAGE_MESSAGE);
                return new ImageMessageViewHolder(view);


            case LEFT_VIDEO_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_list_video_item,parent,false);
                view.setTag(LEFT_VIDEO_MESSAGE);
                return new VideoMessageViewHolder(view);

            case RIGHT_VIDEO_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_list_video_item,parent,false);
                view.setTag(RIGHT_VIDEO_MESSAGE);
                return new VideoMessageViewHolder(view);
            case RIGHT_FILE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_file_item, parent, false);
                view.setTag(RIGHT_FILE_MESSAGE);
                return new FileMessageViewHolder(view);

            case LEFT_FILE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_file_item, parent, false);
                view.setTag(LEFT_FILE_MESSAGE);
                return new FileMessageViewHolder(view);
            case ACTION_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_action_item, parent, false);
                view.setTag(ACTION_MESSAGE);
                return new ActionMessageViewHolder(view);

            case CALL_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_action_item, parent, false);
                view.setTag(CALL_MESSAGE);
                return new ActionMessageViewHolder(view);

            case RIGHT_CUSTOM_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_text_item, parent, false);
                view.setTag(RIGHT_TEXT_MESSAGE);
                return new CustomMessageViewHolder(view);

            case LEFT_CUSTOM_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_text_item, parent, false);
                view.setTag(RIGHT_TEXT_MESSAGE);
                return new CustomMessageViewHolder(view);

            case RIGHT_LOCATION_CUSTOM_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_location_item, parent, false);
                view.setTag(RIGHT_LOCATION_CUSTOM_MESSAGE);
                return new LocationMessageViewHolder(view);

            case LEFT_LOCATION_CUSTOM_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_location_item, parent, false);
                view.setTag(LEFT_LOCATION_CUSTOM_MESSAGE);
                return new LocationMessageViewHolder(view);

            case RIGHT_POLLS_CUSTOM_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_polls_item,parent,false);
                view.setTag(RIGHT_POLLS_CUSTOM_MESSAGE);
                return new PollMessageViewHolder(view);

            case LEFT_POLLS_CUSTOM_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_polls_item,parent,false);
                view.setTag(RIGHT_POLLS_CUSTOM_MESSAGE);
                return new PollMessageViewHolder(view);
            case LEFT_WHITEBOARD_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_whiteboard_item, parent, false);
                view.setTag(LEFT_WHITEBOARD_MESSAGE);
                return new WhiteBoardViewHolder(view);

            case RIGHT_WHITEBOARD_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_whiteboard_item, parent, false);
                view.setTag(RIGHT_WHITEBOARD_MESSAGE);
                return new WhiteBoardViewHolder(view);

            case LEFT_WRITEBOARD_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_writeboard_item, parent, false);
                view.setTag(LEFT_WRITEBOARD_MESSAGE);
                return new WriteBoardViewHolder(view);

            case RIGHT_WRITEBOARD_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_writeboard_item, parent, false);
                view.setTag(RIGHT_WRITEBOARD_MESSAGE);
                return new WriteBoardViewHolder(view);

            case RIGHT_GROUP_CALL_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_group_call_item, parent, false);
                view.setTag(RIGHT_GROUP_CALL_MESSAGE);
                return new GroupCallMessageViewHolder(view);

            case LEFT_GROUP_CALL_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_group_call_item, parent, false);
                view.setTag(LEFT_GROUP_CALL_MESSAGE);
                return new GroupCallMessageViewHolder(view);


            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_text_item, parent, false);
                view.setTag(-1);
                return new TextMessageViewHolder(view);
        }
    }


    /**
     * This method is used to bind the various ViewHolder content with their respective view types.
     * Here different methods are being called for different view type and in each method different
     * ViewHolder are been passed as parameter along with position of item.
     *
     * Ex :- For TextMessage <code>setTextData((TextMessageViewHolder)viewHolder,i)</code> is been called.
     * where <b>viewHolder</b> is casted as <b>TextMessageViewHolder</b> and <b>i</b> is position of item.
     *
     * @see MessageAdapter#setTextData(TextMessageViewHolder, int)
     * @see MessageAdapter#setImageData(ImageMessageViewHolder, int)
     * @see MessageAdapter#setFileData(FileMessageViewHolder, int)
     * @see MessageAdapter#setActionData(ActionMessageViewHolder, int)
     *
     * @param viewHolder is a object of RecyclerViewHolder.
     * @param i is position of item in recyclerView.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        BaseMessage baseMessage = messageList.get(i);
        BaseMessage nextMessage = null, prevMessage = null;
        boolean isNextMessage = false,isPreviousMessage = false,isPrevActionMessage = false;
        if ((i + 1) < messageList.size()) {
            if (messageList.get(i+1).getSender()!=null)
                nextMessage = messageList.get(i + 1);
        }

        if ((i - 1) >= 0) {
            if (messageList.get(i-1).getSender()!=null)
                prevMessage = messageList.get(i -1);
        }

        isPrevActionMessage = (prevMessage!=null && (prevMessage.getCategory().equals(CometChatConstants.CATEGORY_ACTION) || prevMessage.getCategory().equals(CometChatConstants.CATEGORY_CALL)));
        isNextMessage = (nextMessage!=null && baseMessage.getSender().getUid().equals(nextMessage.getSender().getUid()));
        isPreviousMessage = (prevMessage!=null && baseMessage.getSender().getUid().equals(prevMessage.getSender().getUid()));

        if (!isPreviousMessage && isNextMessage) {
           isUserDetailVisible = true;
        }

        if (isPreviousMessage && isNextMessage) {
            isUserDetailVisible = false;
        }
        else if (!isNextMessage && !isPreviousMessage) {
            isUserDetailVisible = true;
        }

        else if(!isNextMessage) {
            isUserDetailVisible = false;
        }
        if (isPrevActionMessage) {
            isUserDetailVisible = true;
        }


        switch (viewHolder.getItemViewType()) {

            case LEFT_DELETE_MESSAGE:
                ((DeleteMessageViewHolder) viewHolder).ivUser.setVisibility(View.GONE);
            case RIGHT_DELETE_MESSAGE:
                setDeleteData((DeleteMessageViewHolder)viewHolder,i);
                break;
            case LEFT_TEXT_MESSAGE:
            case LEFT_REPLY_TEXT_MESSAGE:
                ((TextMessageViewHolder) viewHolder).ivUser.setVisibility(View.GONE);
            case RIGHT_TEXT_MESSAGE:
            case RIGHT_REPLY_TEXT_MESSAGE:
                setTextData((TextMessageViewHolder) viewHolder, i);
                break;
            case LEFT_LINK_MESSAGE:
            case RIGHT_LINK_MESSAGE:
                setLinkData((LinkMessageViewHolder) viewHolder,i);
                break;
            case LEFT_IMAGE_MESSAGE:
            case RIGHT_IMAGE_MESSAGE:
                setImageData((ImageMessageViewHolder) viewHolder, i);
                break;

            case LEFT_STICKER_MESSAGE:
            case RIGHT_STICKER_MESSAGE:
                setStickerData((StickerMessageViewHolder) viewHolder,i);
                break;

            case LEFT_AUDIO_MESSAGE:
            case RIGHT_AUDIO_MESSAGE:
                setAudioData((AudioMessageViewHolder) viewHolder,i);
                break;
            case LEFT_VIDEO_MESSAGE:
            case RIGHT_VIDEO_MESSAGE:
                setVideoData((VideoMessageViewHolder) viewHolder,i);
                break;
            case LEFT_FILE_MESSAGE:
                ((FileMessageViewHolder) viewHolder).ivUser.setVisibility(View.GONE);
            case RIGHT_FILE_MESSAGE:
                setFileData((FileMessageViewHolder) viewHolder, i);
                break;
            case ACTION_MESSAGE:
            case CALL_MESSAGE:
                setActionData((ActionMessageViewHolder) viewHolder, i);
                break;
            case LEFT_CUSTOM_MESSAGE:
                ((CustomMessageViewHolder) viewHolder).ivUser.setVisibility(View.GONE);
            case RIGHT_CUSTOM_MESSAGE:
                setCustomData((CustomMessageViewHolder) viewHolder, i);
                break;
            case LEFT_LOCATION_CUSTOM_MESSAGE:
            case RIGHT_LOCATION_CUSTOM_MESSAGE:
                setLocationData((LocationMessageViewHolder) viewHolder, i);
                break;

            case RIGHT_POLLS_CUSTOM_MESSAGE:
            case LEFT_POLLS_CUSTOM_MESSAGE:
                setPollsData((PollMessageViewHolder) viewHolder,i);
                break;

            case LEFT_WHITEBOARD_MESSAGE:
            case RIGHT_WHITEBOARD_MESSAGE:
                setWhiteBoardData((WhiteBoardViewHolder) viewHolder,i);
                break;

            case LEFT_WRITEBOARD_MESSAGE:
            case RIGHT_WRITEBOARD_MESSAGE:
                setWriteBoardData((WriteBoardViewHolder) viewHolder,i);
                break;

            case LEFT_GROUP_CALL_MESSAGE:
            case RIGHT_GROUP_CALL_MESSAGE:
                setGroupCallMessageData((GroupCallMessageViewHolder)viewHolder,i);
                break;
        }
    }

    private void setPollsData(PollMessageViewHolder viewHolder, int i) {
        BaseMessage baseMessage = messageList.get(i);
        if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                viewHolder.tvUser.setVisibility(View.GONE);
                viewHolder.ivUser.setVisibility(View.GONE);
            } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                if (isUserDetailVisible) {
                    viewHolder.tvUser.setVisibility(View.VISIBLE);
                    viewHolder.ivUser.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.INVISIBLE);
                }
                setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                viewHolder.tvUser.setText(baseMessage.getSender().getName());
            }
        }
        FeatureRestriction.isThreadedMessagesEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (baseMessage.getReplyCount()!=0 && booleanVal) {
                    viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                    viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
                } else {
                    viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
                }
            }
        });
        viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
            Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
            intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
            intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
            intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
            intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());
            intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO, Extensions.getReactionsOnMessage(baseMessage));
            try {
                JSONObject option = ((CustomMessage) baseMessage).getCustomData().getJSONObject("options");
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, UIKitConstants.IntentStrings.POLLS);
                intent.putExtra(UIKitConstants.IntentStrings.POLL_QUESTION,
                        ((CustomMessage) baseMessage).getCustomData().getString("question"));
                intent.putExtra(UIKitConstants.IntentStrings.POLL_OPTION,option.toString());
                intent.putExtra(UIKitConstants.IntentStrings.POLL_VOTE_COUNT,Extensions.getVoteCount(baseMessage));
                intent.putExtra(UIKitConstants.IntentStrings.POLL_RESULT, Extensions.getVoterInfo(baseMessage,option.length()));
            }catch (Exception e) {
                Log.e(TAG, "startThreadActivityError: "+e.getMessage());
            }
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
            intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
            }
            else {
                if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                    intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                else
                    intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
            }
            context.startActivity(intent);
        });
        viewHolder.optionGroup.removeAllViews();
        viewHolder.totalCount.setText("0"+context.getString(R.string.votes));
        ArrayList<String> optionList = new ArrayList<>();
        try {
            JSONObject jsonObject = ((CustomMessage) baseMessage).getCustomData();
            JSONObject options = jsonObject.getJSONObject("options");
            ArrayList<String> voterInfo = Extensions.getVoterInfo(baseMessage,options.length());
            viewHolder.tvQuestion.setText(jsonObject.getString("question"));
                for (int k = 0; k < options.length(); k++) {
                    int voteCount = Extensions.getVoteCount(baseMessage);
                    if (voteCount==1) {
                        viewHolder.totalCount.setText(voteCount +
                                context.getString(R.string.vote));
                    } else {
                        viewHolder.totalCount.setText(voteCount +
                                context.getString(R.string.votes));
                    }
                    LinearLayout linearLayout = new LinearLayout(context);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout
                                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        linearLayout.setPadding(8,8,8,8);
                        linearLayout.setBackgroundColor(Color.parseColor(UIKitSettings.getColor()));
                        linearLayout.setBackgroundTintList(ColorStateList.valueOf(context.getResources()
                                .getColor(R.color.textColorWhite)));
                        layoutParams.bottomMargin = (int) Utils.dpToPx(context, 8);
                        linearLayout.setLayoutParams(layoutParams);

                        TextView textViewPercentage = new TextView(context);
                        TextView textViewOption = new TextView(context);
                        textViewPercentage.setPadding(16, 4, 0, 4);
                        textViewOption.setPadding(16, 4, 0, 4);
                        textViewOption.setTextAppearance(context, R.style.TextAppearance_AppCompat_Medium);
                        textViewPercentage.setTextAppearance(context, R.style.TextAppearance_AppCompat_Medium);

                            textViewPercentage.setTextColor(context.getResources().getColor(R.color.primaryTextColor));
                            textViewOption.setTextColor(context.getResources().getColor(R.color.primaryTextColor));
                        String optionStr = options.getString(String.valueOf(k + 1));
                        textViewOption.setText(optionStr);
                        if (voteCount>0) {
                            int percentage = Math.round((Integer.parseInt(voterInfo.get(k)) * 100) /
                                    voteCount);
                            if (percentage > 0)
                                textViewPercentage.setText(percentage + "% ");
                        }
                        if (k+1==Extensions.userVotedOn(baseMessage,optionList.size()+1,loggedInUser.getUid())) {
                            textViewPercentage.setCompoundDrawablePadding(8);
                            textViewPercentage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_circle_24,0,0,0);
                        }
                        int finalK = k;
                        if (viewHolder.optionGroup.getChildCount()!=options.length()) {
                            viewHolder.loadingProgress.setVisibility(View.GONE);
                            linearLayout.addView(textViewPercentage);
                            linearLayout.addView(textViewOption);
                            viewHolder.optionGroup.addView(linearLayout);
                        }
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    String pollsId=null;
                                    if (jsonObject.has("id"))
                                        pollsId = jsonObject.getString("id");
                                    else
                                        pollsId = baseMessage.getId()+"";
                                    JSONObject pollsJsonObject = new JSONObject();
                                    pollsJsonObject.put("vote",finalK+1);
                                    pollsJsonObject.put("id", pollsId);
                                    CometChat.callExtension("polls", "POST", "/v2/vote",
                                            pollsJsonObject, new CometChat.CallbackListener<JSONObject>() {
                                                    @Override
                                                    public void onSuccess(JSONObject jsonObject) {
                                                        // Voted successfully
                                                        viewHolder.loadingProgress.setVisibility(View.VISIBLE);
                                                        viewHolder.totalCount.setText("0"+context.getString(R.string.votes));
                                                        Log.e(TAG, "onSuccess: " + jsonObject.toString());
                                                        Toast.makeText(context, context.getString(R.string.voted_success), Toast.LENGTH_LONG).show();
                                                    }

                                                    @Override
                                                    public void onError(CometChatException e) {
                                                        // Some error occured
                                                        Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
                                                        Log.e(TAG, "onErrorExtension: " + e.getMessage() + "\n" + e.getCode());
                                                    }
                                                });
                                } catch (Exception e) {
                                    Log.e(TAG, "onError: "+e.getMessage());
                                }
                            }
                        });
                    optionList.add(options.getString(String.valueOf(k + 1)));
                }
        } catch (Exception e) {
            Log.e(TAG, "setPollsData: "+e.getMessage()+"\n"+viewHolder.totalCount.getText());
        }
        PatternUtils.setHyperLinkSupport(context,viewHolder.tvQuestion);
        showMessageTime(viewHolder,baseMessage);
//        if (messageList.get(messageList.size()-1).equals(baseMessage))
//        {
//            selectedItemList.add(baseMessage.getId());
//        }
//        if (selectedItemList.contains(baseMessage.getId()))
        viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);

        setColorFilter(baseMessage,viewHolder.cvMessageView);
        viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!isImageMessageClick && !isTextMessageClick) {
                    isLongClickEnabled = true;
                    isLocationMessageClick = true;
                    setLongClickSelectedItem(baseMessage);
                    messageLongClick.setLongMessageClick(longselectedItemList);
                }
                return true;
            }
        });
        viewHolder.reactionLayout.setVisibility(View.GONE);
        setReactionSupport(baseMessage,viewHolder.reactionLayout);
    }

    private void setGroupCallMessageData(GroupCallMessageViewHolder viewHolder, int i) {
        BaseMessage baseMessage = messageList.get(i);
        if (baseMessage!=null) {
            if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.GONE);
                }
                else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    if (isUserDetailVisible) {
                        viewHolder.tvUser.setVisibility(View.VISIBLE);
                        viewHolder.ivUser.setVisibility(View.VISIBLE);
                    } else
                    {
                        viewHolder.tvUser.setVisibility(View.GONE);
                        viewHolder.ivUser.setVisibility(View.INVISIBLE);
                    }
                    setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                    viewHolder.tvUser.setText(baseMessage.getSender().getName());
                }
                viewHolder.groupCallMessage.setText(baseMessage.getSender().getName() + " " +
                        context.getString(R.string.has_shared_group_call));

            } else {
                viewHolder.groupCallMessage.setText(context.getString(R.string.you_created_group_call));
            }

            viewHolder.joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CustomMessage)baseMessage).getCustomData()!=null) {
                        if (CometChat.getActiveCall()==null)
                            CallUtils.startDirectCall(context, Utils.getDirectCallData(baseMessage));
                        else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(context);
                            alert.setTitle(context.getResources().getString(R.string.ongoing_call))
                                    .setMessage(context.getResources().getString(R.string.ongoing_call_message))
                                    .setPositiveButton(context.getResources().getString(R.string.join), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            CallUtils.joinOnGoingCall(context,CometChat.getActiveCall());
                                        }
                                    }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                        }
                    }
                }
            });

            if (baseMessage.getReplyCount()!=0) {
                viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
            } else {
                viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
            }
            viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
                Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
                intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
                intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
                intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
                intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
                intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());

                intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE,Extensions.getWriteBoardUrl(baseMessage));

                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
                intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
                }
                else {
                    if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                    else
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
                }
                context.startActivity(intent);
            });

            showMessageTime(viewHolder, baseMessage);
            viewHolder.txtTime.setVisibility(View.VISIBLE);
            setColorFilter(baseMessage,viewHolder.messageContainer);
            viewHolder.rlMessageBubble.setOnClickListener(view -> {

                if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())){
                    if (isLongClickEnabled && !isImageMessageClick) {
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                    } else {
                        setSelectedMessage(baseMessage.getId());
                    }
                    notifyItemChanged(i);
                }

            });

            viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (!isImageMessageClick && !isTextMessageClick) {
                        isLongClickEnabled = true;
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                        notifyItemChanged(i);
                    }
                    return true;
                }
            });
            setReactionSupport(baseMessage,viewHolder.reactionLayout);
        }
    }


    private void setWriteBoardData(WriteBoardViewHolder viewHolder,int i) {
        BaseMessage baseMessage = messageList.get(i);
        if (baseMessage!=null) {
            if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.GONE);
                }
                else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    if (isUserDetailVisible)
                    {
                        viewHolder.tvUser.setVisibility(View.VISIBLE);
                        viewHolder.ivUser.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        viewHolder.tvUser.setVisibility(View.GONE);
                        viewHolder.ivUser.setVisibility(View.INVISIBLE);
                    }
                    setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                    viewHolder.tvUser.setText(baseMessage.getSender().getName());
                }
                viewHolder.whiteBoardMessage.setText(baseMessage.getSender().getName()+" "+context.getString(R.string.has_shared_document));
            } else {
                viewHolder.whiteBoardMessage.setText(context.getString(R.string.you_created_document));
            }
            viewHolder.joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Extensions.openWriteBoard(baseMessage, context);
                }
            });


            if (baseMessage.getReplyCount()!=0) {
                viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
            } else {
                viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
            }
            viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
                Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
                intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
                intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
                intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
                intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
                intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());

                intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE,Extensions.getWhiteBoardUrl(baseMessage));

                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
                intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
                }
                else {
                    if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                    else
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
                }
                context.startActivity(intent);
            });

            showMessageTime(viewHolder, baseMessage);
            viewHolder.txtTime.setVisibility(View.VISIBLE);
            setColorFilter(baseMessage,viewHolder.messageContainer);
            viewHolder.rlMessageBubble.setOnClickListener(view -> {

                if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())){
                    if (isLongClickEnabled && !isImageMessageClick) {
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                    } else {
                        setSelectedMessage(baseMessage.getId());
                    }
                    notifyItemChanged(i);
                }

            });

            viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (!isImageMessageClick && !isTextMessageClick) {
                        isLongClickEnabled = true;
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                        notifyItemChanged(i);
                    }
                    return true;
                }
            });
            setReactionSupport(baseMessage,viewHolder.reactionLayout);
        }
    }


    private void setWhiteBoardData(WhiteBoardViewHolder viewHolder,int i) {
        BaseMessage baseMessage = messageList.get(i);
        if (baseMessage!=null) {
            if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.GONE);
                }
                else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    if (isUserDetailVisible)
                    {
                        viewHolder.tvUser.setVisibility(View.VISIBLE);
                        viewHolder.ivUser.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        viewHolder.tvUser.setVisibility(View.GONE);
                        viewHolder.ivUser.setVisibility(View.INVISIBLE);
                    }
                    setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                    viewHolder.tvUser.setText(baseMessage.getSender().getName());
                }
                viewHolder.whiteBoardMessage.setText(baseMessage.getSender().getName()+" "+
                        context.getString(R.string.has_shared_whiteboard));

            } else {
                viewHolder.whiteBoardMessage.setText(context.getString(R.string.you_created_whiteboard));
            }
            JSONObject jsonObject = baseMessage.getMetadata();
            viewHolder.joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Extensions.openWhiteBoard(baseMessage, context);
                }
            });


            if (baseMessage.getReplyCount()!=0) {
                viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
            } else {
                viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
            }
            viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
                Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
                intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
                intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
                intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
                intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
                intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());

                intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE,Extensions.getWriteBoardUrl(baseMessage));

                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
                intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
                }
                else {
                    if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                    else
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
                }
                context.startActivity(intent);
            });

            showMessageTime(viewHolder, baseMessage);
            viewHolder.txtTime.setVisibility(View.VISIBLE);
            setColorFilter(baseMessage,viewHolder.messageContainer);
            viewHolder.rlMessageBubble.setOnClickListener(view -> {

                if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())){
                    if (isLongClickEnabled && !isImageMessageClick) {
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                    } else {
                        setSelectedMessage(baseMessage.getId());
                    }
                    notifyItemChanged(i);
                }

            });

            viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (!isImageMessageClick && !isTextMessageClick) {
                        isLongClickEnabled = true;
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                        notifyItemChanged(i);
                    }
                    return true;
                }
            });
            setReactionSupport(baseMessage,viewHolder.reactionLayout);
        }
    }

    /**
     * This method is called whenever viewType of item is customMessage and customType is Location.
     * It is used to bind LocationMessageViewHolder
     * contents with CustomMessage at a given position.
     *
     * @param viewHolder is a object of LocationMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see CustomMessage
     * @see BaseMessage
     */
    private void setLocationData(LocationMessageViewHolder viewHolder, int i) {
        BaseMessage baseMessage = messageList.get(i);
        if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                viewHolder.tvUser.setVisibility(View.GONE);
                viewHolder.ivUser.setVisibility(View.GONE);
            } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                if (isUserDetailVisible) {
                    viewHolder.tvUser.setVisibility(View.VISIBLE);
                    viewHolder.ivUser.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.INVISIBLE);
                }
                setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                viewHolder.tvUser.setText(baseMessage.getSender().getName());
            }
        }
        FeatureRestriction.isThreadedMessagesEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (baseMessage.getReplyCount()!=0 && booleanVal) {
                    viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                    viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
                } else {
                    viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
                }
            }
        });

        viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
            Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
            intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
            intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
            intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
            intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
            intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());
            try {
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, UIKitConstants.IntentStrings.LOCATION);
                intent.putExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE,
                        ((CustomMessage) baseMessage).getCustomData().getDouble("latitude"));
                intent.putExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE,
                        ((CustomMessage) baseMessage).getCustomData().getDouble("longitude"));
            }catch (Exception e) {
                Log.e(TAG, "startThreadActivityError: "+e.getMessage());
            }
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
            intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
            }
            else {
                if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                    intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                else
                    intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
            }
            context.startActivity(intent);
        });
        try {
            LATITUDE = ((CustomMessage) baseMessage).getCustomData().getDouble("latitude");
            LONGITUDE = ((CustomMessage) baseMessage).getCustomData().getDouble("longitude");
            viewHolder.tvAddress.setText(Utils.getAddress(context, LATITUDE, LONGITUDE));
            String mapUrl = UIKitConstants.MapUrl.MAPS_URL +LATITUDE+","+LONGITUDE+"&key="+ UIKitConstants.MapUrl.MAP_ACCESS_KEY;
            Glide.with(context)
                    .load(mapUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.default_map)
                    .into(viewHolder.ivMap);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        viewHolder.senderTxt.setText(String.format(context.getString(R.string.shared_location),baseMessage.getSender().getName()));
        viewHolder.ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double latitude = ((CustomMessage)baseMessage).getCustomData().getDouble("latitude");
                    double longitude = ((CustomMessage)baseMessage).getCustomData().getDouble("longitude");;
                    String label = Utils.getAddress(context,latitude,longitude);
                    String uriBegin = "geo:" + latitude + "," + longitude;
                    String query = label;
                    String encodedQuery = Uri.encode(query);
                    String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                    Uri uri = Uri.parse(uriString);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
//                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        showMessageTime(viewHolder,baseMessage);
        setColorFilter(baseMessage,viewHolder.cvMessageView);
//        if (messageList.get(messageList.size()-1).equals(baseMessage))
//        {
//            selectedItemList.add(baseMessage.getId());
//        }
//        if (selectedItemList.contains(baseMessage.getId()))
            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);

        viewHolder.rlMessageBubble.setOnClickListener(view -> {
            if (isLongClickEnabled && !isImageMessageClick) {
                setLongClickSelectedItem(baseMessage);
                messageLongClick.setLongMessageClick(longselectedItemList);
            }
            else {
                setSelectedMessage(baseMessage.getId());
            }
            notifyItemChanged(i);
        });
        viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!isImageMessageClick && !isTextMessageClick) {
                    isLongClickEnabled = true;
                    isLocationMessageClick = true;
                    setLongClickSelectedItem(baseMessage);
                    messageLongClick.setLongMessageClick(longselectedItemList);
                    notifyItemChanged(i);
                }
                return true;
            }
        });

        viewHolder.reactionLayout.setVisibility(View.GONE);
        setReactionSupport(baseMessage,viewHolder.reactionLayout);
    }

    /**
     * This method is called whenever viewType of item is file. It is used to bind AudioMessageViewHolder
     * contents with MediaMessage at a given position.
     *
     * @param viewHolder is a object of AudioMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see MediaMessage
     * @see BaseMessage
     */
    private void setAudioData(AudioMessageViewHolder viewHolder, int i) {
        BaseMessage baseMessage = messageList.get(i);
        if (baseMessage!=null&&baseMessage.getDeletedAt()==0) {
            if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                viewHolder.playBtn.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.textColorWhite)));
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.GONE);
                } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    if (isUserDetailVisible) {
                        viewHolder.tvUser.setVisibility(View.VISIBLE);
                        viewHolder.ivUser.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.tvUser.setVisibility(View.GONE);
                        viewHolder.ivUser.setVisibility(View.INVISIBLE);
                    }
                    setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                    viewHolder.tvUser.setText(baseMessage.getSender().getName());
                }
            } else {
                viewHolder.playBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor(UIKitSettings.getColor())));
            }

            FeatureRestriction.isThreadedMessagesEnabled(new FeatureRestriction.OnSuccessListener() {
                @Override
                public void onSuccess(Boolean booleanVal) {
                    if (baseMessage.getReplyCount()!=0 && booleanVal) {
                        viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                        viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
                    } else {
                        viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
                    }
                }
            });
            viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
                Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
//                intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
                intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
                intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
                intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
                intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME,((MediaMessage)baseMessage).getAttachment().getFileName());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION,((MediaMessage)baseMessage).getAttachment().getFileExtension());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL,((MediaMessage)baseMessage).getAttachment().getFileUrl());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE,((MediaMessage)baseMessage).getAttachment().getFileSize());
                intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
                }
                else {
                    if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                    else
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
                }
                context.startActivity(intent);
            });


            showMessageTime(viewHolder,baseMessage);
            setColorFilter(baseMessage,viewHolder.rlMessageBubble);
//            if (selectedItemList.contains(baseMessage.getId()))
                viewHolder.txtTime.setVisibility(View.VISIBLE);
//            else
//                viewHolder.txtTime.setVisibility(View.GONE);
            if (((MediaMessage)baseMessage).getAttachment()!=null) {
                viewHolder.length.setText(Utils.getFileSize(((MediaMessage) baseMessage).getAttachment().getFileSize()));
                viewHolder.playBtn.setVisibility(View.VISIBLE);
            } else {
                viewHolder.length.setText("-");
                viewHolder.playBtn.setVisibility(View.GONE);
            }
            viewHolder.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            viewHolder.playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,CometChatMediaViewActivity.class);
                    intent.putExtra(UIKitConstants.IntentStrings.MEDIA_SIZE,
                            ((MediaMessage)baseMessage).getAttachment().getFileSize());
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, CometChatConstants.MESSAGE_TYPE_AUDIO);
                    intent.putExtra(UIKitConstants.IntentStrings.INTENT_MEDIA_MESSAGE,
                            ((MediaMessage)baseMessage).getAttachment().getFileUrl());
                    intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
                    intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());
                    context.startActivity(intent);
                }
            });
            viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isLongClickEnabled && !isTextMessageClick) {
                        isImageMessageClick = true;
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                        notifyItemChanged(i);
                    }
                    return true;
                }
            });
            viewHolder.reactionLayout.setVisibility(View.GONE);
            setReactionSupport(baseMessage,viewHolder.reactionLayout);
        }
    }

    public void stopPlayingAudio() {
        if (mediaPlayer!=null)
            mediaPlayer.stop();
    }
    /**
     * This method is called whenever viewType of item is file. It is used to bind FileMessageViewHolder
     * contents with MediaMessage at a given position.
     * It shows FileName, FileType, FileSize.
     *
     * @param viewHolder is a object of FileMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see MediaMessage
     * @see BaseMessage
     */
    private void setFileData(FileMessageViewHolder viewHolder, int i) {
        BaseMessage baseMessage = messageList.get(i);

          if (baseMessage!=null&&baseMessage.getDeletedAt()==0) {
              if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                  if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                      viewHolder.tvUser.setVisibility(View.GONE);
                      viewHolder.ivUser.setVisibility(View.GONE);
                  } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                      if (isUserDetailVisible) {
                          viewHolder.tvUser.setVisibility(View.VISIBLE);
                          viewHolder.ivUser.setVisibility(View.VISIBLE);
                      }
                      else {
                          viewHolder.tvUser.setVisibility(View.GONE);
                          viewHolder.ivUser.setVisibility(View.INVISIBLE);
                      }
                      setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                      viewHolder.tvUser.setText(baseMessage.getSender().getName());
                  }
              }
              if (((MediaMessage)baseMessage).getAttachment()!=null) {
                  viewHolder.fileName.setText(((MediaMessage) baseMessage).getAttachment().getFileName());
                  viewHolder.fileExt.setText(((MediaMessage) baseMessage).getAttachment().getFileExtension());
                  int fileSize = ((MediaMessage) baseMessage).getAttachment().getFileSize();

                  viewHolder.fileSize.setText(Utils.getFileSize(fileSize));
              } else {
                  viewHolder.fileName.setText(context.getString(R.string.uploading));
                  viewHolder.fileExt.setText("-");
                  viewHolder.fileSize.setText("-");
              }
              
              FeatureRestriction.isThreadedMessagesEnabled(new FeatureRestriction.OnSuccessListener() {
                  @Override
                  public void onSuccess(Boolean booleanVal) {
                      if (baseMessage.getReplyCount()!=0 && booleanVal) {
                          viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                          viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
                      } else {
                          viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
                      }
                  }
              });
              viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
                  Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
//                  intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                  intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
                  intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
                  intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
                  intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
                  intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
                  intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
                  intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
                  intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());
                  intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME,((MediaMessage)baseMessage).getAttachment().getFileName());
                  intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION,((MediaMessage)baseMessage).getAttachment().getFileExtension());
                  intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL,((MediaMessage)baseMessage).getAttachment().getFileUrl());
                  intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE,((MediaMessage)baseMessage).getAttachment().getFileSize());
                  intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
                  intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
                  if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                      intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
                  }
                  else {
                      if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                          intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                      else
                          intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
                  }
                  context.startActivity(intent);
              });


              showMessageTime(viewHolder, baseMessage);
              setColorFilter(baseMessage,viewHolder.cvMessageView);
//              if (selectedItemList.contains(baseMessage.getId()))
                  viewHolder.txtTime.setVisibility(View.VISIBLE);
//              else
//                  viewHolder.txtTime.setVisibility(View.GONE);


              viewHolder.rlMessageBubble.setOnClickListener(view -> {
//                  if (isLongClickEnabled && !isTextMessageClick) {
//                          setLongClickSelectedItem(baseMessage);
//                  }
//                  else {
                  MediaUtils.openFile(((MediaMessage) baseMessage).getAttachment().getFileUrl(),context);
                  setSelectedMessage(baseMessage.getId());
//                  }
                  notifyItemChanged(i);
              });
              viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                  @Override
                  public boolean onLongClick(View v) {
                      if (!isLongClickEnabled && !isTextMessageClick) {
                          isImageMessageClick = true;
                          setLongClickSelectedItem(baseMessage);
                          messageLongClick.setLongMessageClick(longselectedItemList);
                          notifyItemChanged(i);
                      }
                      return true;
                  }
              });
          }

        viewHolder.reactionLayout.setVisibility(View.GONE);
        setReactionSupport(baseMessage,viewHolder.reactionLayout);
    }


    /**
     * This method is called whenever viewType of item is media. It is used to bind ImageMessageViewHolder
     * contents with MediaMessage at a given position.
     * It loads image of MediaMessage using its url.
     *
     * @param viewHolder is a object of ImageMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see MediaMessage
     * @see BaseMessage
     */
    private void setImageData(ImageMessageViewHolder viewHolder, int i) {


        BaseMessage baseMessage = messageList.get(i);
        if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                viewHolder.tvUser.setVisibility(View.GONE);
                viewHolder.ivUser.setVisibility(View.GONE);
            } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                if (isUserDetailVisible) {
                    viewHolder.tvUser.setVisibility(View.VISIBLE);
                    viewHolder.ivUser.setVisibility(View.VISIBLE);
                }
                else {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.INVISIBLE);
                }
                setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                viewHolder.tvUser.setText(baseMessage.getSender().getName());
            }
        }


        boolean isImageNotSafe = Extensions.getImageModeration(context,baseMessage);
        String smallUrl = Extensions.getThumbnailGeneration(context,baseMessage);

        Attachment attachment = ((MediaMessage)baseMessage).getAttachment();
        if (attachment!=null) {
            if (smallUrl!=null) {
                if (attachment.getFileExtension().equalsIgnoreCase(".gif")) {
                    setImageDrawable(viewHolder,smallUrl,true,false);
                } else {
                    setImageDrawable(viewHolder,smallUrl,false,isImageNotSafe);
                }
            } else {
                if (attachment.getFileExtension().equalsIgnoreCase(".gif"))
                    setImageDrawable(viewHolder,((MediaMessage)baseMessage).getAttachment().getFileUrl(),true,false);
                else
                    setImageDrawable(viewHolder,((MediaMessage)baseMessage).getAttachment().getFileUrl(),false,isImageNotSafe);
            }
        }
        if (isImageNotSafe) {
            viewHolder.sensitiveLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.sensitiveLayout.setVisibility(View.GONE);
        }

        FeatureRestriction.isThreadedMessagesEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (baseMessage.getReplyCount()!=0 && booleanVal) {
                    viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                    viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
                } else {
                    viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
                }
            }
        });
        if (baseMessage.getMetadata()!=null) {
            try {
                String filePath = baseMessage.getMetadata().getString("path");
                Glide.with(context).load(filePath).diskCacheStrategy(DiskCacheStrategy.NONE).into(viewHolder.imageView);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
            Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
            intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
            intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
            intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
            intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
            intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME,((MediaMessage)baseMessage).getAttachment().getFileName());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION,((MediaMessage)baseMessage).getAttachment().getFileExtension());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL,((MediaMessage)baseMessage).getAttachment().getFileUrl());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE,((MediaMessage)baseMessage).getAttachment().getFileSize());
            intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
            }
            else {
                if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                    intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                else
                    intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
            }
            context.startActivity(intent);
        });



        showMessageTime(viewHolder, baseMessage);
//        if (selectedItemList.contains(baseMessage.getId()))
            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);


        viewHolder.rlMessageBubble.setOnClickListener(view -> {
            if (isImageNotSafe) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Unsafe Content");
                alert.setIcon(R.drawable.ic_hand);
                alert.setMessage("Are you surely want to see this unsafe content");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MediaUtils.openFile(((MediaMessage) baseMessage).getAttachment().getFileUrl(), context);
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.create().show();
            } else {
                setSelectedMessage(baseMessage.getId());
                notifyDataSetChanged();
                openMediaViewActivity(baseMessage);
            }

        });
        viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true;
                    setLongClickSelectedItem(baseMessage);
                    messageLongClick.setLongMessageClick(longselectedItemList);
                    notifyItemChanged(i);
                }
                return true;
            }
        });

        viewHolder.reactionLayout.setVisibility(View.GONE);
        setReactionSupport(baseMessage,viewHolder.reactionLayout);
    }


    private void setImageDrawable(ImageMessageViewHolder viewHolder, String url, boolean gif,boolean isImageNotSafe) {
        if (gif) {
            Glide.with(context).asGif().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).load(url).into(viewHolder.imageView);
        } else {
            Glide.with(context).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).load(url).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    if (isImageNotSafe)
                        viewHolder.imageView.setImageBitmap(Utils.blur(context, resource));
                    else
                        viewHolder.imageView.setImageBitmap(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
        }

    }

    private void openMediaViewActivity(BaseMessage baseMessage) {
        if (((MediaMessage)baseMessage).getAttachment()!=null) {
            Intent intent = new Intent(context, CometChatMediaViewActivity.class);
            intent.putExtra(UIKitConstants.IntentStrings.NAME, baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.UID, baseMessage.getSender().getUid());
            intent.putExtra(UIKitConstants.IntentStrings.SENTAT, baseMessage.getSentAt());
            intent.putExtra(UIKitConstants.IntentStrings.INTENT_MEDIA_MESSAGE,
                    ((MediaMessage) baseMessage).getAttachment().getFileUrl());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, baseMessage.getType());
            context.startActivity(intent);
        }
    }


    /**
     * This method is called whenever viewType of item is sticker. It is used to bind StickerMessageViewHolder
     * contents with CustomMessage at a given position.
     * It loads image from url availabl in CustomMessage.
     *
     * @param viewHolder is a object of StickerMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see CustomMessage
     * @see BaseMessage
     */
    private void setStickerData(StickerMessageViewHolder viewHolder, int i) {


        BaseMessage baseMessage = messageList.get(i);
        if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                viewHolder.tvUser.setVisibility(View.GONE);
                viewHolder.ivUser.setVisibility(View.GONE);
            } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                if (isUserDetailVisible) {
                    viewHolder.tvUser.setVisibility(View.VISIBLE);
                    viewHolder.ivUser.setVisibility(View.VISIBLE);
                }
                else {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.INVISIBLE);
                }
                setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                viewHolder.tvUser.setText(baseMessage.getSender().getName());
            }
        }


        viewHolder.stickerView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_defaulf_image));
        try {
            Glide.with(context).load(((CustomMessage)baseMessage).getCustomData().getString("sticker_url")).into(viewHolder.stickerView);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FeatureRestriction.isThreadedMessagesEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (baseMessage.getReplyCount()!=0 && booleanVal) {
                    viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                    viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
                } else {
                    viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
                }
            }
        });

        viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
            Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
            intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
            intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
            intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, UIKitConstants.IntentStrings.STICKERS);
            intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
            intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());
            try {
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME,((CustomMessage)baseMessage).getCustomData().getString("name"));
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL,((CustomMessage)baseMessage).getCustomData().getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
            }
            else {
                if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                    intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                else
                    intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
            }
            context.startActivity(intent);
        });



        showMessageTime(viewHolder, baseMessage);
//        if (selectedItemList.contains(baseMessage.getId()))
        viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);


        viewHolder.stickerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true;
                    setLongClickSelectedItem(baseMessage);
                    messageLongClick.setLongMessageClick(longselectedItemList);
                    notifyItemChanged(i);
                }
                return true;
            }
        });

        viewHolder.reactionLayout.setVisibility(View.GONE);
        setReactionSupport(baseMessage,viewHolder.reactionLayout);
    }


    private void setVideoData(VideoMessageViewHolder viewHolder, int i) {


        BaseMessage baseMessage = messageList.get(i);
        if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                viewHolder.tvUser.setVisibility(View.GONE);
                viewHolder.ivUser.setVisibility(View.GONE);
            } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                if (isUserDetailVisible) {
                    viewHolder.tvUser.setVisibility(View.VISIBLE);
                    viewHolder.ivUser.setVisibility(View.VISIBLE);
                }
                else {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.INVISIBLE);
                }
                setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                viewHolder.tvUser.setText(baseMessage.getSender().getName());
            }
        }
        if (((MediaMessage)baseMessage).getAttachment()!=null)
            Glide.with(context).load(((MediaMessage) baseMessage).getAttachment().getFileUrl()).into(viewHolder.imageView);


        FeatureRestriction.isThreadedMessagesEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (baseMessage.getReplyCount()!=0 && booleanVal) {
                    viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                    viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
                } else {
                    viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
                }
            }
        });

        viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
            Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
//            intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
            intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
            intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
            intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
            intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
            intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
            intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME,((MediaMessage)baseMessage).getAttachment().getFileName());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION,((MediaMessage)baseMessage).getAttachment().getFileExtension());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL,((MediaMessage)baseMessage).getAttachment().getFileUrl());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE,((MediaMessage)baseMessage).getAttachment().getFileSize());
            intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
            }
            else {
                if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                    intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                else
                    intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
            }
            context.startActivity(intent);
        });


        showMessageTime(viewHolder, baseMessage);
//        if (selectedItemList.contains(baseMessage.getId()))
            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);
//


        viewHolder.rlMessageBubble.setOnClickListener(view -> {
            setSelectedMessage(baseMessage.getId());
            notifyDataSetChanged();

        });
        viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true;
                    setLongClickSelectedItem(baseMessage);
                    messageLongClick.setLongMessageClick(longselectedItemList);
                    notifyItemChanged(i);
                }
                return true;
            }
        });
        viewHolder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMediaViewActivity(baseMessage);
            }
        });


        viewHolder.reactionLayout.setVisibility(View.GONE);
        setReactionSupport(baseMessage,viewHolder.reactionLayout);
    }





    private void setDeleteData(DeleteMessageViewHolder viewHolder, int i) {


        BaseMessage baseMessage = messageList.get(i);
        if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                viewHolder.tvUser.setVisibility(View.GONE);
                viewHolder.ivUser.setVisibility(View.GONE);
            } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                if (isUserDetailVisible)
                {
                    viewHolder.tvUser.setVisibility(View.VISIBLE);
                    viewHolder.ivUser.setVisibility(View.VISIBLE);
                }
                else
                {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.INVISIBLE);
                }
                setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                viewHolder.tvUser.setText(baseMessage.getSender().getName());
            }
        }
        if (baseMessage.getDeletedAt()!=0) {
            viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
            viewHolder.txtMessage.setText(R.string.this_message_deleted);
            viewHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.secondaryTextColor));
            viewHolder.txtMessage.setTypeface(null, Typeface.ITALIC);
        }
        showMessageTime(viewHolder, baseMessage);
        setColorFilter(baseMessage,viewHolder.cardView);
//        if (selectedItemList.contains(baseMessage.getId()))
            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);
//
    }





    /**
     * This method is called whenever viewType of item is Action. It is used to bind
     * ActionMessageViewHolder contents with Action at a given position. It shows action message
     * or call status based on message category
     *
     * @param viewHolder is a object of ActionMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see Action
     * @see Call
     * @see BaseMessage
     */
    private void setActionData(ActionMessageViewHolder viewHolder, int i) {
        BaseMessage baseMessage = messageList.get(i);
        if(Utils.isDarkMode(context))
            viewHolder.textView.setTextColor(context.getResources().getColor(R.color.textColorWhite));
        else
            viewHolder.textView.setTextColor(context.getResources().getColor(R.color.primaryTextColor));

        viewHolder.textView.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));
        if (baseMessage instanceof Action) {
            String actionMessage = "";
            if (((Action) baseMessage).getAction().equals(CometChatConstants.ActionKeys.ACTION_JOINED))
                actionMessage = ((User)((Action) baseMessage).getActioBy()).getName()+" "+context.getString(R.string.joined);
            else if (((Action)baseMessage).getAction().equals(CometChatConstants.ActionKeys.ACTION_MEMBER_ADDED))
                actionMessage = ((User)((Action) baseMessage).getActioBy()).getName()+" "
                        +context.getString(R.string.added)+" "+((User)((Action) baseMessage).getActionOn()).getName();
            else if (((Action) baseMessage).getAction().equals(CometChatConstants.ActionKeys.ACTION_KICKED))
            actionMessage = ((User)((Action) baseMessage).getActioBy()).getName()+" "
                    +context.getString(R.string.kicked_by)+" "+((User)((Action) baseMessage).getActionOn()).getName();
            else if (((Action) baseMessage).getAction().equals(CometChatConstants.ActionKeys.ACTION_BANNED))
                actionMessage = ((User)((Action) baseMessage).getActioBy()).getName()+" "
                        +context.getString(R.string.ban)+" "+((User)((Action) baseMessage).getActionOn()).getName();
            else if (((Action) baseMessage).getAction().equals(CometChatConstants.ActionKeys.ACTION_UNBANNED))
                actionMessage = ((User)((Action) baseMessage).getActioBy()).getName()+" "
                        +context.getString(R.string.unban)+" "+((User)((Action) baseMessage).getActionOn()).getName();
            else if (((Action) baseMessage).getAction().equals(CometChatConstants.ActionKeys.ACTION_LEFT))
                actionMessage = ((User)((Action)baseMessage).getActioBy()).getName()+" "+context.getString(R.string.left);
            else if (((Action) baseMessage).getAction().equals(CometChatConstants.ActionKeys.ACTION_SCOPE_CHANGED))
                if (((Action)baseMessage).getNewScope().equals(CometChatConstants.SCOPE_MODERATOR)) {
                    actionMessage = ((User) ((Action) baseMessage).getActioBy()).getName() + " " + context.getString(R.string.made) + " "
                            + ((User) ((Action) baseMessage).getActionOn()).getName() + " " + context.getString(R.string.moderator);
                } else if (((Action)baseMessage).getNewScope().equals(CometChatConstants.SCOPE_ADMIN)) {
                    actionMessage = ((User) ((Action) baseMessage).getActioBy()).getName() + " " + context.getString(R.string.made) + " "
                            + ((User) ((Action) baseMessage).getActionOn()).getName() + " " + context.getString(R.string.admin);
                } else if (((Action)baseMessage).getNewScope().equals(CometChatConstants.SCOPE_PARTICIPANT)) {
                    actionMessage = ((User) ((Action) baseMessage).getActioBy()).getName() + " " + context.getString(R.string.made) + " "
                            + ((User) ((Action) baseMessage).getActionOn()).getName() + " " + context.getString(R.string.participant);
                }
            else
                actionMessage = ((Action) baseMessage).getMessage();
            viewHolder.textView.setText(actionMessage);

        }
        else if (baseMessage instanceof Call) {
            Call call = ((Call) baseMessage);
            String callMessageText = "";
            boolean isMissed = false,isIncoming = false,isVideo = false;
            if (call.getCallStatus().equals(CometChatConstants.CALL_STATUS_INITIATED)) {
                callMessageText = call.getSender().getName() + " " + context.getString(R.string.initiated);
            }
            else if(call.getCallStatus().equals(CometChatConstants.CALL_STATUS_UNANSWERED) ||
                    call.getCallStatus().equals(CometChatConstants.CALL_STATUS_CANCELLED)) {
                callMessageText = context.getResources().getString(R.string.missed_call);
                isMissed = true;
            } else if(call.getCallStatus().equals(CometChatConstants.CALL_STATUS_REJECTED)) {
                callMessageText = context.getResources().getString(R.string.rejected_call);
            } else if(call.getCallStatus().equals(CometChatConstants.CALL_STATUS_ONGOING)) {
                callMessageText = context.getString(R.string.ongoing);
            } else if (call.getCallStatus().equals(CometChatConstants.CALL_STATUS_ENDED)){
                callMessageText = context.getString(R.string.ended);
            } else {
                callMessageText = call.getCallStatus();
            }
            if (((User) call.getCallInitiator()).getUid().equals(loggedInUser)) {
                isIncoming = false;
            } else {
                isIncoming = true;
            }

            if(call.getType().equals(CometChatConstants.CALL_TYPE_VIDEO))
            {
                callMessageText = callMessageText+" "+context.getResources().getString(R.string.video_call);
                isVideo = true;
            }
            else
            {
                callMessageText = callMessageText+" "+context.getResources().getString(R.string.audio_call);
                isVideo = false;
            }
            viewHolder.textView.setText(callMessageText);
        }
    }

    /**
     * This method is used to show message time below message whenever we click on message.
     * Since we have different ViewHolder, we have to pass <b>txtTime</b> of each viewHolder to
     * <code>setStatusIcon(RecyclerView.ViewHolder viewHolder,BaseMessage baseMessage)</code>
     * along with baseMessage.
     * @see MessageAdapter#setStatusIcon(ProgressBar,TextView, BaseMessage)
     *      *
     * @param viewHolder is object of ViewHolder.
     * @param baseMessage is a object of BaseMessage.
     *
     * @see BaseMessage
     */
    private void showMessageTime(RecyclerView.ViewHolder viewHolder, BaseMessage baseMessage) {

        if (viewHolder instanceof TextMessageViewHolder) {
            setStatusIcon(((TextMessageViewHolder) viewHolder).progressBar,((TextMessageViewHolder) viewHolder).txtTime, baseMessage);
        } else if (viewHolder instanceof LinkMessageViewHolder) {
            setStatusIcon(((LinkMessageViewHolder) viewHolder).progressBar,((LinkMessageViewHolder) viewHolder).txtTime, baseMessage);
        } else if (viewHolder instanceof ImageMessageViewHolder) {
            setStatusIcon(((ImageMessageViewHolder) viewHolder).progressBar,((ImageMessageViewHolder) viewHolder).txtTime, baseMessage);
        } else if (viewHolder instanceof FileMessageViewHolder) {
            setStatusIcon(((FileMessageViewHolder) viewHolder).progressBar,((FileMessageViewHolder) viewHolder).txtTime, baseMessage);
        } else if (viewHolder instanceof VideoMessageViewHolder) {
            setStatusIcon(((VideoMessageViewHolder) viewHolder).progressBar,((VideoMessageViewHolder) viewHolder).txtTime, baseMessage);
        } else if (viewHolder instanceof AudioMessageViewHolder) {
            setStatusIcon(((AudioMessageViewHolder) viewHolder).progressBar,((AudioMessageViewHolder)viewHolder).txtTime, baseMessage);
        } else if (viewHolder instanceof LocationMessageViewHolder){
            setStatusIcon(null,((LocationMessageViewHolder) viewHolder).txtTime, baseMessage);
        } else if (viewHolder instanceof  PollMessageViewHolder) {
            setStatusIcon(null,(((PollMessageViewHolder) viewHolder).txtTime),baseMessage);
        } else if (viewHolder instanceof StickerMessageViewHolder) {
            setStatusIcon(null,(((StickerMessageViewHolder) viewHolder).txtTime),baseMessage);
        } else if (viewHolder instanceof WhiteBoardViewHolder) {
            setStatusIcon(null,(((WhiteBoardViewHolder) viewHolder).txtTime),baseMessage);
        } else if (viewHolder instanceof  WriteBoardViewHolder) {
            setStatusIcon(null,(((WriteBoardViewHolder) viewHolder).txtTime),baseMessage);
        } else if (viewHolder instanceof GroupCallMessageViewHolder) {
            setStatusIcon(null,(((GroupCallMessageViewHolder) viewHolder).txtTime),baseMessage);
        }

    }

    /**
     * This method is used set message time i.e sentAt, deliveredAt & readAt in <b>txtTime</b>.
     * If sender of baseMessage is user then for user side messages it will show readAt, deliveredAt
     * time along with respective icon. For reciever side message it will show only deliveredAt time
     *
     * @param txtTime is a object of TextView which will show time.
     * @param baseMessage is a object of BaseMessage used to identify baseMessage sender.
     * @see BaseMessage
     */
    private void setStatusIcon(ProgressBar progressBar,TextView txtTime, BaseMessage baseMessage) {
        FeatureRestriction.isDeliveryReceiptsEnabled(new FeatureRestriction.OnSuccessListener() {
            @Override
            public void onSuccess(Boolean booleanVal) {
                if (booleanVal) {
                    if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                        if (baseMessage.getReceiverType()!=null && baseMessage.getReceiverType()
                                .equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                            if (progressBar!=null)
                                progressBar.setVisibility(View.GONE);
                            if (baseMessage.getReadAt() != 0) {
                                txtTime.setText(Utils.getHeaderDate(baseMessage.getReadAt() * 1000));
                                txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_message_read, 0);
                                txtTime.setCompoundDrawablePadding(10);
                            } else if (baseMessage.getDeliveredAt() != 0) {
                                txtTime.setText(Utils.getHeaderDate(baseMessage.getDeliveredAt() * 1000));
                                txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_message_delivered, 0);
                                txtTime.setCompoundDrawablePadding(10);
                            } else if (baseMessage.getSentAt()>0){
                                txtTime.setText(Utils.getHeaderDate(baseMessage.getSentAt() * 1000));
                                txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_message_sent, 0);
                                txtTime.setCompoundDrawablePadding(10);
                            } else if (baseMessage.getSentAt()==-1) {
                                txtTime.setText("");
                                txtTime.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_info_red,0);
                            } else {
                                txtTime.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                                txtTime.setText("");
                                if (progressBar!=null)
                                    progressBar.setVisibility(View.VISIBLE);
                            }
                        } else {
                            txtTime.setText(Utils.getHeaderDate(baseMessage.getSentAt() * 1000));
                        }
                    } else {
                        txtTime.setText(Utils.getHeaderDate(baseMessage.getSentAt() * 1000));
                    }
                }
            }
        });
    }

    /**
     * This method is called whenever viewType of item is TextMessage. It is used to bind
     * TextMessageViewHolder content with TextMessage at given position.
     * It shows text of a message if deletedAt = 0 else it shows "message deleted"
     *
     * @param viewHolder is a object of TextMessageViewHolder.
     * @param i is postion of item in recyclerView.
     * @see TextMessage
     * @see BaseMessage
     */
    private void setTextData(TextMessageViewHolder viewHolder, int i) {

        BaseMessage baseMessage = messageList.get(i);
        if (baseMessage!=null) {
             if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                 if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                     viewHolder.tvUser.setVisibility(View.GONE);
                     viewHolder.ivUser.setVisibility(View.GONE);
                 } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                     if (isUserDetailVisible)
                     {
                         viewHolder.tvUser.setVisibility(View.VISIBLE);
                         viewHolder.ivUser.setVisibility(View.VISIBLE);
                     }
                     else
                     {
                         viewHolder.tvUser.setVisibility(View.GONE);
                         viewHolder.ivUser.setVisibility(View.INVISIBLE);
                     }
                     setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                     viewHolder.tvUser.setText(baseMessage.getSender().getName());
                 }
                 boolean isSentimentNegative = Extensions.checkSentiment(baseMessage);
                 if (isSentimentNegative) {
                     viewHolder.txtMessage.setVisibility(View.GONE);
                     viewHolder.sentimentVw.setVisibility(View.VISIBLE);
                 }
                 else {
                     viewHolder.txtMessage.setVisibility(View.VISIBLE);
                     viewHolder.sentimentVw.setVisibility(View.GONE);
                 }
                 viewHolder.viewSentimentMessage.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         AlertDialog.Builder sentimentAlert = new AlertDialog.Builder(context)
                                 .setTitle(context.getResources().getString(R.string.sentiment_alert))
                                 .setMessage(context.getResources().getString(R.string.sentiment_alert_message))
                                 .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                         viewHolder.txtMessage.setVisibility(View.VISIBLE);
                                         viewHolder.sentimentVw.setVisibility(View.GONE);
                                     }
                                 })
                                 .setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                         dialog.dismiss();
                                     }
                                 });
                         sentimentAlert.create().show();
                     }
                 });
             }
             if (baseMessage.getMetadata()!=null) {
                 try {
                     JSONObject metaData = baseMessage.getMetadata();
                     if (metaData.has("reply-message")) {
                         JSONObject replyMessageJSON = metaData.getJSONObject("reply-message");
                         BaseMessage replyMessage = CometChatHelper.processMessage(replyMessageJSON);
                         String messageType = replyMessage.getType();
                         viewHolder.replyLayout.setVisibility(View.VISIBLE);
                         viewHolder.replyUser.setVisibility(View.VISIBLE);
                         if (replyMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER))
                             viewHolder.replyUser.setText(replyMessage.getSender().getName());
                         else
                             viewHolder.replyUser.setText(replyMessage.getSender().getName()+
                                     " ~ "+((Group)replyMessage.getReceiver()).getName());
                         if (messageType.equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                            String message = ((TextMessage)replyMessage).getText();
                            if(CometChat.isExtensionEnabled("profanity-filter")) {
                                message = Extensions.checkProfanityMessage(context,replyMessage);
                            }
                            if(CometChat.isExtensionEnabled("data-masking")) {
                                message = Extensions.checkDataMasking(context,replyMessage);
                            }
                            viewHolder.replyMessageImage.setVisibility(View.GONE);
                            viewHolder.replyMessage.setText(message);
                         } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_IMAGE)) {
                             viewHolder.replyMessage.setText(context.getResources().getString(R.string.message_image));
                             viewHolder.replyMessageImage.setVisibility(View.GONE);
                         } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_AUDIO)) {
                             viewHolder.replyMessageImage.setVisibility(View.GONE);
                                 viewHolder.replyMessage.setText(context.getResources()
                                         .getString(R.string.message_audio));
                         } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {
                             viewHolder.replyMessageImage.setVisibility(View.GONE);
                             viewHolder.replyMessage.setText(context.getResources().getString(R.string.message_video));
                         } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_FILE)) {
                                 viewHolder.replyMessage.setText(context.getResources().getString(R.string.message_file));
                             viewHolder.replyMessageImage.setVisibility(View.GONE);
                         } else if (messageType.equals(UIKitConstants.IntentStrings.LOCATION)) {
                             viewHolder.replyMessage.setText(R.string.custom_message_location);
                             viewHolder.replyMessageImage.setImageResource(R.drawable.default_map);
                         } else if (messageType.equals(UIKitConstants.IntentStrings.POLLS)) {
                             viewHolder.replyMessageImage.setVisibility(View.GONE);
                             viewHolder.replyMessage.setText(context.getString(R.string.custom_message_poll));
                         } else if (messageType.equals(UIKitConstants.IntentStrings.STICKERS)) {
                             viewHolder.replyMessageImage.setVisibility(View.GONE);
                             viewHolder.replyMessage.setText(String.format(context.getString(R.string.custom_message_sticker)));
                         } else if (messageType.equals(UIKitConstants.IntentStrings.WHITEBOARD)) {
                             viewHolder.replyMessageImage.setVisibility(View.GONE);
                             viewHolder.replyMessage.setText(context.getString(R.string.custom_message_whiteboard));
                         } else if (messageType.equals(UIKitConstants.IntentStrings.WRITEBOARD)) {
                             viewHolder.replyMessageImage.setVisibility(View.GONE);
                             viewHolder.replyMessage.setText(context.getString(R.string.custom_message_document));
                         } else if (messageType.equals(UIKitConstants.IntentStrings.GROUP_CALL)) {
                             viewHolder.replyMessageImage.setVisibility(View.GONE);
                             viewHolder.replyMessage.setText(context.getString(R.string.custom_message_meeting));
                         }
                         viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                             @Override
                             public boolean onLongClick(View view) {
                                 if (!isImageMessageClick) {
                                     isLongClickEnabled = true;
                                     isTextMessageClick = true;
                                     setLongClickSelectedItem(baseMessage);
                                     messageLongClick.setLongMessageClick(longselectedItemList);
                                     notifyItemChanged(i);
                                 }
                                 return true;
                             }
                         });
                     } else {
                         viewHolder.replyLayout.setVisibility(View.GONE);
                     }
                 } catch(Exception e){
                     Log.e(TAG, "setTextData: " + e.getMessage());
                 }
             }

            FeatureRestriction.isThreadedMessagesEnabled(new FeatureRestriction.OnSuccessListener() {
                @Override
                public void onSuccess(Boolean booleanVal) {
                    if (baseMessage.getReplyCount()!=0 && booleanVal) {
                        viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                        viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
                    } else {
                        viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
                    }
                }
            });
             viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
                Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
//                intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
                intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
                intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
                intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
                intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());
                intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE,((TextMessage)baseMessage).getText());

                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
                intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
                }
                else {
                    if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                    else
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
                }
                context.startActivity(intent);
            });

            String txtMessage = ((TextMessage) baseMessage).getText().trim();
            viewHolder.txtMessage.setTextSize(16f);
            int count = 0;
            CharSequence processed = EmojiCompat.get().process(txtMessage, 0,
                        txtMessage.length() -1, Integer.MAX_VALUE, EmojiCompat.REPLACE_STRATEGY_ALL);
            if (processed instanceof Spannable) {
                Spannable spannable = (Spannable) processed;
                count = spannable.getSpans(0, spannable.length() - 1, EmojiSpan.class).length;
                if (PatternUtils.removeEmojiAndSymbol(txtMessage).trim().length() == 0) {
                    int tempcount = count;
                    FeatureRestriction.isLargeSizeEmojisEnabled(new FeatureRestriction.OnSuccessListener() {
                        @Override
                        public void onSuccess(Boolean booleanVal) {
                            if (booleanVal) {
                                if (tempcount == 1) {
                                    viewHolder.txtMessage.setTextSize((int) Utils.dpToPx(context, 32));
                                } else if (tempcount == 2) {
                                    viewHolder.txtMessage.setTextSize((int) Utils.dpToPx(context, 24));
                                }
                            }
                        }
                    });
                }
            }

            String message = txtMessage;

            if(CometChat.isExtensionEnabled("profanity-filter")) {
                message = Extensions.checkProfanityMessage(context,baseMessage);
            }

            if(CometChat.isExtensionEnabled("data-masking")) {
                message = Extensions.checkDataMasking(context,baseMessage);
            }


            if (baseMessage.getMetadata()!=null && baseMessage.getMetadata().has("values")) {
                try {
                    if (Extensions.isMessageTranslated(baseMessage.getMetadata().getJSONObject("values"), ((TextMessage) baseMessage).getText())) {
                        String translatedMessage = Extensions.getTranslatedMessage(baseMessage);
                        message = message + "\n(" + translatedMessage + ")";
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, context.getString(R.string.no_translation_available), Toast.LENGTH_SHORT).show();
                }
            }

            viewHolder.txtMessage.setText(message);
            viewHolder.txtMessage.setTypeface(fontUtils.getTypeFace(FontUtils.robotoRegular));

            PatternUtils.setHyperLinkSupport(context,viewHolder.txtMessage);

            viewHolder.txtMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!isImageMessageClick) {
                        isLongClickEnabled = true;
                        isTextMessageClick = true;
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                        notifyItemChanged(i);
                    }
                    return true;
                }
            });
            if (baseMessage.getSender().getUid().equals(loggedInUser.getUid()))
                 viewHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.textColorWhite));
            else
                 viewHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.primaryTextColor));

             showMessageTime(viewHolder, baseMessage);
//             if (messageList.get(messageList.size()-1).equals(baseMessage))
//             {
//                 selectedItemList.add(baseMessage.getId());
//             }
//             if (selectedItemList.contains(baseMessage.getId()))
                 viewHolder.txtTime.setVisibility(View.VISIBLE);
//             else
//                 viewHolder.txtTime.setVisibility(View.GONE);

              setColorFilter(baseMessage,viewHolder.cvMessageView);

//             viewHolder.rlMessageBubble.setOnClickListener(view -> {
//                 if (isLongClickEnabled && !isImageMessageClick) {
//                     setLongClickSelectedItem(baseMessage);
//                     messageLongClick.setLongMessageClick(longselectedItemList);
//                 }
//                 else {
//                     setSelectedMessage(baseMessage.getId());
//                 }
//                 notifyDataSetChanged();
//
//             });

//             viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
//                 @Override
//                 public boolean onLongClick(View view) {
//                     if (!isImageMessageClick) {
//                         isLongClickEnabled = true;
//                         isTextMessageClick = true;
//                         setLongClickSelectedItem(baseMessage);
//                         messageLongClick.setLongMessageClick(longselectedItemList);
//                         notifyDataSetChanged();
//                     }
//                         return true;
//                 }
//             });

            viewHolder.reactionLayout.setVisibility(View.GONE);
            setReactionSupport(baseMessage,viewHolder.reactionLayout);
             viewHolder.itemView.setTag(R.string.message, baseMessage);
         }
    }

    private void setReactionSupport(BaseMessage baseMessage, ChipGroup reactionLayout) {
        HashMap<String,String> reactionOnMessage = Extensions.getReactionsOnMessage(baseMessage);
        if (reactionOnMessage!=null && reactionOnMessage.size()>0) {
            reactionLayout.setVisibility(View.VISIBLE);
            reactionLayout.removeAllViews();
            for (String str : reactionOnMessage.keySet()) {
//                if (reactionLayout.getChildCount()<reactionOnMessage.size()) {
                    Chip chip = new Chip(context);
                    chip.setChipStrokeWidth(2f);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(context.getResources().getColor(android.R.color.transparent)));
                    chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor(UIKitSettings.getColor())));
                    chip.setText(str + " " + reactionOnMessage.get(str));
                    reactionLayout.addView(chip);
                    chip.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            Intent intent = new Intent(context, CometChatReactionInfoActivity.class);
                            intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,baseMessage.getMetadata().toString());
                            context.startActivity(intent);
                            return true;
                        }
                    });
                    chip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            JSONObject body=new JSONObject();
                            try {
                                body.put("msgId", baseMessage.getId());
                                body.put("emoji", str);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            CometChat.callExtension("reactions", "POST", "/v1/react", body,
                                    new CometChat.CallbackListener<JSONObject>() {
                                        @Override
                                        public void onSuccess(JSONObject responseObject) {
                                            // ReactionModel added successfully.
                                        }

                                        @Override
                                        public void onError(CometChatException e) {
                                            // Some error occured.
                                        }
                                    });
                        }
                    });
//                }
            }
        }
    }


    private void setCustomData(CustomMessageViewHolder viewHolder, int i) {

        BaseMessage baseMessage = messageList.get(i);
        if (baseMessage!=null) {
            if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.GONE);
                } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    if (isUserDetailVisible)
                    {
                        viewHolder.tvUser.setVisibility(View.VISIBLE);
                        viewHolder.ivUser.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        viewHolder.tvUser.setVisibility(View.GONE);
                        viewHolder.ivUser.setVisibility(View.INVISIBLE);
                    }
                    setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                    viewHolder.tvUser.setText(baseMessage.getSender().getName());
                }
            }

            viewHolder.txtMessage.setText(context.getResources().getString(R.string.custom_message));
            viewHolder.txtMessage.setTypeface(fontUtils.getTypeFace(FontUtils.robotoLight));
            if (baseMessage.getSender().getUid().equals(loggedInUser.getUid()))
                viewHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.textColorWhite));
            else
                viewHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.primaryTextColor));

            showMessageTime(viewHolder, baseMessage);
            if (messageList.get(messageList.size()-1).equals(baseMessage))
            {
                selectedItemList.add(baseMessage.getId());
            }
//            if (selectedItemList.contains(baseMessage.getId()))
                viewHolder.txtTime.setVisibility(View.VISIBLE);
//            else
//                viewHolder.txtTime.setVisibility(View.GONE);
            setColorFilter(baseMessage,viewHolder.cardView);
            viewHolder.rlMessageBubble.setOnClickListener(view -> {
                setSelectedMessage(baseMessage.getId());
                notifyDataSetChanged();

            });
            viewHolder.itemView.setTag(R.string.message, baseMessage);
        }
    }

    private void setColorFilter(BaseMessage baseMessage,View view){

        if (!longselectedItemList.contains(baseMessage))
        {
            if (baseMessage.getSender().equals(CometChat.getLoggedInUser())) {
                if (view instanceof CardView) {
                    ((CardView)view).setCardBackgroundColor(Color.parseColor(UIKitSettings.getColor()));
                } else {
                    if (Build.VERSION.SDK_INT >= 29) {
                        view.getBackground().setColorFilter(new PorterDuffColorFilter(Color.parseColor(UIKitSettings.getColor()), PorterDuff.Mode.SRC_ATOP));
                    } else {
                        view.getBackground().setColorFilter(Color.parseColor(UIKitSettings.getColor()), PorterDuff.Mode.SRC_ATOP);
                    }
                }
            } else {
                if (!(view instanceof CardView)) {
                    if (Build.VERSION.SDK_INT >= 29)
                        view.getBackground().setColorFilter(new PorterDuffColorFilter(context.getColor(R.color.message_bubble_grey), PorterDuff.Mode.SRC_ATOP));
                    else
                        view.getBackground().setColorFilter(context.getResources().getColor(R.color.message_bubble_grey), PorterDuff.Mode.SRC_ATOP);
                }
            }
        } else {
            if (baseMessage.getSentAt()>0) {
                if (baseMessage.getSender().equals(CometChat.getLoggedInUser()))
                    view.getBackground().setColorFilter(context.getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                else
                    view.getBackground().setColorFilter(context.getResources().getColor(R.color.secondaryTextColor), PorterDuff.Mode.SRC_ATOP);

            }
        }

    }


    private void setLinkData(LinkMessageViewHolder viewHolder, int i) {

        BaseMessage baseMessage = messageList.get(i);

        String url = null;

        if (baseMessage!=null) {
            if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    viewHolder.tvUser.setVisibility(View.GONE);
                    viewHolder.ivUser.setVisibility(View.GONE);
                } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    if (isUserDetailVisible)
                    {
                        viewHolder.tvUser.setVisibility(View.VISIBLE);
                        viewHolder.ivUser.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        viewHolder.tvUser.setVisibility(View.GONE);
                        viewHolder.ivUser.setVisibility(View.INVISIBLE);
                    }
                    setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
                    viewHolder.tvUser.setText(baseMessage.getSender().getName());
                }
            }
            if (baseMessage.getDeletedAt() == 0) {
                HashMap<String,JSONObject> extensionList = Extensions.extensionCheck(baseMessage);
                if (extensionList!=null) {
                    if (extensionList.containsKey("linkPreview")) {
                        JSONObject linkPreviewJsonObject = extensionList.get("linkPreview");
                        try {
                            String description = linkPreviewJsonObject.getString("description");
                            String image = linkPreviewJsonObject.getString("image");
                            String title = linkPreviewJsonObject.getString("title");
                            url = linkPreviewJsonObject.getString("url");
                            Log.e("setLinkData: ", baseMessage.toString() + "\n\n" + url + "\n" + description + "\n" + image);
                            viewHolder.linkTitle.setText(title);
                            viewHolder.linkSubtitle.setText(description);
                            Glide.with(context).load(Uri.parse(image)).timeout(1000).into(viewHolder.linkImg);
                            if (url.contains("youtu.be") || url.contains("youtube")) {
                                viewHolder.videoLink.setVisibility(View.VISIBLE);
                                viewHolder.linkVisit.setText(context.getResources().getString(R.string.view_on_youtube));
                            } else {
                                viewHolder.videoLink.setVisibility(View.GONE);
                                viewHolder.linkVisit.setText(context.getResources().getString(R.string.visit));
                            }
                            String messageStr = ((TextMessage) baseMessage).getText();
                            if (((TextMessage) baseMessage).getText().equals(url) || ((TextMessage) baseMessage).getText().equals(url + "/")) {
                                viewHolder.message.setVisibility(View.GONE);
                            } else {
                                viewHolder.message.setVisibility(View.VISIBLE);
                            }
                            viewHolder.message.setText(messageStr);
                        } catch (Exception e) {
                            Log.e("setLinkData: ", e.getMessage());
                        }
                    }
                }
            }

            FeatureRestriction.isThreadedMessagesEnabled(new FeatureRestriction.OnSuccessListener() {
                @Override
                public void onSuccess(Boolean booleanVal) {
                    if (baseMessage.getReplyCount()!=0 && booleanVal) {
                        viewHolder.tvThreadReplyCount.setVisibility(View.VISIBLE);
                        viewHolder.tvThreadReplyCount.setText(baseMessage.getReplyCount()+" "+context.getResources().getString(R.string.replies));
                    } else {
                        viewHolder.tvThreadReplyCount.setVisibility(View.GONE);
                    }
                }
            });

            viewHolder.tvThreadReplyCount.setOnClickListener(view -> {
                Intent intent = new Intent(context, CometChatThreadMessageListActivity.class);
//                intent.putExtra(StringContract.IntentStrings.PARENT_BASEMESSAGE,baseMessage.toString());
                intent.putExtra(UIKitConstants.IntentStrings.NAME,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
                intent.putExtra(UIKitConstants.IntentStrings.REPLY_COUNT,baseMessage.getReplyCount());
                intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getName());
                intent.putExtra(UIKitConstants.IntentStrings.PARENT_ID,baseMessage.getId());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,baseMessage.getType());
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY,baseMessage.getCategory());
                intent.putExtra(UIKitConstants.IntentStrings.REACTION_INFO,Extensions.getReactionsOnMessage(baseMessage));
                intent.putExtra(UIKitConstants.IntentStrings.SENTAT,baseMessage.getSentAt());
                if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_TEXT))
                    intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE,((TextMessage)baseMessage).getText());
                else {
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME,((MediaMessage)baseMessage).getAttachment().getFileName());
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION,((MediaMessage)baseMessage).getAttachment().getFileExtension());
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL,((MediaMessage)baseMessage).getAttachment().getFileUrl());
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE,((MediaMessage)baseMessage).getAttachment().getFileSize());
                }
                intent.putExtra(UIKitConstants.IntentStrings.TYPE,baseMessage.getReceiverType());
                if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                    intent.putExtra(UIKitConstants.IntentStrings.GUID,baseMessage.getReceiverUid());
                }
                else {
                    if (baseMessage.getReceiverUid().equals(loggedInUser.getUid()))
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getSender().getUid());
                    else
                        intent.putExtra(UIKitConstants.IntentStrings.UID,baseMessage.getReceiverUid());
                }
                context.startActivity(intent);
            });


            PatternUtils.setHyperLinkSupport(context,viewHolder.message);

            showMessageTime(viewHolder, baseMessage);
            String finalUrl = url;
            viewHolder.linkVisit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (finalUrl != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(finalUrl));
                        context.startActivity(intent);
                    }
                }
            });
//            if (selectedItemList.contains(baseMessage.getId()))
                viewHolder.txtTime.setVisibility(View.VISIBLE);
//            else
//                viewHolder.txtTime.setVisibility(View.GONE);
//            if (i < selectedItems.length && selectedItems[i] == 0) {
//                viewHolder.txtTime.setVisibility(View.GONE);
//            } else
//                viewHolder.txtTime.setVisibility(View.VISIBLE);

            setColorFilter(baseMessage,viewHolder.cvMessageView);
            viewHolder.rlMessageBubble.setOnClickListener(view -> {
                if (isLongClickEnabled && !isImageMessageClick) {
                    setLongClickSelectedItem(baseMessage);
                    messageLongClick.setLongMessageClick(longselectedItemList);
                }
                else {
                    setSelectedMessage(baseMessage.getId());
                }
                notifyItemChanged(i);

            });
            viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!isImageMessageClick) {
                        isLongClickEnabled = true;
                        isTextMessageClick = true;
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                        notifyItemChanged(i);
                    }
                    return true;
                }
            });
            viewHolder.reactionLayout.setVisibility(View.GONE);
            setReactionSupport(baseMessage,viewHolder.reactionLayout);
            viewHolder.itemView.setTag(R.string.message, baseMessage);
        }
    }

    public void setSelectedMessage(Integer id)
    {
        if (selectedItemList.contains(id))
            selectedItemList.remove(id);
        else
            selectedItemList.add(id);
    }

    public void setLongClickSelectedItem(BaseMessage baseMessage) {


        if (longselectedItemList.contains(baseMessage))
            longselectedItemList.remove(baseMessage);
        else
            longselectedItemList.add(baseMessage);
    }
    /**
     * This method is used to set avatar of groupMembers to show in groupMessages. If avatar of
     * group member is not available then it calls <code>setInitials(String name)</code> to show
     * first two letter of group member name.
     *
     * @param avatar is a object of Avatar
     * @param avatarUrl is a String. It is url of avatar.
     * @param name is a String. It is a name of groupMember.
     * @see CometChatAvatar
     *
     */
    private void setAvatar(CometChatAvatar avatar, String avatarUrl, String name) {

        if (avatarUrl != null && !avatarUrl.isEmpty())
            avatar.setAvatar(avatarUrl);
        else
            avatar.setInitials(name);

    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public long getHeaderId(int var1) {

        BaseMessage baseMessage = messageList.get(var1);
        return Long.parseLong(Utils.getDateId(baseMessage.getSentAt() * 1000));
    }

    @Override
    public DateItemHolder onCreateHeaderViewHolder(ViewGroup var1) {
        View view = LayoutInflater.from(var1.getContext()).inflate(R.layout.cometchat_messagedate_header,
                var1, false);

        return new DateItemHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(DateItemHolder var1, int var2, long var3) {
        BaseMessage baseMessage = messageList.get(var2);
        var1.txtMessageDate.setBackground(context.getResources().getDrawable(R.drawable.cc_rounded_date_button));
        if (baseMessage.getSentAt()>0) {
            Date date = new Date(baseMessage.getSentAt());
            String formattedDate = Utils.getDate(context,date.getTime());
            var1.txtMessageDate.setText(formattedDate);
        } else {
            var1.txtMessageDate.setText(context.getString(R.string.updating));
        }
    }

    /**
     * This method is used to maintain different viewType based on message category and type and
     * returns the different view types to adapter based on it.
     *
     * Ex :- For message with category <b>CometChatConstants.CATEGORY_MESSAGE</b> and type
     * <b>CometChatConstants.MESSAGE_TYPE_TEXT</b> and message is sent by a <b>Logged-in user</b>,
     * It will return <b>RIGHT_TEXT_MESSAGE</b>
     *
     *
     * @param position is a position of item in recyclerView.
     * @return It returns int which is value of view type of item.
     *
     * @see MessageAdapter#onCreateViewHolder(ViewGroup, int)
     * @see BaseMessage
     *
     */
    private int getItemViewTypes(int position) {
        BaseMessage baseMessage = messageList.get(position);
        HashMap<String,JSONObject> extensionList = Extensions.extensionCheck(baseMessage);
        if (baseMessage.getDeletedAt()==0) {
            if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_MESSAGE)) {
                switch (baseMessage.getType()) {

                    case CometChatConstants.MESSAGE_TYPE_TEXT:
                        if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                            if (extensionList != null && extensionList.containsKey("linkPreview") && extensionList.get("linkPreview") != null)
                                return RIGHT_LINK_MESSAGE;
                            else if (baseMessage.getMetadata()!=null && baseMessage.getMetadata().has("reply"))
                                return RIGHT_REPLY_TEXT_MESSAGE;
                            else
                                return RIGHT_TEXT_MESSAGE;
                        } else {
                            if (extensionList != null && extensionList.containsKey("linkPreview") && extensionList.get("linkPreview") != null)
                                return LEFT_LINK_MESSAGE;
                            else if (baseMessage.getMetadata()!=null && baseMessage.getMetadata().has("reply"))
                                return LEFT_REPLY_TEXT_MESSAGE;
                            else
                                return LEFT_TEXT_MESSAGE;
                        }
                    case CometChatConstants.MESSAGE_TYPE_AUDIO:
                        if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                            return RIGHT_AUDIO_MESSAGE;
                        } else {
                            return LEFT_AUDIO_MESSAGE;
                        }
                    case CometChatConstants.MESSAGE_TYPE_IMAGE:
                        if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                            return RIGHT_IMAGE_MESSAGE;
                        } else {
                            return LEFT_IMAGE_MESSAGE;
                        }
                    case CometChatConstants.MESSAGE_TYPE_VIDEO:
                        if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                            return RIGHT_VIDEO_MESSAGE;
                        } else {
                            return LEFT_VIDEO_MESSAGE;
                        }
                    case CometChatConstants.MESSAGE_TYPE_FILE:
                        if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                            return RIGHT_FILE_MESSAGE;
                        } else {
                            return LEFT_FILE_MESSAGE;
                        }
                    default:
                        return -1;
                }
            } else {
                if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_ACTION)) {
                    return ACTION_MESSAGE;
                } else if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_CALL)) {
                    return CALL_MESSAGE;
                } else if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_CUSTOM)){
                    if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                        if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.LOCATION))
                            return RIGHT_LOCATION_CUSTOM_MESSAGE;
                        else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.POLLS))
                            return RIGHT_POLLS_CUSTOM_MESSAGE;
                        else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.STICKERS))
                            return RIGHT_STICKER_MESSAGE;
                        else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.WHITEBOARD))
                            return RIGHT_WHITEBOARD_MESSAGE;
                        else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.WRITEBOARD))
                            return RIGHT_WRITEBOARD_MESSAGE;
                        else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.GROUP_CALL))
                            return RIGHT_GROUP_CALL_MESSAGE;
                        else
                            return RIGHT_CUSTOM_MESSAGE;
                    }
                    else
                        if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.LOCATION))
                            return LEFT_LOCATION_CUSTOM_MESSAGE;
                        else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.POLLS))
                            return LEFT_POLLS_CUSTOM_MESSAGE;
                        else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.STICKERS))
                            return LEFT_STICKER_MESSAGE;
                        else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.WHITEBOARD))
                            return LEFT_WHITEBOARD_MESSAGE;
                        else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.WRITEBOARD))
                            return LEFT_WRITEBOARD_MESSAGE;
                        else if (baseMessage.getType().equalsIgnoreCase(UIKitConstants.IntentStrings.GROUP_CALL))
                            return LEFT_GROUP_CALL_MESSAGE;
                        else
                            return LEFT_CUSTOM_MESSAGE;
                }
            }
        }
        else
        {
            if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                return RIGHT_DELETE_MESSAGE;
            } else {
                return LEFT_DELETE_MESSAGE;
            }
        }
        return -1;

    }

    /**
     * This method is used to update message list of adapter.
     *
     * @param baseMessageList is list of baseMessages.
     *
     */
    public void updateList(List<BaseMessage> baseMessageList) {
        setMessageList(baseMessageList);
    }

    /**
     * This method is used to set real time delivery receipt of particular message in messageList
     * of adapter by updating message.
     *
     * @param messageReceipt is a object of MessageReceipt.
     * @see MessageReceipt
     */
    public void setDeliveryReceipts(MessageReceipt messageReceipt) {

        for (int i = messageList.size() - 1; i >= 0; i--) {
            BaseMessage baseMessage = messageList.get(i);
            if (baseMessage.getDeliveredAt() == 0) {
                int index = messageList.indexOf(baseMessage);
                messageList.get(index).setDeliveredAt(messageReceipt.getDeliveredAt());
            }
        }
        notifyDataSetChanged();
    }

    /**
     * This method is used to set real time read receipt of particular message in messageList
     * of adapter by updating message.
     *
     * @param messageReceipt is a object of MessageReceipt.
     * @see MessageReceipt
     */
    public void setReadReceipts(MessageReceipt messageReceipt) {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            BaseMessage baseMessage = messageList.get(i);
            if (baseMessage.getReadAt() == 0) {
                int index = messageList.indexOf(baseMessage);
                messageList.get(index).setReadAt(messageReceipt.getReadAt());
            }
        }

        notifyDataSetChanged();
    }

    /**
     * This method is used to add message in messageList when send by a user or when received in
     * real time.
     *
     * @param baseMessage is a object of BaseMessage. It is new message which will added.
     * @see BaseMessage
     *
     */
    public void addMessage(BaseMessage baseMessage) {
//        if (!messageList.contains(baseMessage)) {
            messageList.add(baseMessage);
            selectedItemList.clear();
//        }
        notifyItemInserted(messageList.size()-1);
    }

    /**
     * THis method is used to update the old message with the new message
     * @param baseMessage
     */
    public void updateChangedMessage(BaseMessage baseMessage) {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            String muid = messageList.get(i).getMuid();
            if (muid!=null && muid.equals(baseMessage.getMuid())) {
                messageList.remove(i);
                messageList.add(i,baseMessage);
                notifyItemChanged(i);
            }
        }
    }
    /**
     * This method is used to update previous message with new message in messageList of adapter.
     *
     * @param baseMessage is a object of BaseMessage. It is new message which will be updated.
     */
    public void setUpdatedMessage(BaseMessage baseMessage) {

        if (messageList.contains(baseMessage)) {
            int index = messageList.indexOf(baseMessage);
            messageList.remove(baseMessage);
            messageList.add(index, baseMessage);
            notifyItemChanged(index);
        }
    }

    public void remove(BaseMessage baseMessage) {
        int index = messageList.indexOf(baseMessage);
        messageList.remove(baseMessage);
        notifyItemRemoved(index);
    }
    public void resetList() {
        messageList.clear();
        notifyDataSetChanged();
    }

    public void clearLongClickSelectedItem() {
        isLongClickEnabled = false;
        isTextMessageClick = false;
        isImageMessageClick = false;
        isLocationMessageClick = false;
        longselectedItemList.clear();
        notifyDataSetChanged();
    }

    public BaseMessage getLastMessage() {
        if (messageList.size()>0)
        {
            Log.e(TAG, "getLastMessage: "+messageList.get(messageList.size()-1 ));
            return messageList.get(messageList.size()-1);
        }
        else
            return null;
    }

    public int getPosition(BaseMessage baseMessage){
        return messageList.indexOf(baseMessage);
    }

    public void updateReplyCount(int parentMessageId) {
        for(BaseMessage baseMessage : messageList) {
            if (baseMessage.getId()==parentMessageId) {
                int replyCount = baseMessage.getReplyCount();
                baseMessage.setReplyCount(++replyCount);
                notifyItemChanged(messageList.indexOf(baseMessage));
            }
        }
    }

    class ImageMessageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private CardView cardView;
        private ProgressBar progressBar;
        private CometChatAvatar ivUser;
        public TextView txtTime,tvUser;
        private RelativeLayout rlMessageBubble;
        private TextView tvThreadReplyCount;

        private RelativeLayout sensitiveLayout;
        private ChipGroup reactionLayout;

        public ImageMessageViewHolder(@NonNull View view) {
            super(view);
            int type = (int) view.getTag();
            imageView = view.findViewById(R.id.go_img_message);
            tvUser= view.findViewById(R.id.tv_user);
            cardView = view.findViewById(R.id.cv_image_message_container);
            progressBar = view.findViewById(R.id.progress_bar);
            txtTime = view.findViewById(R.id.txt_time);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            tvThreadReplyCount = view.findViewById(R.id.thread_reply_count);
            sensitiveLayout = view.findViewById(R.id.sensitive_layout);
            reactionLayout = view.findViewById(R.id.reactions_layout);
            progressBar = view.findViewById(R.id.progress_bar);
        }
    }

    public class ActionMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public ActionMessageViewHolder(@NonNull View view) {
            super(view);
            int type = (int) view.getTag();
            textView = view.findViewById(R.id.go_txt_message);
        }
    }

    class VideoMessageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private ImageView playBtn;
        private CardView cardView;
        private ProgressBar progressBar;
        private CometChatAvatar ivUser;
        public TextView txtTime,tvUser;
        private RelativeLayout rlMessageBubble;
        private TextView tvThreadReplyCount;
        private ChipGroup reactionLayout;

        public VideoMessageViewHolder(@NonNull View view) {
            super(view);
            int type = (int) view.getTag();
            imageView = view.findViewById(R.id.go_video_message);
            playBtn = view.findViewById(R.id.playBtn);
            tvUser= view.findViewById(R.id.tv_user);
            cardView = view.findViewById(R.id.cv_image_message_container);
            progressBar = view.findViewById(R.id.progress_bar);
            txtTime = view.findViewById(R.id.txt_time);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            tvThreadReplyCount = view.findViewById(R.id.thread_reply_count);
            reactionLayout = view.findViewById(R.id.reactions_layout);
        }
    }

    public class FileMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView fileName;
        private TextView fileExt;
        public TextView txtTime;
        private TextView fileSize;
        private TextView tvUser;
        private View view;
        private CometChatAvatar ivUser;
        private RelativeLayout cvMessageView;
        private RelativeLayout rlMessageBubble;
        private TextView tvThreadReplyCount;
        private ChipGroup reactionLayout;
        private ProgressBar progressBar;

        FileMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            fileSize = itemView.findViewById(R.id.tvFileSize);
            ivUser = itemView.findViewById(R.id.iv_user);
            tvUser = itemView.findViewById(R.id.tv_user);
            fileExt = itemView.findViewById(R.id.tvFileExtension);
            txtTime = itemView.findViewById(R.id.txt_time);
            fileName = itemView.findViewById(R.id.tvFileName);
            cvMessageView = itemView.findViewById(R.id.cv_message_container);
            rlMessageBubble = itemView.findViewById(R.id.rl_message);
            tvThreadReplyCount = itemView.findViewById(R.id.thread_reply_count);
            reactionLayout = itemView.findViewById(R.id.reactions_layout);
            progressBar = itemView.findViewById(R.id.progress_bar);
            this.view = itemView;
        }
    }

    public class DeleteMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView txtMessage;
        private RelativeLayout cardView;
        private View view;
        public TextView txtTime;
        public TextView tvUser;
        private ImageView imgStatus;
        private int type;
        private CometChatAvatar ivUser;
        private RelativeLayout rlMessageBubble;
        private TextView tvThreadReplyCount;
        private LinearLayout lvReplyAvatar;

        DeleteMessageViewHolder(@NonNull View view) {
            super(view);
            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            txtMessage = view.findViewById(R.id.go_txt_message);
            cardView = view.findViewById(R.id.cv_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            imgStatus = view.findViewById(R.id.img_pending);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            tvThreadReplyCount = view.findViewById(R.id.thread_reply_count);
            this.view = view;
        }
    }

    /**
     * This is TextMessageViewHolder which is used to handle TextMessage.
     */
    public class TextMessageViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rlMessageBubble;
        private RelativeLayout cvMessageView;
        private View view;
        private TextView txtMessage;            //Text Message
        private TextView tvThreadReplyCount;    //Thread Reply Count
        public TextView txtTime;                //Message Sent time.
        private ImageView imgStatus;

        private int type;
        private CometChatAvatar ivUser;                  //sender avatar
        public TextView tvUser;                 //sender name

        private RelativeLayout sentimentVw;     //sentiment extension layout
        private TextView viewSentimentMessage;  //sentiment extension text

        private MaterialCardView replyLayout;     //reply message layout
        private TextView replyUser;             //reply message sender name
        private TextView replyMessage;          //reply message text
        private ImageView replyMessageImage;

        private ChipGroup reactionLayout;

        private ProgressBar progressBar;        //ProgressBar to show message status;

        TextMessageViewHolder(@NonNull View view) {
            super(view);

            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            txtMessage = view.findViewById(R.id.go_txt_message);
            cvMessageView = view.findViewById(R.id.cv_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            imgStatus = view.findViewById(R.id.img_pending);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            replyLayout = view.findViewById(R.id.replyLayout);
            replyUser = view.findViewById(R.id.reply_user);
            replyMessageImage = view.findViewById(R.id.reply_image);
            replyMessage = view.findViewById(R.id.reply_message);
            tvThreadReplyCount = view.findViewById(R.id.thread_reply_count);
            sentimentVw = view.findViewById(R.id.sentiment_layout);
            viewSentimentMessage = view.findViewById(R.id.view_sentiment);
            reactionLayout = view.findViewById(R.id.reactions_layout);
            progressBar = view.findViewById(R.id.progress_bar);
            this.view = view;

        }
    }

    public class CustomMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView txtMessage;
        private RelativeLayout cardView;
        private View view;
        public TextView txtTime;
        public TextView tvUser;
        private ImageView imgStatus;
        private int type;
        private CometChatAvatar ivUser;
        private RelativeLayout rlMessageBubble;


        CustomMessageViewHolder(@NonNull View view) {
            super(view);

            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            txtMessage = view.findViewById(R.id.go_txt_message);
            cardView = view.findViewById(R.id.cv_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            imgStatus = view.findViewById(R.id.img_pending);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            this.view = view;
        }
    }

    public class StickerMessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView stickerView;
        public TextView tvThreadReplyCount;
        public TextView txtTime;
        public TextView tvUser;
        public CometChatAvatar ivUser;
        private ChipGroup reactionLayout;

        public int type;
        public View view;

        public StickerMessageViewHolder(View itemView) {
            super(itemView);

            type =  (int) itemView.getTag();

            stickerView = itemView.findViewById(R.id.sticker_view);
            tvUser = itemView.findViewById(R.id.tv_user);
            ivUser = itemView.findViewById(R.id.iv_user);
            tvThreadReplyCount = itemView.findViewById(R.id.thread_reply_count);
            txtTime = itemView.findViewById(R.id.txt_time);
            reactionLayout = itemView.findViewById(R.id.reactions_layout);
            this.view = itemView;

        }
    }
    public class LocationMessageViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout rlMessageBubble;
        private View view;
        private int type;

        public ImageView ivMap;
        public TextView tvAddress;
        public TextView senderTxt;

        public MaterialCardView cvMessageView;

        public TextView tvThreadReplyCount;
        public TextView txtTime;
        public TextView tvUser;
        public CometChatAvatar ivUser;

        private ChipGroup reactionLayout;

        public LocationMessageViewHolder(View itemView) {
            super(itemView);

            type = (int) itemView.getTag();

            ivMap = itemView.findViewById(R.id.iv_map);
            tvAddress = itemView.findViewById(R.id.tv_place_name);
            txtTime = itemView.findViewById(R.id.txt_time);
            cvMessageView = itemView.findViewById(R.id.cv_message_container);
            rlMessageBubble = itemView.findViewById(R.id.rl_message);
            tvUser = itemView.findViewById(R.id.tv_user);
            ivUser = itemView.findViewById(R.id.iv_user);
            tvThreadReplyCount = itemView.findViewById(R.id.thread_reply_count);
            senderTxt = itemView.findViewById(R.id.sender_location_txt);
            reactionLayout = itemView.findViewById(R.id.reactions_layout);
            this.view = itemView;
        }
    }

    public class PollMessageViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout rlMessageBubble;
        private View view;
        private int type;

        public TextView tvQuestion;
        public TextView totalCount;
        public LinearLayout optionGroup;
        public ProgressBar loadingProgress;

        public LinearLayout cvMessageView;
        public TextView tvThreadReplyCount;
        public TextView txtTime;
        public TextView tvUser;
        public CometChatAvatar ivUser;

        private ChipGroup reactionLayout;

        public PollMessageViewHolder(View itemView) {
            super(itemView);

            type = (int) itemView.getTag();

            totalCount = itemView.findViewById(R.id.total_votes);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            optionGroup = itemView.findViewById(R.id.options_group);
            txtTime = itemView.findViewById(R.id.txt_time);
            rlMessageBubble = itemView.findViewById(R.id.rl_message);
            cvMessageView = itemView.findViewById(R.id.cv_message_container);
            tvUser = itemView.findViewById(R.id.tv_user);
            ivUser = itemView.findViewById(R.id.iv_user);
            loadingProgress = itemView.findViewById(R.id.loading_progressBar);
            tvThreadReplyCount = itemView.findViewById(R.id.thread_reply_count);
            reactionLayout = itemView.findViewById(R.id.reactions_layout);
            this.view = itemView;
        }
    }


    public class GroupCallMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView tvThreadReplyCount;
        private RelativeLayout messageContainer;
        private View view;
        public TextView txtTime;
        public TextView tvUser;
        private int type;
        private CometChatAvatar ivUser;
        private RelativeLayout rlMessageBubble;
        private ChipGroup reactionLayout;

        private ImageView icon;
        private TextView groupCallMessage;
        private MaterialButton joinBtn;

        GroupCallMessageViewHolder(@NonNull View view) {
            super(view);

            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            icon = view.findViewById(R.id.icon);
            messageContainer = view.findViewById(R.id.cv_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            tvThreadReplyCount = view.findViewById(R.id.thread_reply_count);
            reactionLayout = view.findViewById(R.id.reactions_layout);
            groupCallMessage = view.findViewById(R.id.group_call_message);
            joinBtn = view.findViewById(R.id.join_call);
            this.view = view;

        }
    }

    public class WhiteBoardViewHolder extends RecyclerView.ViewHolder {

        private TextView tvThreadReplyCount;
        private RelativeLayout messageContainer;
        private View view;
        public TextView txtTime;
        public TextView tvUser;
        private int type;
        private CometChatAvatar ivUser;
        private RelativeLayout rlMessageBubble;
        private ChipGroup reactionLayout;

        private TextView whiteBoardMessage;
        private MaterialButton joinBtn;

        WhiteBoardViewHolder(@NonNull View view) {
            super(view);

            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            messageContainer = view.findViewById(R.id.cv_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            tvThreadReplyCount = view.findViewById(R.id.thread_reply_count);
            reactionLayout = view.findViewById(R.id.reactions_layout);
            whiteBoardMessage = view.findViewById(R.id.whiteboard_message);
            joinBtn = view.findViewById(R.id.join_whiteboard);
            this.view = view;

        }
    }


    public class WriteBoardViewHolder extends RecyclerView.ViewHolder {

        private TextView tvThreadReplyCount;
        private RelativeLayout messageContainer;
        private View view;
        public TextView txtTime;
        public TextView tvUser;
        private int type;
        private CometChatAvatar ivUser;
        private RelativeLayout rlMessageBubble;
        private ChipGroup reactionLayout;

        private TextView whiteBoardMessage;
        private MaterialButton joinBtn;

        WriteBoardViewHolder(@NonNull View view) {
            super(view);

            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            messageContainer = view.findViewById(R.id.cv_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            tvThreadReplyCount = view.findViewById(R.id.thread_reply_count);
            reactionLayout = view.findViewById(R.id.reactions_layout);
            whiteBoardMessage = view.findViewById(R.id.writeboard_message);
            joinBtn = view.findViewById(R.id.join_whiteboard);
            this.view = view;

        }
    }


    public class AudioMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView length;
        private ImageView playBtn;
        private int type;
        private TextView tvUser;
        private CometChatAvatar ivUser;
        private RelativeLayout rlMessageBubble;
        private TextView txtTime;
        private TextView tvThreadReplyCount;
        private ChipGroup reactionLayout;
        private ProgressBar progressBar;
        public AudioMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            type = (int)itemView.getTag();
            length = itemView.findViewById(R.id.audiolength_tv);
            playBtn = itemView.findViewById(R.id.playBtn);
            rlMessageBubble = itemView.findViewById(R.id.cv_message_container);
            tvUser = itemView.findViewById(R.id.tv_user);
            ivUser = itemView.findViewById(R.id.iv_user);
            txtTime = itemView.findViewById(R.id.txt_time);
            tvThreadReplyCount = itemView.findViewById(R.id.thread_reply_count);
            reactionLayout = itemView.findViewById(R.id.reactions_layout);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
    public class LinkMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView linkTitle;
        private TextView linkVisit;
        private TextView linkSubtitle;
        private TextView message;
        private ImageView videoLink;
        private CardView cvMessageView;
        private View view;
        public TextView txtTime;
        private ImageView imgStatus;
        private ImageView linkImg;
        private int type;
        private TextView tvUser;
        private CometChatAvatar ivUser;
        private RelativeLayout rlMessageBubble;
        private TextView tvThreadReplyCount;

        private ChipGroup reactionLayout;

        private ProgressBar progressBar;

        LinkMessageViewHolder(@NonNull View view) {
            super(view);

            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            linkTitle = view.findViewById(R.id.link_title);
            linkSubtitle = view.findViewById(R.id.link_subtitle);
            linkVisit = view.findViewById(R.id.visitLink);
            linkImg = view.findViewById(R.id.link_img);
            message = view.findViewById(R.id.message);
            videoLink = view.findViewById(R.id.videoLink);
            cvMessageView = view.findViewById(R.id.cv_link_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            imgStatus = view.findViewById(R.id.img_pending);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            tvThreadReplyCount = view.findViewById(R.id.thread_reply_count);
            reactionLayout = view.findViewById(R.id.reactions_layout);
            progressBar = view.findViewById(R.id.progress_bar);
            this.view = view;
        }
    }

    public class DateItemHolder extends RecyclerView.ViewHolder {

        TextView txtMessageDate;

        DateItemHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageDate = itemView.findViewById(R.id.txt_message_date);
        }
    }

    public interface OnMessageLongClick
    {
        void setLongMessageClick(List<BaseMessage> baseMessage);
    }


}



