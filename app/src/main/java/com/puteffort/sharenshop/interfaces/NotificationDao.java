package com.puteffort.sharenshop.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.puteffort.sharenshop.models.Notification;
import com.puteffort.sharenshop.services.NotificationDatabase;

import java.util.List;

@Dao
public interface NotificationDao {
    @Query("Select * from " + NotificationDatabase.NOTIFICATION_DB_NAME)
    List<Notification> getAllNotifications();

    @Insert
    long addNotification(Notification notification);

    @Update
    void updateNotification(Notification notification);

    @Query("DELETE from " + NotificationDatabase.NOTIFICATION_DB_NAME)
    void deleteAll();
}
