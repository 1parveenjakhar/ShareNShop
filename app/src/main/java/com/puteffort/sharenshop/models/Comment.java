package com.puteffort.sharenshop.models;

public class Comment {
    private String id, message, userID;
    private long postedTime;

    public Comment() {}

    public Comment(String id, String message, String userID) {
        this.id = id;
        this.message = message;
        this.userID = userID;
        postedTime = System.currentTimeMillis();
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

    public long getPostedTime() {
        return postedTime;
    }

    public void setPostedTime(long postedTime) {
        this.postedTime = postedTime;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
