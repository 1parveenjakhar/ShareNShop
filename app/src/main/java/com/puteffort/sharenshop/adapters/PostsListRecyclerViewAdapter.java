package com.puteffort.sharenshop.adapters;

import static com.puteffort.sharenshop.utils.DBOperations.USER_PROFILE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.utils.StaticData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PostsListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<PostInfo> postsInfo;
    private final Set<String> wishListedPosts;
    private final Map<String, String> postsStatus;
    private ItemClickListener mClickListener;
    private final FirebaseFirestore db;

    public interface ItemClickListener {
        void onItemClick(View view, int position, Drawable ownerImage);
        void changeFavourite(int position, boolean isFavourite);
        void changeStatus(int position, String status);
    }

    public PostsListRecyclerViewAdapter(Context context, List<PostInfo> postsInfo, Set<String> wishListedPosts, Map<String, String> postsStatus) {
        this.context = context;
        this.postsInfo = postsInfo;
        this.wishListedPosts = wishListedPosts;
        this.postsStatus = postsStatus;
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
                    Glide.with(context).load(docSnap.getString("imageURL"))
                            .error(Glide.with(postHolder.image).load(R.drawable.default_person_icon))
                            .circleCrop().into(postHolder.image);
                }
            });

        postHolder.isFavourite = wishListedPosts.contains(post.getId());
        int icon = postHolder.isFavourite ? R.drawable.filled_star_icon : R.drawable.unfilled_star_icon;
        postHolder.favorite.setImageResource(icon);

        // Change button accordingly, as per status of the user towards the post
        postHolder.postStatus.setText(postsStatus.containsKey(post.getId())
            ? postsStatus.get(post.getId()) : "Interested ?");
    }

    @Override
    public int getItemCount() {
        return postsInfo.size();
    }

    class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, amount, time, people;
        ImageView image, favorite;
        Button postStatus;
        boolean isFavourite = false;
        ProgressBar progressBar;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            title = itemView.findViewById(R.id.postTitle);
            amount = itemView.findViewById(R.id.postAmount);
            time = itemView.findViewById(R.id.postTime);
            people = itemView.findViewById(R.id.postPeople);
            image = itemView.findViewById(R.id.imageView);
            favorite = itemView.findViewById(R.id.favouriteIcon);
            postStatus = itemView.findViewById(R.id.postStatusButton);
            progressBar = itemView.findViewById(R.id.statusProgressBar);
            progressBar.setVisibility(View.INVISIBLE);

            favorite.setOnClickListener(view -> {
                if (mClickListener != null) {
                    isFavourite = !isFavourite;
                    mClickListener.changeFavourite(getAdapterPosition(), isFavourite);
                }
            });
            postStatus.setOnClickListener(view -> {
                if (mClickListener != null && DBOperations.statusMap.containsKey(postStatus.getText().toString())) {
                    progressBar.setVisibility(View.VISIBLE);
                    String status = postStatus.getText().toString();
                    postStatus.setText("");
                    mClickListener.changeStatus(getAdapterPosition(), status);
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition(), image.getDrawable());
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
}
