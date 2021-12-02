package com.puteffort.sharenshop.fragments;

import static android.view.View.GONE;

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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel.RecyclerViewComment;

import java.util.ArrayList;
import java.util.List;

public class CommentRecyclerView extends Fragment {
    private RecyclerView recyclerView;
    private PostFragmentViewModel model;
    private CommentRecyclerViewAdapter adapter;
    private ProgressBar progressBar;

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

        progressBar.setVisibility(GONE);

        view.findViewById(R.id.addCommentButton).setOnClickListener(this::createComment);

        addObservers();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addObservers() {
        adapter = new CommentRecyclerViewAdapter(requireContext(), this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        model.getComments().observe(getViewLifecycleOwner(), comments -> {
            if (comments == null) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }
            adapter.setComments(comments);
            progressBar.setVisibility(GONE);
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

    private void openUserFragment(int position) {
        ((PostFragment)requireParentFragment()).openUserFragment(adapter.getUser(position));
    }


    private static class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<RecyclerViewComment> comments;
        private final Context context;
        private final CommentRecyclerView commentRecyclerView;

        public CommentRecyclerViewAdapter(Context context, CommentRecyclerView commentRecyclerView) {
            this.context = context;
            this.commentRecyclerView = commentRecyclerView;
            this.comments = new ArrayList<>();
        }

        void setComments(List<RecyclerViewComment> newComments) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommentDiffCallBack(comments, newComments));
            comments.clear();
            comments.addAll(newComments);
            diffResult.dispatchUpdatesTo(this);
        }

        UserProfile getUser(int position) {
            return comments.get(position).getUserProfile();
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


            Glide.with(context).load(comment.getUserProfile().getImageURL())
                    .error(R.drawable.default_person_icon)
                    .circleCrop().into(commentHolder.userImage);

            commentHolder.userName.setText(comment.getUserProfile().getName());
            commentHolder.comment.setText(comment.getComment().getMessage());
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }


        private static class CommentDiffCallBack extends DiffUtil.Callback {
            private final List<RecyclerViewComment> newList, oldList;

            public CommentDiffCallBack(List<RecyclerViewComment> oldList,
                                       List<RecyclerViewComment> newList) {
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
                return oldList.get(oldItemPosition).getComment().getId().equals(
                        newList.get(newItemPosition).getComment().getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                RecyclerViewComment oldItem = oldList.get(oldItemPosition);
                RecyclerViewComment newItem = newList.get(newItemPosition);
                return oldItem.getComment().isContentSame(newItem.getComment())
                        && oldItem.getUserProfile().isContentSame(newItem.getUserProfile());
            }
        }


        private class CommentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView userName, comment;
            ImageView userImage;

            public CommentHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                userName = itemView.findViewById(R.id.userName);
                userImage = itemView.findViewById(R.id.userImage);
                comment = itemView.findViewById(R.id.comment);
            }

            @Override
            public void onClick(View v) {
                commentRecyclerView.openUserFragment(getAdapterPosition());
            }
        }
    }
}