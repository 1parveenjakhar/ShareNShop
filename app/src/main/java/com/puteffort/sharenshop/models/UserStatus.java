package com.puteffort.sharenshop.models;

import com.puteffort.sharenshop.utils.DBOperations;

public class UserStatus {
    private String userID, status;

    public UserStatus() {

    }

    public UserStatus(String userID) {
        this.userID = userID;
        this.status = DBOperations.ADDED;
    }

    public UserStatus(String userID, String status) {
        this.userID = userID;
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
