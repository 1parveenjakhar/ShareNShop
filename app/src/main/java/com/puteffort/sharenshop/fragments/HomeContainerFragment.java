package com.puteffort.sharenshop.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.interfaces.DualPanePostCommunicator;

public class HomeContainerFragment extends Fragment implements DualPanePostCommunicator {
    private boolean isDualPaneSystem;
    private String postID;

    public HomeContainerFragment() {
        // Required empty public constructor
    }

    public HomeContainerFragment(String postID) {
        this.postID = postID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_container, container, false);
        isDualPaneSystem = view.findViewById(R.id.postFragment) != null;

        if (postID != null) {
            openPostFragment(new PostFragment(postID));
        }

        return view;
    }

    @Override
    public void openPostFragment(PostFragment postFragment) {
        if (isDualPaneSystem) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.postFragment, postFragment)
                    .commit();
        } else {
            ((MainActivity)requireActivity()).changeFragment(postFragment);
        }
    }
}