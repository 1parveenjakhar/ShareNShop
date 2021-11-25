package com.puteffort.sharenshop.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.puteffort.sharenshop.services.NotificationDatabase;

import java.io.Serializable;

@Entity(tableName = NotificationDatabase.NOTIFICATION_DB_NAME)
public class Notification implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public long id;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Notification that = (Notification) o;
        return id == that.id;
    }
}
