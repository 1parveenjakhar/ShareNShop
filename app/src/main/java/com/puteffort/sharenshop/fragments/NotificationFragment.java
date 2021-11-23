package com.puteffort.sharenshop.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.interfaces.DualPanePostCommunicator;
import com.puteffort.sharenshop.interfaces.NotificationHandler;
import com.puteffort.sharenshop.models.Notification;
import com.puteffort.sharenshop.viewmodels.NotificationFragmentViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationFragmentViewModel model;
    private ProgressBar progressBar;
    private NotificationRecyclerViewAdapter adapter;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        model = new ViewModelProvider(this).get(NotificationFragmentViewModel.class);

        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        setUpComponents();
        addObservers();

        return view;
    }

    private void setUpComponents() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new NotificationRecyclerViewAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);

    }

    private void addObservers() {
        model.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }
            adapter.setNotifications(notifications);
            progressBar.setVisibility(View.GONE);
        });
    }

    private void openPostFragment(int position) {
        Notification notification = Objects.requireNonNull(model.getNotifications().getValue()).get(position);
        if (!notification.markedAsRead) {
            model.updateNotification(position);
            ((NotificationHandler)requireActivity()).reduceUnreadCount();
        }

        PostFragment postFragment = new PostFragment(notification.postID);
        ((DualPanePostCommunicator)requireParentFragment()).openPostFragment(postFragment);
    }

    private static class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Notification> notifications;
        private final Context context;
        private final NotificationFragment notificationFragment;

        public NotificationRecyclerViewAdapter(Context context, NotificationFragment notificationFragment) {
            this.context = context;
            this.notificationFragment = notificationFragment;
            this.notifications = new ArrayList<>();
        }

        void setNotifications(List<Notification> newNotifications) {
            int previousSize = notifications.size();
            notifications.clear();
            notifications.addAll(newNotifications);
            notifyItemRangeRemoved(0, previousSize);
            notifyItemRangeInserted(0, newNotifications.size());
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(context).inflate(R.layout.notification_row, parent, false);
            return new NotificationHolder(root);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Notification notification = notifications.get(position);
            NotificationHolder notificationHolder = (NotificationHolder) holder;

            notificationHolder.title.setText(notification.title);
            notificationHolder.message.setText(notification.message);

            notificationHolder.icon.setVisibility(notification.markedAsRead ? View.GONE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class NotificationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title, message;
            ImageView icon;

            public NotificationHolder(@NonNull View view) {
                super(view);
                view.setOnClickListener(this);

                title = view.findViewById(R.id.notificationTitle);
                message = view.findViewById(R.id.notificationMessage);
                icon = view.findViewById(R.id.unreadIcon);
            }

            @Override
            public void onClick(View v) {
                notificationFragment.openPostFragment(getAdapterPosition());
            }
        }
    }

}