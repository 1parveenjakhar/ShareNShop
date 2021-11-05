package com.puteffort.sharenshop.models;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserProfile {
    private String email;
    private String id;
    private String imageBitmapString;
    private boolean authLinkedStatus;
    private String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getImageBitmapString() {
        return imageBitmapString;
    }

    public boolean getAuthLinkedStatus() {
        return authLinkedStatus;
    }

    public void setAuthLinkedStatus(boolean authLinkedStatus) {
        this.authLinkedStatus = authLinkedStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
