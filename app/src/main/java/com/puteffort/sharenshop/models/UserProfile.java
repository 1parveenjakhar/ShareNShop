package com.puteffort.sharenshop.models;

public class UserProfile {
    private String id, name, email, imageBitmapString;

    public UserProfile() {}
    public UserProfile(String name, String email, String imageBitmapString) {
        this.name = name;
        this.email = email;
        this.imageBitmapString = imageBitmapString;
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

    public String getImageBitmapString() {
        return imageBitmapString;
    }

    public void setImageBitmapString(String imageBitmapString) {
        this.imageBitmapString = imageBitmapString;
    }
}
