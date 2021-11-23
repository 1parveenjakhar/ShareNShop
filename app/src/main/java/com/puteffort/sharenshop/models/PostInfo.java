package com.puteffort.sharenshop.models;

import androidx.annotation.NonNull;

public class PostInfo {
    private String title, description;
    private String ownerID;
    private String id;
    private int days, months, years, peopleRequired;
    private int amount;
    private boolean accomplished, asked;
    private long lastActivity;

    public PostInfo() {
        // Empty constructor required for Firestore
    }

    public PostInfo(String title, String description, String ownerID, String days, String months,
                    String years, String peopleRequired, String amount) {
        this.title = title;
        this.description = description;
        this.ownerID = ownerID;
        this.days = Integer.parseInt(days);
        this.months = Integer.parseInt(months);
        this.years = Integer.parseInt(years);
        this.peopleRequired = Integer.parseInt(peopleRequired);
        this.amount = Integer.parseInt(amount);
        this.accomplished = this.asked = false;
    }

    @Override
    public boolean equals (Object post) {
        return this.id.equals(((PostInfo)post).getId());
    }

    @NonNull
    @Override
    public String toString() {
        return "PostInfo{" +
                "title='" + title + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public int getYears() {
        return years;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public int getPeopleRequired() {
        return peopleRequired;
    }

    public void setPeopleRequired(int peopleRequired) {
        this.peopleRequired = peopleRequired;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean getAccomplished() {
        return accomplished;
    }

    public void setAccomplished(boolean accomplished) {
        this.accomplished = accomplished;
    }

    public boolean getAsked() { return asked; }

    public void setAsked(boolean asked) { this.asked = asked; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }
}
