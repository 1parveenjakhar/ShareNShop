package com.puteffort.sharenshop.models;

import java.util.ArrayList;
import java.util.List;

public class UserActivity {
    private String id;
    private List<String> postsCreated, postsWishListed;
    private List<PostStatus> postsInvolved;

    public UserActivity() {}

    public UserActivity(String id) {
        this.id = id;
        postsCreated = new ArrayList<>();
        postsInvolved = new ArrayList<>();
        postsWishListed = new ArrayList<>();
    }

    public UserActivity(String id, String postID) {
        this(id);
        postsInvolved.add(new PostStatus(postID));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPostsCreated() {
        return postsCreated;
    }

    public void setPostsCreated(List<String> postsCreated) {
        this.postsCreated = postsCreated;
    }

    public List<PostStatus> getPostsInvolved() {
        return postsInvolved;
    }

    public void setPostsInvolved(List<PostStatus> postsInvolved) {
        this.postsInvolved = postsInvolved;
    }

    public List<String> getPostsWishListed() {
        return postsWishListed;
    }

    public void setPostsWishListed(List<String> postsWishListed) {
        this.postsWishListed = postsWishListed;
    }

    public void setValues(UserActivity userActivity) {
        this.id = userActivity.getId();
        this.postsCreated = userActivity.getPostsCreated();
        this.postsInvolved = userActivity.getPostsInvolved();
        this.postsWishListed = userActivity.getPostsWishListed();
    }
}