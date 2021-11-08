package com.puteffort.sharenshop.fragments;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentPostBinding;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel;

import java.util.Objects;

public class PostFragment extends Fragment implements TabLayout.OnTabSelectedListener {
    private static PostInfo postInfo;

    private FragmentPostBinding binding;
    private PostFragmentViewModel model;
    private Drawable ownerImage;

    public PostFragment() {
        // Required empty public constructor
    }

    public PostFragment(PostInfo postInfo, Drawable ownerImage) {
        this.ownerImage = ownerImage;
        PostFragment.postInfo = postInfo;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post, container, false);
        model = new ViewModelProvider(this, new PostFragmentViewModelFactory(postInfo)).get(PostFragmentViewModel.class);

        setPostInfo();
        setListeners();
        setObservers();

        return binding.getRoot();
    }

    @SuppressLint("DefaultLocale")
    private void setPostInfo() {
        binding.postTitle.setText(postInfo.getTitle());
        binding.postAmount.setText(String.format("Rs. %s", postInfo.getAmount()));
        binding.postPeople.setText(String.format("%d\nPeople", postInfo.getPeopleRequired()));
        StringBuilder time = new StringBuilder();
        if (postInfo.getYears() != 0) time.append(postInfo.getYears()).append("Y ");
        if (postInfo.getMonths() != 0) time.append(postInfo.getMonths()).append("M ");
        if (postInfo.getDays() != 0) time.append(postInfo.getDays()).append("D ");
        binding.postTime.setText(time.toString().trim());
        binding.imageView.setImageDrawable(ownerImage);
    }

    private void setListeners() {
        binding.tabLayout.addOnTabSelectedListener(this);
        Objects.requireNonNull(binding.tabLayout.getTabAt(model.getPreviousTabIndex())).select();
    }

    private void setObservers() {
        model.getSelectedTab().observe(getViewLifecycleOwner(), tab -> {
            FragmentActivity activity = getActivity();
            if (isAdded() && activity != null) {
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.recyclerViewFrameLayout, tab)
                        .commit();
            }
        });

        model.getPostDescription().observe(getViewLifecycleOwner(), description -> {
            if (description == null) {
                binding.postDescription.setText(getString(R.string.none));
                return;
            }
            binding.postDescription.setText(description);
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        model.setSelectedTab(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    static class PostFragmentViewModelFactory extends ViewModelProvider.NewInstanceFactory {
        private final PostInfo postInfo;

        public PostFragmentViewModelFactory(PostInfo postInfo) {
            this.postInfo = postInfo;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new PostFragmentViewModel(postInfo);
        }
    }
}