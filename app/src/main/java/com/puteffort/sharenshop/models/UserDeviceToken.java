package com.puteffort.sharenshop.models;

public class UserDeviceToken {
    private String token;

    public UserDeviceToken() {

    }

    public UserDeviceToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
