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

import java.util.ArrayList;
import java.util.List;

public class AddedRecyclerView extends Fragment {
    private RecyclerView recyclerView;
    private PostFragmentViewModel model;
    private AddedRecyclerViewAdapter adapter;
    private ProgressBar progressBar;

    public AddedRecyclerView() {
        // Required empty public constructor
    }

    public AddedRecyclerView(PostFragmentViewModel model) {
        this.model = model;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_added_recycler_view, container, false);

        recyclerView = view.findViewById(R.id.addedRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        addObservers();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addObservers() {
        adapter = new AddedRecyclerViewAdapter(requireContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        model.getUsersAdded().observe(getViewLifecycleOwner(), usersAdded -> {
            if (usersAdded != null) {
                progressBar.setVisibility(View.INVISIBLE);
                adapter.setUsers(usersAdded);
                adapter.notifyDataSetChanged();
            }
        });

        model.getAddedIndex().observe(getViewLifecycleOwner(), index -> adapter.notifyItemInserted(index));
    }
}

class AddedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<UserProfile> usersAdded;
    private final Context context;

    public AddedRecyclerViewAdapter(Context context) {
        this.usersAdded = new ArrayList<>();
        this.context = context;
    }

    void setUsers(List<UserProfile> usersAdded) {
        this.usersAdded = usersAdded;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.card_user_info,parent,false);
        return new UserHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserProfile user = usersAdded.get(position);
        UserHolder userHolder = (UserHolder) holder;

        userHolder.userName.setText(user.getName());
        Glide.with(context).load(user.getImageURL())
                .error(Glide.with(userHolder.userImage).load(R.drawable.default_person_icon))
                .circleCrop().into(userHolder.userImage);
    }

    @Override
    public int getItemCount() {
        return usersAdded.size();
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView userImage;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            userImage = itemView.findViewById(R.id.userImage);
        }
    }
}