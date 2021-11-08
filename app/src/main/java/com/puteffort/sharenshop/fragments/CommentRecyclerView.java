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
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel;
import com.puteffort.sharenshop.viewmodels.PostFragmentViewModel.RecyclerViewComment;

import java.util.List;

public class CommentRecyclerView extends Fragment {
    private RecyclerView recyclerView;
    private PostFragmentViewModel model;
    private CommentRecyclerViewAdapter adapter;
    private ProgressBar progressBar;

    public CommentRecyclerView() {
        // Required empty public constructor
    }

    public CommentRecyclerView(PostFragmentViewModel model) {
        this.model = model;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_recycler_view, container, false);

        recyclerView = view.findViewById(R.id.commentRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        addObservers();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addObservers() {
        adapter = new CommentRecyclerViewAdapter(requireContext(), model.getComments().getValue());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);


        model.getComments().observe(getViewLifecycleOwner(), comments -> {
            if (comments != null) {
                progressBar.setVisibility(View.INVISIBLE);
                adapter.setComments(comments);
                adapter.notifyDataSetChanged();
            }
        });

        model.getCommentIndex().observe(getViewLifecycleOwner(), index -> adapter.notifyItemInserted(index));
    }
}

class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<RecyclerViewComment> comments;
    private final Context context;

    public CommentRecyclerViewAdapter(Context context, List<RecyclerViewComment> comments) {
        this.comments = comments;
        this.context = context;
    }

    void setComments(List<RecyclerViewComment> comments) {
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
                .error(Glide.with(commentHolder.userImage).load(R.drawable.default_person_icon))
                .circleCrop().into(commentHolder.userImage);

        commentHolder.userName.setText(comment.getName());
        commentHolder.comment.setText(comment.getMessage());
    }

    @Override
    public int getItemCount() {
        if (comments == null)
            return 0;
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