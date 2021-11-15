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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.adapters.PostsListRecyclerViewAdapter;
import com.puteffort.sharenshop.databinding.FragmentHomeBinding;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.utils.UITasks;
import com.puteffort.sharenshop.viewmodels.HomeFragmentViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment implements PostsListRecyclerViewAdapter.ItemClickListener {
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

        setUpComponents();
        addObservers();

        return binding.getRoot();
    }

    private void setUpComponents() {
        binding.searchView.setOnQueryTextListener(model);

        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.postsRecyclerView.setHasFixedSize(true);
        recyclerViewAdapter = new PostsListRecyclerViewAdapter(requireContext(),
                model.getPosts().getValue(),
                model.getWishListedPosts(),
                model.getPostsStatus());
        recyclerViewAdapter.setClickListener(this);
        binding.postsRecyclerView.setAdapter(recyclerViewAdapter);
        binding.swipeRefreshPostList.setOnRefreshListener(DBOperations::getUserDetails);
        binding.swipeRefreshPostList.setRefreshing(true);
        binding.filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterDialogFragment newFragment = new FilterDialogFragment(model.getCheckedFilter());
                newFragment.show(getActivity().getSupportFragmentManager(), "filter_dialog");
                newFragment.setOnFilterClick(new FilterDialogFragment.OnFilterClick(){
                    @Override
                    public void onFilterClicked(ArrayList<Integer> checked, ArrayList<Integer> type) {
                        model.getFilteredPosts(checked, type);
                    }
                });
            }
        });

        binding.sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SortDialogFragment newFragment = new SortDialogFragment(model.getCheckedSort());
                newFragment.show(getActivity().getSupportFragmentManager(), "sort_dialog");
                newFragment.setOnSortClick(new SortDialogFragment.OnSortClick(){
                    @Override
                    public void onSortClicked(String sortBy) {
                        model.sortPosts(sortBy);
                    }
                });
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addObservers() {
        model.getDataAdded().observe(requireActivity(), index -> recyclerViewAdapter.notifyItemInserted(index));
        model.getDataChanged().observe(requireActivity(), index -> recyclerViewAdapter.notifyItemChanged(index));
        model.getDataRemoved().observe(requireActivity(), index -> recyclerViewAdapter.notifyItemRemoved(index));

        model.getPosts().observe(requireActivity(), unused -> recyclerViewAdapter.notifyDataSetChanged());
        model.areUserDetailsChanged().observe(requireActivity(), detailsChanged -> {
            if (detailsChanged) {
                recyclerViewAdapter.notifyDataSetChanged();
            }
        });

        model.getToastMessage().observe(requireActivity(), toastID -> UITasks.showToast(requireContext(), requireContext().getString(toastID)));

        model.isDataUpdating().observe(requireActivity(), dataUpdating -> binding.progressBar.setVisibility(dataUpdating ? View.VISIBLE : View.INVISIBLE));

        DBOperations.getUserActivity().observe(requireActivity(), userActivity -> {
            System.out.println("Live data change observed .......................");
            binding.swipeRefreshPostList.setRefreshing(false);
            model.changeUserDetails(userActivity);
        });
    }

    @Override
    public void onItemClick(View view, int position, Drawable ownerImage) {
        // Handling a particular post click.
        PostInfo post = Objects.requireNonNull(model.getPosts().getValue()).get(position);
        ((MainActivity)requireActivity()).changeFragment(new PostFragment(post, ownerImage));
    }

    @Override
    public void changeFavourite(int position, boolean isFavourite) {
        model.changePostFavourite(position, isFavourite);
    }

    @Override
    public void changeStatus(int position, String status) {
        model.changeStatus(position, status);
    }
}