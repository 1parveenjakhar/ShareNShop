package com.puteffort.sharenshop.fragments;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentHomeBinding;
import com.puteffort.sharenshop.interfaces.DualPanePostCommunicator;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.viewmodels.HomeFragmentViewModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

        setUpComponents();
        addObservers();

        return view;
    }

    private void setUpComponents() {
        binding.searchView.setOnQueryTextListener(model);

        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.postsRecyclerView.setHasFixedSize(true);
        recyclerViewAdapter = new HomeRecyclerViewAdapter(requireContext(),
                model.getPosts().getValue(),
                model.getWishListedPosts(),
                model.getPostsStatus(),
                this);
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

        binding.notificationIconImageView.setOnClickListener(v -> {
            NotificationRecyclerView notificationRecyclerView = new NotificationRecyclerView();
            ((MainActivity)requireActivity()).changeFragment(notificationRecyclerView);
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

        model.isDataUpdating().observe(requireActivity(),
                dataUpdating -> binding.progressBar.setVisibility(dataUpdating ? View.VISIBLE : View.GONE));

        DBOperations.getUserActivity().observe(requireActivity(), userActivity -> {
            if (userActivity == null) return;
            binding.swipeRefreshPostList.setRefreshing(false);
            model.changeUserDetails(userActivity);
        });
    }

    private void openPostFragment(int position, Drawable postOwnerImage) {
        PostFragment postFragment =
                new PostFragment(Objects.requireNonNull(model.getPosts().getValue()).get(position), postOwnerImage);
        ((DualPanePostCommunicator)requireParentFragment()).openPostFragment(postFragment);
    }

    public void changeFavourite(int position, ImageView favorite, boolean isFavourite, ProgressBar progressBar) {
        model.changePostFavourite(position, favorite, isFavourite, progressBar);
    }

    public void changeStatus(int position, Button status, ProgressBar progressBar) {
        model.changeStatus(position, status, progressBar);
    }

    private static class HomeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final Context context;
        private final HomeFragment homeFragment;
        private final List<PostInfo> postsInfo;
        private final Set<String> wishListedPosts;
        private final Map<String, String> postsStatus;
        private final FirebaseFirestore db;

        public HomeRecyclerViewAdapter(Context context, List<PostInfo> postsInfo, Set<String> wishListedPosts, Map<String, String> postsStatus, HomeFragment homeFragment) {
            this.context = context;
            this.postsInfo = postsInfo;
            this.wishListedPosts = wishListedPosts;
            this.postsStatus = postsStatus;
            this.homeFragment = homeFragment;
            db = FirebaseFirestore.getInstance();
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
            PostInfo post = postsInfo.get(position);
            PostHolder postHolder = (PostHolder) holder;

            postHolder.title.setText(post.getTitle());
            postHolder.amount.setText(String.format("Rs. %s", post.getAmount()));
            postHolder.people.setText(String.format("%d\nPeople", post.getPeopleRequired()));
            postHolder.time.setText(getFormattedTime(post.getYears(), post.getMonths(), post.getDays()));

            db.collection(USER_PROFILE).document(post.getOwnerID()).get()
                    .addOnSuccessListener(docSnap -> {
                        if (docSnap != null) {
                            Glide.with(context).load(docSnap.getString("imageURL"))
                                    .error(R.drawable.default_person_icon)
                                    .circleCrop().into(postHolder.image);
                        }
                    });

            postHolder.isFavourite = wishListedPosts.contains(post.getId());
            int icon = postHolder.isFavourite ? R.drawable.filled_star_icon : R.drawable.unfilled_star_icon;
            postHolder.favorite.setImageResource(icon);

            // Change button accordingly, as per status of the user towards the post
            postHolder.postStatus.setText(postsStatus.containsKey(post.getId())
                    ? postsStatus.get(post.getId()) : "Interested ?");
        }

        @Override
        public int getItemCount() {
            return postsInfo.size();
        }

        class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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