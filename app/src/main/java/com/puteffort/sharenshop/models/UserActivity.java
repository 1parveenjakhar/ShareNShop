package com.puteffort.sharenshop.models;

import java.util.ArrayList;
import java.util.List;

public class UserActivity {
    private String id;
    private List<String> postsCreated, postsInvolved, postsWishListed;

    public UserActivity() {}

    public UserActivity(String id) {
        this.id = id;
        postsCreated = new ArrayList<>();
        postsInvolved = new ArrayList<>();
        postsWishListed = new ArrayList<>();
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

    public List<String> getPostsInvolved() {
        return postsInvolved;
    }

    public void setPostsInvolved(List<String> postsInvolved) {
        this.postsInvolved = postsInvolved;
    }

    public List<String> getPostsWishListed() {
        return postsWishListed;
    }

    public void setPostsWishListed(List<String> postsWishListed) {
        this.postsWishListed = postsWishListed;
    }
}
