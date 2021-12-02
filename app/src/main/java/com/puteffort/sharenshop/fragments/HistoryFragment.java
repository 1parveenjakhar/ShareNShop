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
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentHistoryBinding;
import com.puteffort.sharenshop.interfaces.DualPanePostCommunicator;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.viewmodels.HistoryFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private HistoryFragmentViewModel model;
    private HistoryRecyclerViewAdapter recyclerViewAdapter;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false);
        model = new ViewModelProvider(requireActivity()).get(HistoryFragmentViewModel.class);

        binding.progressBar.setVisibility(View.GONE);

        setUpComponents();
        addObservers();

        return binding.getRoot();
    }

    @SuppressLint("NonConstantResourceId")
    private void setUpComponents() {
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.postsRecyclerView.setHasFixedSize(true);
        recyclerViewAdapter = new HistoryRecyclerViewAdapter(requireContext(), this);
        binding.postsRecyclerView.setAdapter(recyclerViewAdapter);

        binding.swipeRefreshHistory.setOnRefreshListener(DBOperations::getUserDetails);

        for (int i = 0; i < binding.chips.getChildCount(); i++) {
            ((Chip)binding.chips.getChildAt(i)).setOnCheckedChangeListener((chip, isChecked) -> {
                Integer chipNum;
                switch (chip.getId()) {
                    case R.id.postsCreatedChip: chipNum = 0; break;
                    case R.id.postsWishListedChip: chipNum = 1; break;
                    case R.id.postsInvolvedChip: chipNum = 2; break;
                    default: chipNum = null;
                }
                if (chipNum != null)
                    model.changeData(chipNum, isChecked);
            });
        }

        for (int chipNum: model.getChipNumbers()) {
            Chip chip;
            switch (chipNum) {
                case 0: chip = binding.postsCreatedChip; break;
                case 1: chip = binding.postsWishListedChip; break;
                case 2: chip = binding.postsInvolvedChip; break;
                default: chip = null;
            }
            if (chip != null)
                chip.setChecked(true);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addObservers() {
        model.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts == null) {
                binding.progressBar.setVisibility(View.VISIBLE);
                return;
            }
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.swipeRefreshHistory.setRefreshing(false);
            recyclerViewAdapter.setPosts(posts);
        });
    }

    private void openPostFragment(int position, Drawable postOwnerImage) {
        ((DualPanePostCommunicator)requireParentFragment()).openPostFragment(recyclerViewAdapter.getPost(position), postOwnerImage);
    }

    private void openUserFragment(int position) {
        ((DualPanePostCommunicator)requireParentFragment()).openUserFragment(recyclerViewAdapter.getPost(position).getOwnerID());
    }

    private void removeFavourite(int position, ProgressBar favProgress, ImageView favIcon) {
        model.removeFavourite(position, favProgress, favIcon);
    }

    private static class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<PostInfo> posts;
        private final Context context;
        private final HistoryFragment historyFragment;
        private final FirebaseFirestore db;

        public HistoryRecyclerViewAdapter(Context context, HistoryFragment historyFragment) {
            this.context = context;
            this.posts = new ArrayList<>();
            this.historyFragment = historyFragment;
            db = FirebaseFirestore.getInstance();
        }

        void setPosts(List<PostInfo> posts) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new HistoryDiffCallback(this.posts, posts));
            this.posts.clear();
            this.posts.addAll(posts);
            diffResult.dispatchUpdatesTo(this);
        }

        PostInfo getPost(int position) {
            return posts.get(position);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(context).inflate(R.layout.card_history,parent,false);
            return new PostHolder(rootView);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            PostInfo post = posts.get(position);
            PostHolder postHolder = (PostHolder) holder;
            db.collection(USER_PROFILE).document(post.getOwnerID()).get()
                    .addOnSuccessListener(docSnap -> {
                        if (docSnap != null) {
                            Glide.with(context).load(docSnap.getString("imageURL"))
                                    .error(R.drawable.default_person_icon)
                                    .circleCrop().into(postHolder.image);
                        }
                    });

            if (historyFragment.model.getWishListedIds().contains(post.getId())) {
                postHolder.favorite.setVisibility(View.VISIBLE);
            } else {
                postHolder.favorite.setVisibility(View.INVISIBLE);
            }

            postHolder.title.setText(post.getTitle());
            postHolder.amount.setText(String.format("Rs. %s", post.getAmount()));
            postHolder.people.setText(String.format("%d\nPeople", post.getPeopleRequired()));
            postHolder.time.setText(getFormattedTime(post.getYears(), post.getMonths(), post.getDays()));
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        private static class HistoryDiffCallback extends DiffUtil.Callback {
            private final List<PostInfo> newList, oldList;

            public HistoryDiffCallback(List<PostInfo> oldList,
                                    List<PostInfo> newList) {
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
                return oldList.get(oldItemPosition).getId().equals(
                        newList.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).isContentSame(newList.get(newItemPosition));
            }
        }

        class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title, amount, time, people;
            ImageView image, favorite;
            ProgressBar favProgress;

            public PostHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                title = itemView.findViewById(R.id.postTitle);
                amount = itemView.findViewById(R.id.postAmount);
                time = itemView.findViewById(R.id.postTime);
                people = itemView.findViewById(R.id.postPeople);
                image = itemView.findViewById(R.id.imageView);
                favorite = itemView.findViewById(R.id.favouriteIcon);
                favProgress = itemView.findViewById(R.id.favProgress);

                favProgress.setVisibility(View.GONE);

                image.setOnClickListener(view ->
                        historyFragment.openUserFragment(getAdapterPosition()));

                favorite.setOnClickListener(view -> historyFragment.removeFavourite(getAdapterPosition(), favProgress, favorite));
            }

            @Override
            public void onClick(View view) {
                historyFragment.openPostFragment(getAdapterPosition(), image.getDrawable());
            }

        }
    }
}