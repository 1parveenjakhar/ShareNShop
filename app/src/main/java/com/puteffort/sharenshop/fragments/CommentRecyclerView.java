package com.puteffort.sharenshop.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel.RecyclerViewComment;

import java.util.List;

public class CommentRecyclerView extends Fragment {
    private RecyclerView recyclerView;
    private PostFragmentViewModel model;
    private CommentRecyclerViewAdapter adapter;
    private ProgressBar progressBar;

    public static final int EMPTY_LIST = -1;

    public CommentRecyclerView() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_recycler_view, container, false);
        model = new ViewModelProvider(requireParentFragment()).get(PostFragmentViewModel.class);

        recyclerView = view.findViewById(R.id.commentRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        view.findViewById(R.id.addCommentButton).setOnClickListener(this::createComment);

        addObservers();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addObservers() {
        adapter = new CommentRecyclerViewAdapter(requireContext(), model.getComments());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);


        model.getCommentIndex().observe(getViewLifecycleOwner(), index -> {
            // For loading data
            if (index == null) {
                progressBar.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
                return;
            }

            if (index != EMPTY_LIST) {
                // Insert only if index != -1
                adapter.notifyItemInserted(index);
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void createComment(View view) {
        @SuppressLint("InflateParams") View customDialog = LayoutInflater.from(requireContext())
                .inflate(R.layout.comment_pop_up, null, false);

        EditText commentBox = ((TextInputLayout)customDialog.findViewById(R.id.commentBox)).getEditText();
        if (commentBox == null) {
            commentBox = new EditText(requireContext());
        }
        ProgressBar progressBar = customDialog.findViewById(R.id.progressBar);

        EditText finalCommentBox = commentBox;
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext()).setView(customDialog)
                .setPositiveButton("Comment", null)
                .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String message = finalCommentBox.getText().toString().trim();
            if (!message.isEmpty()) {
                model.addComment(message, alertDialog, requireContext(), progressBar);
            } else {
                finalCommentBox.setError("Comment should not be empty !");
            }
        });
    }
}

class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<RecyclerViewComment> comments;
    private final Context context;

    public CommentRecyclerViewAdapter(Context context, List<RecyclerViewComment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.card_comment_info,parent,false);
        return new CommentHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecyclerViewComment comment = comments.get(position);
        CommentHolder commentHolder = (CommentHolder) holder;


        Glide.with(context).load(comment.getImageURL())
                .error(R.drawable.default_person_icon)
                .circleCrop().into(commentHolder.userImage);

        commentHolder.userName.setText(comment.getName());
        commentHolder.comment.setText(comment.getMessage());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentHolder extends RecyclerView.ViewHolder {
        TextView userName, comment;
        ImageView userImage;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            userImage = itemView.findViewById(R.id.userImage);
            comment = itemView.findViewById(R.id.comment);
        }
    }
}