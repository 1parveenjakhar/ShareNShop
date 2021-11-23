package com.puteffort.sharenshop.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.puteffort.sharenshop.services.NotificationDatabase;

@Entity(tableName = NotificationDatabase.NOTIFICATION_DB_NAME)
public class Notification {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo
    public String title;

    @ColumnInfo
    public String message;

    @ColumnInfo
    public String postID;

    @ColumnInfo
    public boolean markedAsRead;

    public Notification() {

    }

    @Ignore
    public Notification(String title, String message, String postID) {
        this.title = title;
        this.message = message;
        this.postID = postID;
        this.markedAsRead = false;
    }
}
