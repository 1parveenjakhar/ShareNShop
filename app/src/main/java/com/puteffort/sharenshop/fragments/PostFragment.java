package com.puteffort.sharenshop.fragments;

import static com.puteffort.sharenshop.utils.UtilFunctions.getFormattedTime;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentPostBinding;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel;

public class PostFragment extends Fragment {
    private PostInfo postInfo;

    private FragmentPostBinding binding;
    private PostFragmentViewModel model;
    private Drawable ownerImage;

    private String postID;

    public PostFragment() {
        // Required empty public constructor
    }

    public PostFragment(PostInfo postInfo, Drawable ownerImage) {
        this.ownerImage = ownerImage;
        this.postInfo = postInfo;
    }

    public PostFragment(String postID) {
        this.postID = postID;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post, container, false);
        model = new ViewModelProvider(this).get(PostFragmentViewModel.class);

        if (postInfo != null) model.setPostInfo(postInfo, ownerImage);
        else if (postID != null) model.setPostInfo(postID);

        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        setListeners();
        setObservers();

        return binding.getRoot();
    }

    @SuppressLint("DefaultLocale")
    private void setPostInfo() {
        binding.postTitle.setText(postInfo.getTitle());
        binding.postAmount.setText(String.format("Rs. %s", postInfo.getAmount()));
        binding.postPeople.setText(String.format("%d\nPeople", postInfo.getPeopleRequired()));
        binding.postTime.setText(getFormattedTime(postInfo.getYears(), postInfo.getMonths(), postInfo.getDays()));
        binding.swipeRefreshPost.setRefreshing(false);
    }

    private void setListeners() {
        binding.swipeRefreshPost.setOnRefreshListener(() -> {
            model.loadPostInfo(this.postInfo.getId());
            model.loadPostDetailInfo(this.postInfo.getId());
        });

        binding.viewPager.setAdapter(new ViewPagerAdapter(this));
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(model.getFragmentTitle(position))).attach();
        binding.viewPager.setCurrentItem(1);
    }

    private void setObservers() {
        model.getPostDescription().observe(getViewLifecycleOwner(), description -> {
            if (description == null || description.isEmpty()) {
                binding.postDescription.setVisibility(View.GONE);
                return;
            }
            binding.postDescription.setVisibility(View.VISIBLE);
            binding.postDescription.setText(description);
        });

        model.getPostInfo().observe(getViewLifecycleOwner(), postInfo -> {
            if (postInfo != null) {
                this.postInfo = postInfo;
                setPostInfo();
            }
        });

        model.getOwnerImage().observe(getViewLifecycleOwner(), ownerImage -> {
            if (ownerImage == null) return;
            binding.imageView.setImageDrawable(ownerImage);
        });
        model.getOwnerImageURL().observe(getViewLifecycleOwner(), imageURL -> {
            if (imageURL == null) return;
            Glide.with(requireContext()).load(imageURL)
                    .error(R.drawable.default_person_icon)
                    .circleCrop().into(binding.imageView);
        });
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(Fragment fragment){
            super(fragment);
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return model.getFragment(position);
        }
    }
}