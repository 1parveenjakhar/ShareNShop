package com.puteffort.sharenshop.interfaces;

import android.graphics.drawable.Drawable;

import com.puteffort.sharenshop.models.PostInfo;

public interface DualPanePostCommunicator {
    void openPostFragment(PostInfo postInfo, Drawable ownerImage);
    void openPostFragment(String postID);
}