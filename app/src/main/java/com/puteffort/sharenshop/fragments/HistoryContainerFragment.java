package com.puteffort.sharenshop.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;

public class HistoryContainerFragment extends Fragment implements HomeFragment.PostCommunicator {
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
    public void addPostFragment(PostFragment postFragment) {
        if (isDualPaneSystem) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.postFragment, postFragment)
                    .commit();
        } else {
            ((MainActivity)requireActivity()).changeFragment(postFragment);
        }
    }
}