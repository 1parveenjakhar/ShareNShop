package com.puteffort.sharenshop.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.Notification;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

public class NotificationRecyclerView extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ArrayList<Notification> notifications = new ArrayList<>();
        for(int i = 0; i < 50; i++) {
            notifications.add(new Notification("Netflix Subscription","Hey!, Congratulations, Your netflix subscription request as has been accepted. You can now contact the owner :). Happy sharing!!"));
        }

        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.notificationRecycleView);

        NotificationRecyclerViewAdapter notificationRecyclerViewAdapter = new NotificationRecyclerViewAdapter(notifications);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(notificationRecyclerViewAdapter);

        return view;
    }

}

class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.NotificationViewHolder> {

    private final List<Notification> notifications;

    NotificationRecyclerViewAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_row, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {

        final Notification notification = notifications.get(position);
        holder.productName.setText(notification.getProductName());
        holder.notifDescription.setText(notification.getNotifDescription());

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        public TextView productName, notifDescription;
        private final LinearLayout notificationLayout;

        public NotificationViewHolder(View view) {
            super(view);
            productName = view.findViewById(R.id.productName);
            notifDescription = view.findViewById(R.id.notifDescription);
            notificationLayout = view.findViewById(R.id.notificationLayout);
        }
    }
}
