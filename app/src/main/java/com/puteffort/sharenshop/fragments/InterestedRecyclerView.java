package com.puteffort.sharenshop.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel;

import java.util.List;

public class InterestedRecyclerView extends Fragment {
    private RecyclerView recyclerView;
    private PostFragmentViewModel model;
    private InterestedRecyclerViewAdapter adapter;
    private ProgressBar progressBar;
    private boolean isUserPostOwner;

    public InterestedRecyclerView() {
        // Required empty public constructor
    }

    public InterestedRecyclerView(PostFragmentViewModel model, boolean isUserPostOwner) {
        this.model = model;
        this.isUserPostOwner = isUserPostOwner;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interested_recycler_view, container, false);

        recyclerView = view.findViewById(R.id.interestedRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        addObservers();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addObservers() {
        adapter = new InterestedRecyclerViewAdapter(this, isUserPostOwner, model.getUsersInterested());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        model.getInterestedIndex().observe(getViewLifecycleOwner(), index -> {
            // For loading data
            if (index == null) {
                progressBar.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
                return;
            }

            if (index != -1) {
                // Insert at index, if list is not empty
                adapter.notifyItemInserted(index);
            }
            progressBar.setVisibility(View.INVISIBLE);
        });

        model.getInterestedRemoveIndex().observe(getViewLifecycleOwner(), index -> {
            if (index == null) return;
            adapter.notifyItemRemoved(index);
        });
    }

    void addUser(boolean isUserAdded, int position, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        if (isUserAdded) model.addUser(position, progressBar);
        else model.removeUser(position, progressBar);
    }
}

class InterestedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<UserProfile> usersInterested;
    private final Context context;
    private final InterestedRecyclerView parentFragment;
    private final boolean isUserPostOwner;

    public InterestedRecyclerViewAdapter(InterestedRecyclerView parentFragment, boolean isUserPostOwner, List<UserProfile> usersInterested) {
        this.usersInterested = usersInterested;
        this.parentFragment = parentFragment;
        this.context = parentFragment.requireContext();
        this.isUserPostOwner = isUserPostOwner;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.card_user_info_owner,parent,false);
        return new UserHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserProfile user = usersInterested.get(position);
        UserHolder userHolder = (UserHolder) holder;

        if (isUserPostOwner) {
            userHolder.cross.setVisibility(View.VISIBLE);
            userHolder.tick.setVisibility(View.VISIBLE);

            userHolder.cross.setOnClickListener(view -> parentFragment.addUser(false, position, userHolder.crossPB));
            userHolder.tick.setOnClickListener(view -> parentFragment.addUser(true, position, userHolder.tickPB));
        }

        userHolder.userName.setText(user.getName());
        Glide.with(context).load(user.getImageURL())
                .error(R.drawable.default_person_icon)
                .circleCrop().into(userHolder.userImage);
    }

    @Override
    public int getItemCount() {
        return usersInterested.size();
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView userImage, cross, tick;
        ProgressBar tickPB, crossPB;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            userImage = itemView.findViewById(R.id.userImage);
            cross = itemView.findViewById(R.id.cross);
            tick = itemView.findViewById(R.id.tick);
            tickPB = itemView.findViewById(R.id.tickProgressBar);
            crossPB = itemView.findViewById(R.id.crossProgressBar);
        }
    }
}