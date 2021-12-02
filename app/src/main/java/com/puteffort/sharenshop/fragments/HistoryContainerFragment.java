package com.puteffort.sharenshop.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.interfaces.DualPanePostCommunicator;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.UserProfile;

public class HistoryContainerFragment extends Fragment implements DualPanePostCommunicator {
    private boolean isDualPaneSystem;

    public HistoryContainerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_container, container, false);
        isDualPaneSystem = view.findViewById(R.id.postFragment) != null;

        return view;
    }

    @Override
    public void openPostFragment(PostInfo postInfo, Drawable ownerImage) {
        PostFragment postFragment = new PostFragment(postInfo, ownerImage);
        if (isDualPaneSystem) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.postFragment, postFragment)
                    .commit();
        } else {
            ((MainActivity)requireActivity()).changeFragment(postFragment);
        }
    }

    @Override
    public void openPostFragment(String postID) {
        PostFragment postFragment = new PostFragment(postID);
        if (isDualPaneSystem) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.postFragment, postFragment)
                    .commit();
        } else {
            ((MainActivity)requireActivity()).changeFragment(postFragment);
        }
    }

    @Override
    public void openUserFragment(String userID) {
        MyProfileFragment userFragment = new MyProfileFragment(userID);
        if (isDualPaneSystem) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.postFragment, userFragment).commit();
        } else {
            ((MainActivity)requireActivity()).changeFragment(userFragment);
        }
    }

    @Override
    public void openUserFragment(UserProfile userProfile) {
        MyProfileFragment userFragment = new MyProfileFragment(userProfile);
        if (isDualPaneSystem) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.postFragment, userFragment)
                    .commit();
        } else {
            ((MainActivity)requireActivity()).changeFragment(userFragment);
        }
    }
}