package com.example.pharmacyl3;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Color;
import java.util.ArrayList;

public class AdminNotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView emptyView;
    private Button markAllAsReadButton;
    private Button clearAllButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications);

        recyclerView = findViewById(R.id.recycler_notifications);
        emptyView = findViewById(R.id.text_empty_notifications);
        markAllAsReadButton = findViewById(R.id.btn_mark_all_read);
        clearAllButton = findViewById(R.id.btn_clear_all_notifications);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notifications = dbHelper.getAllNotifications();
        adapter = new NotificationAdapter(notifications, dbHelper);
        recyclerView.setAdapter(adapter);

        if (notifications.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            markAllAsReadButton.setVisibility(View.GONE);
            clearAllButton.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            markAllAsReadButton.setVisibility(View.VISIBLE);
            clearAllButton.setVisibility(View.VISIBLE);
        }

        markAllAsReadButton.setOnClickListener(v -> {
            dbHelper.markAllNotificationsAsRead();
            for (Notification n : notifications) n.setRead(true);
            adapter.notifyDataSetChanged();
        });

        clearAllButton.setOnClickListener(v -> {
            dbHelper.clearAllNotifications();
            notifications.clear();
            adapter.notifyDataSetChanged();
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            markAllAsReadButton.setVisibility(View.GONE);
            clearAllButton.setVisibility(View.GONE);
        });

        // Swipe to delete
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Notification notif = notifications.get(pos);
                dbHelper.getWritableDatabase().delete(DBHelper.TABLE_NOTIFICATIONS, DBHelper.COLUMN_NOTIFICATION_ID + "=?", new String[]{String.valueOf(notif.getId())});
                notifications.remove(pos);
                adapter.notifyItemRemoved(pos);
                if (notifications.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    markAllAsReadButton.setVisibility(View.GONE);
                    clearAllButton.setVisibility(View.GONE);
                }
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }
}
