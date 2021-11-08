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
import com.google.firebase.auth.FirebaseAuth;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class InterestedRecyclerView extends Fragment {
    private RecyclerView recyclerView;
    private PostFragmentViewModel model;
    private InterestedRecyclerViewAdapter adapter;
    private ProgressBar progressBar;
    private String postOwnerID;
    private String userID;

    public InterestedRecyclerView() {
        // Required empty public constructor
    }

    public InterestedRecyclerView(PostFragmentViewModel model, String postOwnerID) {
        this.model = model;
        this.postOwnerID = postOwnerID;
        userID = FirebaseAuth.getInstance().getUid();
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
        adapter = new InterestedRecyclerViewAdapter(requireContext(), userID.equals(postOwnerID));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        model.getUsersInterested().observe(getViewLifecycleOwner(), usersInterested -> {
            if (usersInterested != null) {
                progressBar.setVisibility(View.INVISIBLE);
                adapter.setUsers(usersInterested);
                adapter.notifyDataSetChanged();
            }
        });

        model.getInterestedIndex().observe(getViewLifecycleOwner(), index -> adapter.notifyItemInserted(index));
    }
}

class InterestedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<UserProfile> usersInterested;
    private final Context context;
    private final boolean showOptions;

    public InterestedRecyclerViewAdapter(Context context, boolean showOptions) {
        this.usersInterested = new ArrayList<>();
        this.context = context;
        this.showOptions = showOptions;
    }

    void setUsers(List<UserProfile> usersInterested) {
        this.usersInterested = usersInterested;
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

        if (showOptions) {
            userHolder.cross.setVisibility(View.VISIBLE);
            userHolder.tick.setVisibility(View.VISIBLE);

            // TODO(Add their listeners)
        }

        userHolder.userName.setText(user.getName());
        Glide.with(context).load(user.getImageURL())
                .error(Glide.with(userHolder.userImage).load(R.drawable.default_person_icon))
                .circleCrop().into(userHolder.userImage);
    }

    @Override
    public int getItemCount() {
        return usersInterested.size();
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView userImage, cross, tick;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            userImage = itemView.findViewById(R.id.userImage);
            cross = itemView.findViewById(R.id.cross);
            tick = itemView.findViewById(R.id.tick);
        }
    }
}