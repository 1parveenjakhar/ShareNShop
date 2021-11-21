package com.puteffort.sharenshop.models;

public class Notification {

    private String productName;
    private String notifDescription;

    public Notification(String productName, String notifDescription) {
        this.productName = productName;
        this.notifDescription = notifDescription;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getNotifDescription() {
        return notifDescription;
    }

    public void setNotifDescription(String notifDescription) {
        this.notifDescription = notifDescription;
    }
}
