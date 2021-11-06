package com.puteffort.sharenshop.models;

public class Comment {
    private String id, message, postID, userID;

    public Comment() {}

    public Comment(String id, String message, String postID, String userID) {
        this.id = id;
        this.message = message;
        this.postID = postID;
        this.userID = userID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
