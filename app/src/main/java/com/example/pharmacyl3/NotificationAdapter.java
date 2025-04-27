package com.example.pharmacyl3;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private ArrayList<Notification> notifications;
    private DBHelper dbHelper;

    public NotificationAdapter(ArrayList<Notification> notifications, DBHelper dbHelper) {
        this.notifications = notifications;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.message.setText(notification.getMessage());
        holder.type.setText(notification.getType());
        holder.timestamp.setText(notification.getTimestamp());
        Context context = holder.itemView.getContext();
        int green = context.getResources().getColor(R.color.primaryGreen);
        holder.message.setTextColor(green);
        holder.type.setTextColor(green);
        holder.itemView.setAlpha(notification.isRead() ? 0.5f : 1.0f);
        holder.itemView.setBackgroundColor(notification.isRead() ? Color.parseColor("#EEEEEE") : Color.WHITE);
        holder.itemView.setOnClickListener(v -> {
            if (!notification.isRead()) {
                dbHelper.markNotificationAsRead(notification.getId());
                notification.setRead(true);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView message, type, timestamp;
        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.text_notification_message);
            type = itemView.findViewById(R.id.text_notification_type);
            timestamp = itemView.findViewById(R.id.text_notification_timestamp);
        }
    }
}
