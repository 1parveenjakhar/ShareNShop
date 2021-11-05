package com.puteffort.sharenshop.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentPostBinding;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.viewmodels.HomeFragmentViewModel;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel;

public class PostFragment extends Fragment {
    private PostInfo postInfo;
    public static String POST_ID = "PostID";

    private FragmentPostBinding binding;
    private PostFragmentViewModel model;

    public PostFragment() {
        // Required empty public constructor
    }

    public PostFragment(PostInfo postInfo) {
        this.postInfo = postInfo;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post, container, false);
        model = new ViewModelProvider(requireActivity()).get(PostFragmentViewModel.class);

        return binding.getRoot();
    }
}