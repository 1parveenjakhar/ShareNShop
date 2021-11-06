package com.puteffort.sharenshop.models;

public class PostStatus {
    private String postID, status;

    public PostStatus() {
    }

    public PostStatus(String postID) {
        this.postID = postID;
        this.status = "Added";
    }

    public PostStatus(String postID, String status) {
        this.postID = postID;
        this.status = status;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
