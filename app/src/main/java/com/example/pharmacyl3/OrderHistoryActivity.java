package com.example.pharmacyl3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class OrderHistoryActivity extends AppCompatActivity {
    private RecyclerView rvOrderHistory;
    private OrderHistoryAdapter adapter;
    private ArrayList<Order> orderList;
    private Button clearOrderHistoryButton;
    private int customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        clearOrderHistoryButton = findViewById(R.id.btn_clear_order_history);

        // Fetch orders for the current customer
        customerId = getIntent().getIntExtra("customerId", -1);
        DBHelper dbHelper = new DBHelper(this);
        orderList = dbHelper.getOrdersForCustomer(customerId);
        if (orderList == null || orderList.isEmpty()) {
            Toast.makeText(this, "No order history found.", Toast.LENGTH_SHORT).show();
            orderList = new ArrayList<>();
            clearOrderHistoryButton.setVisibility(View.GONE);
        } else {
            clearOrderHistoryButton.setVisibility(View.VISIBLE);
        }
        adapter = new OrderHistoryAdapter(orderList);
        rvOrderHistory.setAdapter(adapter);

        clearOrderHistoryButton.setOnClickListener(v -> {
            dbHelper.clearOrderHistoryForCustomer(customerId);
            orderList.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Order history cleared.", Toast.LENGTH_SHORT).show();
            // Do NOT hide the button
            // clearOrderHistoryButton.setVisibility(View.GONE);
        });
    }
}
