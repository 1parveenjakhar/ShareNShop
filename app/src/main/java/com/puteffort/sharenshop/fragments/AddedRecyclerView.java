package com.puteffort.sharenshop.fragments;

import static android.view.View.GONE;
import static com.puteffort.sharenshop.utils.DBOperations.ACCEPTED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.UserStatus;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddedRecyclerView extends Fragment {
    private RecyclerView recyclerView;
    private PostFragmentViewModel model;
    private AddedRecyclerViewAdapter adapter;
    private ProgressBar progressBar, buttonProgressBar;
    private Button finalButton;

    public AddedRecyclerView() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_added_recycler_view, container, false);
        model = new ViewModelProvider(requireParentFragment()).get(PostFragmentViewModel.class);

        recyclerView = view.findViewById(R.id.addedRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        finalButton = view.findViewById(R.id.finalButton);
        buttonProgressBar = view.findViewById(R.id.buttonProgressBar);

        setUpComponents();
        addObservers();

        return view;
    }

    private void setUpComponents() {
        buttonProgressBar.setVisibility(GONE);
        finalButton.setVisibility(GONE);
        finalButton.setOnClickListener(buttonView ->
                model.askForFinalConfirmation(buttonProgressBar, finalButton, getString(R.string.ask_for_final_confirmation)));

        adapter = new AddedRecyclerViewAdapter(requireContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void addObservers() {
        model.getUsersAdded().observe(getViewLifecycleOwner(), users -> {
            if (users == null) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }
            adapter.setUsers(users);
            progressBar.setVisibility(GONE);
        });

        model.getPostDetailInfo().observe(getViewLifecycleOwner(), postDetailInfo -> {
            if (postDetailInfo != null) {
                PostInfo postInfo = model.getPostInfo().getValue();
                if (postInfo == null) return;
                if (postDetailInfo.getUsersAdded().size() == postInfo.getPeopleRequired()) { // if requirement is complete
                    if (!postInfo.getAsked()) { // owner has not asked for final confirmation
                        finalButton.setVisibility(model.isUserPostOwner() ? View.VISIBLE : GONE);
                    } else { // already asked
                        finalButton.setVisibility(View.VISIBLE);
                        if (areAllAccepted(postDetailInfo.getUsersAdded())) {
                            finalButton.setText("Completed 🤩🥳");
                        } else {
                            finalButton.setText(R.string.in_progress);
                        }
                    }
                }
            }
        });
    }

    private boolean areAllAccepted(List<UserStatus> users) {
        for (UserStatus user: users) {
            if (!user.getStatus().equals(ACCEPTED))
                return false;
        }
        return true;
    }
}

class AddedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<PostFragmentViewModel.AddedUser> usersAdded;
    private final Context context;

    public AddedRecyclerViewAdapter(Context context) {
        this.context = context;
        this.usersAdded = new ArrayList<>();
    }

    public void setUsers(List<PostFragmentViewModel.AddedUser> users) {
        int previousSize = usersAdded.size();
        usersAdded.clear();
        usersAdded.addAll(users);
        notifyItemRangeRemoved(0, previousSize);
        notifyItemRangeInserted(0, users.size());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.card_added_user,parent,false);
        return new UserHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostFragmentViewModel.AddedUser user = usersAdded.get(position);
        UserHolder userHolder = (UserHolder) holder;

        userHolder.tick.setVisibility(
                    user.getStatus().equals(ACCEPTED) ? View.VISIBLE : GONE);

        userHolder.userName.setText(user.getProfile().getName());
        Glide.with(context).load(user.getProfile().getImageURL())
                .error(R.drawable.default_person_icon)
                .circleCrop().into(userHolder.userImage);
    }

    @Override
    public int getItemCount() {
        return usersAdded.size();
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView userImage, tick;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            userImage = itemView.findViewById(R.id.userImage);
            tick = itemView.findViewById(R.id.tick);
            tick.setVisibility(GONE);
        }
    }
}