package com.puteffort.sharenshop.adapters;

import static com.puteffort.sharenshop.utils.DBOperations.USER_PROFILE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.utils.StaticData;

import java.util.List;

public class PostsListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<PostInfo> postsInfo;
    private ItemClickListener mClickListener;
    private final FirebaseFirestore db;

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public PostsListRecyclerViewAdapter(Context context, List<PostInfo> postsInfo) {
        this.context = context;
        this.postsInfo = postsInfo;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.card_post_info,parent,false);
        return new PostHolder(rootView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostInfo post = postsInfo.get(position);
        PostHolder postHolder = (PostHolder) holder;

        postHolder.title.setText(post.getTitle());
        postHolder.amount.setText(String.format("Rs. %s", post.getAmount()));
        postHolder.people.setText(String.format("%d\nPeople", post.getPeopleRequired()));
        StringBuilder time = new StringBuilder();
        if (post.getYears() != 0) time.append(post.getYears()).append("Y ");
        if (post.getMonths() != 0) time.append(post.getMonths()).append("M ");
        if (post.getDays() != 0) time.append(post.getDays()).append("D ");
        postHolder.time.setText(time.toString().trim());

        db.collection(USER_PROFILE).document(post.getOwnerID()).get()
        .addOnSuccessListener(docSnap -> {
                if (docSnap != null) {
                    try {
                        Glide.with(context).load(docSnap.getString("imageURL")).
                                circleCrop().into(postHolder.image);
                    } catch (Exception e) {
                        postHolder.image.setImageBitmap(StaticData.getDefaultImageBitmap(context));
                    }
                }
            });
    }

    @Override
    public int getItemCount() {
        return postsInfo.size();
    }

    class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, amount, time, people;
        ImageView image;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            title = itemView.findViewById(R.id.postTitle);
            amount = itemView.findViewById(R.id.postAmount);
            time = itemView.findViewById(R.id.postTime);
            people = itemView.findViewById(R.id.postPeople);
            image = itemView.findViewById(R.id.imageView);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
}
