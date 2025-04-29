package com.example.pharmacyl3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class AdminNotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView emptyView;
    private MaterialButton btnMarkAllRead;
    private MaterialButton btnClearAll;
    private LinearLayout buttonContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications);

        recyclerView = findViewById(R.id.recycler_notifications);
        emptyView = findViewById(R.id.text_empty_notifications);
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read);
        btnClearAll = findViewById(R.id.btn_clear_all_notifications);
        buttonContainer = findViewById(R.id.button_container);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notifications = dbHelper.getAllNotifications();
        adapter = new NotificationAdapter(notifications, dbHelper);
        recyclerView.setAdapter(adapter);

        updateUI(notifications.isEmpty());

        btnMarkAllRead.setOnClickListener(v -> {
            dbHelper.markAllNotificationsAsRead();
            notifications.forEach(n -> n.setRead(true));
            adapter.notifyDataSetChanged();
        });

        btnClearAll.setOnClickListener(v -> {
            dbHelper.clearAllNotifications();
            notifications.clear();
            adapter.notifyDataSetChanged();
            updateUI(true);
        });

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
                updateUI(notifications.isEmpty());
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void updateUI(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        buttonContainer.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
