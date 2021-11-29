package com.puteffort.sharenshop.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.interfaces.DualPanePostCommunicator;
import com.puteffort.sharenshop.models.Notification;
import com.puteffort.sharenshop.viewmodels.NotificationFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationFragmentViewModel model;
    private ProgressBar progressBar;
    private Button markAllAsRead;
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
        markAllAsRead = view.findViewById(R.id.markAllAsRead);

        progressBar.setVisibility(View.GONE);

        setUpComponents();
        addObservers();

        return view;
    }

    private void setUpComponents() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new NotificationRecyclerViewAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);

        markAllAsRead.setOnClickListener(view -> model.markAllAsRead());
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
        Notification notification = adapter.getNotification(position);
        if (!notification.markedAsRead) {
            model.markNotificationAsRead(position);
        }
        ((DualPanePostCommunicator)requireParentFragment()).openPostFragment(notification.postID);
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
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NotificationDiffCallback(notifications, newNotifications));
            notifications.clear();
            notifications.addAll(newNotifications);
            diffResult.dispatchUpdatesTo(this);
        }

        Notification getNotification(int index) {
            return notifications.get(index);
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

        private static class NotificationDiffCallback extends DiffUtil.Callback {
            private final List<Notification> newList, oldList;

            public NotificationDiffCallback(List<Notification> oldList,
                                    List<Notification> newList) {
                this.oldList = oldList;
                this.newList = newList;
            }

            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).id == newList.get(newItemPosition).id;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Notification oldItem = oldList.get(oldItemPosition);
                Notification newItem = newList.get(newItemPosition);
                return oldItem.title.equals(newItem.title)
                        && oldItem.message.equals(newItem.message)
                        && oldItem.markedAsRead == newItem.markedAsRead
                        && oldItem.postID.equals(newItem.postID);
            }
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
                icon.setVisibility(View.GONE);
                notificationFragment.openPostFragment(getAdapterPosition());
            }
        }
    }

}