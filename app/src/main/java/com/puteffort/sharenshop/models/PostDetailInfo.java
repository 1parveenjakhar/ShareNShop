package com.puteffort.sharenshop.models;

import java.util.ArrayList;
import java.util.List;

public class PostDetailInfo {
    private String id;
    private List<String> comments, usersInterested;
    private List<UserStatus> usersAdded;

    public PostDetailInfo() {}

    public PostDetailInfo(String userID) {
        this.comments = new ArrayList<>();
        this.usersInterested = new ArrayList<>();
        this.usersAdded = new ArrayList<>();
        usersAdded.add(new UserStatus(userID));
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public List<String> getUsersInterested() {
        return usersInterested;
    }

    public void setUsersInterested(List<String> usersInterested) {
        this.usersInterested = usersInterested;
    }

    public List<UserStatus> getUsersAdded() {
        return usersAdded;
    }

    public void setUsersAdded(List<UserStatus> usersAdded) {
        this.usersAdded = usersAdded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

