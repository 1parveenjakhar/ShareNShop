package com.puteffort.sharenshop.interfaces;

import android.graphics.drawable.Drawable;

import com.puteffort.sharenshop.fragments.PostFragment;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.UserProfile;

public interface DualPanePostCommunicator {
    void openPostFragment(PostInfo postInfo, Drawable ownerImage);
    void openPostFragment(String postID);
    void openUserFragment(String userID);
    // This call will be made from PostFragment only, hence need to add to backstack
    void openUserFragment(UserProfile userProfile);
}