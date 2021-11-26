package com.puteffort.sharenshop.fragments;

import static android.view.View.GONE;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class InterestedRecyclerView extends Fragment {
    private RecyclerView recyclerView;
    PostFragmentViewModel model;
    private InterestedRecyclerViewAdapter adapter;
    private ProgressBar progressBar;

    public InterestedRecyclerView() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interested_recycler_view, container, false);
        model = new ViewModelProvider(requireParentFragment()).get(PostFragmentViewModel.class);

        recyclerView = view.findViewById(R.id.interestedRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        addObservers();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addObservers() {
        adapter = new InterestedRecyclerViewAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        model.getUsersInterested().observe(getViewLifecycleOwner(), usersInterested -> {
            if (usersInterested == null) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }
            adapter.setUsers(usersInterested);
            progressBar.setVisibility(GONE);
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

    public InterestedRecyclerViewAdapter(InterestedRecyclerView parentFragment) {
        this.usersInterested = new ArrayList<>();
        this.parentFragment = parentFragment;
        this.context = parentFragment.requireContext();
    }

    public void setUsers(List<UserProfile> users) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new UserDiffCallBack(usersInterested, users));
        usersInterested.clear();
        usersInterested.addAll(users);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.card_interested_user,parent,false);
        return new UserHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserProfile user = usersInterested.get(position);
        UserHolder userHolder = (UserHolder) holder;


        if (parentFragment.model.isUserPostOwner() && !parentFragment.model.areAllRequiredAdded()) {
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


    private static class UserDiffCallBack extends DiffUtil.Callback {
        private final List<UserProfile> newList, oldList;

        public UserDiffCallBack(List<UserProfile> oldList,
                                List<UserProfile> newList) {
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

            tick.setVisibility(GONE);
            cross.setVisibility(GONE);
            tickPB.setVisibility(GONE);
            crossPB.setVisibility(GONE);
        }
    }
}