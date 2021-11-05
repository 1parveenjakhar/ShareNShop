package com.puteffort.sharenshop.models;

public class UserProfile {
    private String id, name, email, imageURL;
    private boolean isAuthLinked;

    public UserProfile() {}
    public UserProfile(String name, String email, String imageURL) {
        this.name = name;
        this.email = email;
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public boolean isAuthLinked() {
        return isAuthLinked;
    }

    public void setAuthLinked(boolean authLinked) {
        isAuthLinked = authLinked;
    }
}
