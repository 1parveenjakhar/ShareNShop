package com.puteffort.sharenshop.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.interfaces.DualPanePostCommunicator;
import com.puteffort.sharenshop.models.PostInfo;

public class NotificationContainerFragment extends Fragment implements DualPanePostCommunicator {
    private boolean isDualPaneSystem;
    private String currentlyOpenedPost;

    public NotificationContainerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_container, container, false);
        isDualPaneSystem = view.findViewById(R.id.postFragment) != null;

        return view;
    }

    @Override
    public void openPostFragment(PostInfo postInfo, Drawable ownerImage) {
        PostFragment postFragment = new PostFragment(postInfo, ownerImage);
        if (isDualPaneSystem) {
            if (currentlyOpenedPost != null
                    && currentlyOpenedPost.equals(postInfo.getId()))
                return; // no need to open fragment in that case

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.postFragment, postFragment)
                    .commit();
        } else {
            ((MainActivity)requireActivity()).changeFragment(postFragment);
        }
        currentlyOpenedPost = postInfo.getId();
    }

    @Override
    public void openPostFragment(String postID) {
        PostFragment postFragment = new PostFragment(postID);
        if (isDualPaneSystem) {
            if (currentlyOpenedPost != null
                    && currentlyOpenedPost.equals(postID))
                return;

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.postFragment, postFragment)
                    .commit();
        } else {
            ((MainActivity)requireActivity()).changeFragment(postFragment);
        }
        currentlyOpenedPost = postID;
    }
}