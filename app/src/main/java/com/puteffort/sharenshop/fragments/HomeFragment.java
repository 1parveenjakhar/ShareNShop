package com.puteffort.sharenshop.fragments;

import static android.view.View.GONE;
import static com.puteffort.sharenshop.utils.DBOperations.INTERESTED;
import static com.puteffort.sharenshop.utils.DBOperations.USER_PROFILE;
import static com.puteffort.sharenshop.utils.UtilFunctions.getFormattedTime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentHomeBinding;
import com.puteffort.sharenshop.interfaces.DualPanePostCommunicator;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.viewmodels.HomeFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeFragmentViewModel model;
    private HomeRecyclerViewAdapter recyclerViewAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        model = new ViewModelProvider(requireActivity()).get(HomeFragmentViewModel.class);

        View view = binding.getRoot();
        // Setting width of searchView only after layout has been inflated
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> binding.searchView.setMaxWidth(view.getWidth() - 70));

        binding.progressBar.setVisibility(GONE);

        setUpComponents();
        addObservers();

        return view;
    }

    private void setUpComponents() {
        binding.searchView.setOnQueryTextListener(model);
        binding.searchView.setOnSearchClickListener(v -> binding.appNameHeading.setVisibility(View.INVISIBLE));
        binding.searchView.setOnCloseListener(() -> {
            binding.appNameHeading.setVisibility(View.VISIBLE);
            return false;
        });

        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.postsRecyclerView.setHasFixedSize(true);
        recyclerViewAdapter = new HomeRecyclerViewAdapter(requireContext(), this);

        binding.postsRecyclerView.setAdapter(recyclerViewAdapter);
        binding.swipeRefreshPostList.setOnRefreshListener(DBOperations::getUserDetails);
        binding.swipeRefreshPostList.setRefreshing(true);

        binding.filterButton.setOnClickListener(v -> {
            FilterDialogFragment newFragment = new FilterDialogFragment(model.getLastActivityChips(), model.getEditTexts());
            newFragment.show(getChildFragmentManager(), "filter_dialog");
            newFragment.setOnFilterClick((lastActivityChips, fromAndTos) -> model.filterPosts(lastActivityChips, fromAndTos));
        });

        binding.sortButton.setOnClickListener(v -> {
            SortDialogFragment newFragment = new SortDialogFragment(model.getCheckedSort());
            newFragment.show(getChildFragmentManager(), "sort_dialog");
            newFragment.setOnSortClick(sortBy -> model.sortPosts(sortBy));
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addObservers() {
        model.isDataUpdating().observe(getViewLifecycleOwner(),
                dataUpdating -> binding.progressBar.setVisibility(dataUpdating ? View.VISIBLE : GONE));

        model.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts == null) return;
            binding.swipeRefreshPostList.setRefreshing(false);
            recyclerViewAdapter.setPosts(posts);
        });
    }

    private void openPostFragment(int position, Drawable postOwnerImage) {
        ((DualPanePostCommunicator)requireParentFragment()).openPostFragment(recyclerViewAdapter.getPost(position), postOwnerImage);
    }

    private void openUserFragment(int position) {
        ((DualPanePostCommunicator)requireParentFragment()).openUserFragment(recyclerViewAdapter.getPost(position).getOwnerID());
    }

    private void changeFavourite(int position, ImageView favorite, boolean isFavourite, ProgressBar progressBar) {
        model.changePostFavourite(position, favorite, isFavourite, progressBar);
    }

    private void changeStatus(int position, Button status, ProgressBar progressBar) {
        model.changeStatus(position, status, progressBar);
    }

    private static class HomeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final Context context;
        private final HomeFragment homeFragment;
        private final List<HomeFragmentViewModel.RecyclerViewPost> posts;
        private final FirebaseFirestore db;

        public HomeRecyclerViewAdapter(Context context, HomeFragment homeFragment) {
            this.context = context;
            this.posts = new ArrayList<>();
            this.homeFragment = homeFragment;
            db = FirebaseFirestore.getInstance();
        }

        public void setPosts(List<HomeFragmentViewModel.RecyclerViewPost> newPosts) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PostDiffCallback(posts, newPosts));
            posts.clear();
            posts.addAll(newPosts);
            diffResult.dispatchUpdatesTo(this);
        }

        public PostInfo getPost(int position) {
            return posts.get(position).getPostInfo();
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(context).inflate(R.layout.card_post_info,parent,false);
            return new PostHolder(rootView);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            HomeFragmentViewModel.RecyclerViewPost recyclerViewPost = posts.get(position);
            PostInfo post = recyclerViewPost.getPostInfo();
            PostHolder postHolder = (PostHolder) holder;

            postHolder.title.setText(post.getTitle());
            postHolder.amount.setText(String.format("Rs. %s", post.getAmount()));
            postHolder.people.setText(String.format("%d\nPeople", post.getPeopleRequired()));
            postHolder.time.setText(getFormattedTime(post.getYears(), post.getMonths(), post.getDays()));

            if (post.getAccomplished() && recyclerViewPost.getStatus().equals(INTERESTED)) {
                postHolder.postStatus.setVisibility(GONE);
            } else {
                postHolder.postStatus.setText(recyclerViewPost.getStatus());
            }

            db.collection(USER_PROFILE).document(post.getOwnerID()).get()
                    .addOnSuccessListener(docSnap -> {
                        if (docSnap != null) {
                            try {
                                Glide.with(context).load(docSnap.getString("imageURL"))
                                        .error(R.drawable.default_person_icon)
                                        .circleCrop().into(postHolder.image);
                            } catch (Exception ignored) {}
                        }
                    });

            int icon = recyclerViewPost.isFavourite() ? R.drawable.filled_star_icon : R.drawable.unfilled_star_icon;
            postHolder.favorite.setImageResource(icon);
            postHolder.isFavourite = recyclerViewPost.isFavourite();
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        private static class PostDiffCallback extends DiffUtil.Callback {
            private final List<HomeFragmentViewModel.RecyclerViewPost> newList, oldList;

            public PostDiffCallback(List<HomeFragmentViewModel.RecyclerViewPost> oldList,
                                    List<HomeFragmentViewModel.RecyclerViewPost> newList) {
                this.oldList = oldList;
                this.newList = newList;
            }

            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).getPostInfo().getId().equals(
                        newList.get(newItemPosition).getPostInfo().getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                HomeFragmentViewModel.RecyclerViewPost oldItem = oldList.get(oldItemPosition);
                HomeFragmentViewModel.RecyclerViewPost newItem = newList.get(newItemPosition);
                return oldItem.getStatus().equals(newItem.getStatus())
                        && oldItem.isFavourite() == newItem.isFavourite()
                        && oldItem.getPostInfo().isContentSame(newItem.getPostInfo());
            }
        }

        private class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title, amount, time, people;
            ImageView image, favorite;
            Button postStatus;
            boolean isFavourite = false;
            ProgressBar statusProgressBar, favouriteProgressBar;

            public PostHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                title = itemView.findViewById(R.id.postTitle);
                amount = itemView.findViewById(R.id.postAmount);
                time = itemView.findViewById(R.id.postTime);
                people = itemView.findViewById(R.id.postPeople);
                image = itemView.findViewById(R.id.imageView);
                favorite = itemView.findViewById(R.id.favouriteIcon);
                postStatus = itemView.findViewById(R.id.postStatusButton);
                favouriteProgressBar = itemView.findViewById(R.id.favouriteProgressBar);
                statusProgressBar = itemView.findViewById(R.id.statusProgressBar);
                statusProgressBar.setVisibility(GONE);
                favouriteProgressBar.setVisibility(GONE);

                image.setOnClickListener(view ->
                        homeFragment.openUserFragment(getAdapterPosition()));

                favorite.setOnClickListener(view -> {
                    isFavourite = !isFavourite;
                    homeFragment.changeFavourite(getAdapterPosition(), favorite, isFavourite, favouriteProgressBar);
                });
                postStatus.setOnClickListener(view -> {
                    if (DBOperations.statusMap.containsKey(postStatus.getText().toString())) {
                        homeFragment.changeStatus(getAdapterPosition(), postStatus, statusProgressBar);
                    }
                });
            }

            @Override
            public void onClick(View view) {
                homeFragment.openPostFragment(getAdapterPosition(), image.getDrawable());
            }
        }
    }
}