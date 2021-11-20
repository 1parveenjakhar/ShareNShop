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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentHistoryBinding;
import com.puteffort.sharenshop.interfaces.DualPanePostCommunicator;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.viewmodels.HistoryFragmentViewModel;

import java.util.List;
import java.util.Objects;

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

        binding.swipeRefreshHistory.setOnRefreshListener(() -> model.loadData());

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
            recyclerViewAdapter.setPosts(posts);
            if (posts == null) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.INVISIBLE);
                binding.swipeRefreshHistory.setRefreshing(false);
            }
            recyclerViewAdapter.notifyDataSetChanged();
        });

        model.getModifyIndex().observe(getViewLifecycleOwner(), index -> {
            if (index == null) return;
            recyclerViewAdapter.notifyItemChanged(index);
        });
        model.getDeleteIndex().observe(getViewLifecycleOwner(), index -> {
            if (index == null) return;
            recyclerViewAdapter.notifyItemRemoved(index);
        });
    }

    private void openPostFragment(int position, Drawable postOwnerImage) {
        PostFragment postFragment =
                new PostFragment(Objects.requireNonNull(model.getPosts().getValue()).get(position), postOwnerImage);
        ((DualPanePostCommunicator)requireParentFragment()).openPostFragment(postFragment);
    }

    private void removeFavourite(int position, ProgressBar favProgress) {
        model.removeFavourite(position, favProgress);
    }

    private static class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<PostInfo> posts;
        private final Context context;
        private final HistoryFragment historyFragment;
        private final FirebaseFirestore db;

        public HistoryRecyclerViewAdapter(Context context, HistoryFragment historyFragment) {
            this.context = context;
            this.historyFragment = historyFragment;
            db = FirebaseFirestore.getInstance();
        }

        void setPosts(List<PostInfo> posts) {
            this.posts = posts;
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
            if (posts == null) return 0;
            return posts.size();
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

                favorite.setOnClickListener(view -> historyFragment.removeFavourite(getAdapterPosition(), favProgress));
            }

            @Override
            public void onClick(View view) {
                historyFragment.openPostFragment(getAdapterPosition(), image.getDrawable());
            }

        }
    }
}