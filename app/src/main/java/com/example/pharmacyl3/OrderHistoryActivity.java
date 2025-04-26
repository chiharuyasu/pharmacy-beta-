package com.example.pharmacyl3;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class OrderHistoryActivity extends AppCompatActivity {
    private RecyclerView rvOrderHistory;
    private OrderHistoryAdapter adapter;
    private ArrayList<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));

        // Fetch orders for the current customer
        int customerId = getIntent().getIntExtra("customerId", -1);
        DBHelper dbHelper = new DBHelper(this);
        orderList = dbHelper.getOrdersForCustomer(customerId);
        if (orderList == null || orderList.isEmpty()) {
            Toast.makeText(this, "No order history found.", Toast.LENGTH_SHORT).show();
            orderList = new ArrayList<>();
        }
        adapter = new OrderHistoryAdapter(orderList);
        rvOrderHistory.setAdapter(adapter);
    }
}
