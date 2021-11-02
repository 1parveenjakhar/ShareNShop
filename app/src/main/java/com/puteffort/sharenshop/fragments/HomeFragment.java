package com.puteffort.sharenshop.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.adapters.PostsListRecyclerViewAdapter;
import com.puteffort.sharenshop.databinding.FragmentHomeBinding;
import com.puteffort.sharenshop.viewmodels.HomeFragmentViewModel;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeFragmentViewModel model;
    private PostsListRecyclerViewAdapter recyclerViewAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        model = new ViewModelProvider(requireActivity()).get(HomeFragmentViewModel.class);

        binding.searchView.setOnQueryTextListener(model);
        binding.postsRecyclerView.setHasFixedSize(true);
        addObservers();

        return binding.getRoot();
    }

    private void addObservers() {
        model.getPostsInfoLiveData().observe(requireActivity(), postsInfo -> {
            binding.progressBar.setVisibility(View.INVISIBLE);
            recyclerViewAdapter = new PostsListRecyclerViewAdapter(getContext(), postsInfo);
            binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.postsRecyclerView.setAdapter(recyclerViewAdapter);
        });
    }
}