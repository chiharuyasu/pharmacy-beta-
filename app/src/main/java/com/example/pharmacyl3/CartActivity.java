package com.example.pharmacyl3;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private TextView tvTotalAmount;
    private MaterialButton btnCheckout;
    private ArrayList<Product> cartItems;
    private CartAdapter adapter;
    private int customerId;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnCheckout = findViewById(R.id.btnCheckout);

        // Get customerId from intent
        customerId = getIntent().getIntExtra("customerId", -1);
        dbHelper = new DBHelper(this);

        // Load cart items from DB
        cartItems = dbHelper.getCartItems(customerId);

        if (cartItems == null) {
            cartItems = new ArrayList<>();
            Snackbar.make(findViewById(android.R.id.content), 
                         "Error loading cart items", 
                         Snackbar.LENGTH_SHORT).show();
        }

        // Setup RecyclerView
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cartItems, new CartAdapter.CartItemListener() {
            @Override
            public void onRemoveItem(int position) {
                Product product = cartItems.get(position);
                dbHelper.removeCartItem(customerId, product.getId());
                cartItems.remove(position);
                adapter.notifyItemRemoved(position);
                updateTotal();
            }
        });
        rvCartItems.setAdapter(adapter);

        // Update total
        updateTotal();

        // Setup checkout button
        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Snackbar.make(v, "Your cart is empty", Snackbar.LENGTH_SHORT).show();
                return;
            }
            // Check stock availability before placing order
            for (Product product : cartItems) {
                if (product.getQuantity() > product.getStock()) {
                    Snackbar.make(rvCartItems,
                        "Only " + product.getStock() + " items available for " + product.getName(),
                        Snackbar.LENGTH_LONG).show();
                    return;
                }
            }
            // Insert each cart item as an order
            int totalQuantity = 0;
            ArrayList<String> productNames = new ArrayList<>();
            for (Product product : cartItems) {
                dbHelper.insertOrder(
                    customerId,
                    product.getName(),
                    product.getQuantity(),
                    product.getPrice() * product.getQuantity(),
                    String.valueOf(System.currentTimeMillis())
                );
                // Decrease product stock in DB
                int newStock = product.getStock() - product.getQuantity();
                if (newStock < 0) newStock = 0;
                product.setStock(newStock);
                dbHelper.updateProduct(product);
                totalQuantity += product.getQuantity();
                productNames.add(product.getName());
            }
            // Fetch customer name for notification
            Customer customer = dbHelper.getCustomerById(customerId);
            String customerName = customer != null ? customer.name : "Unknown Customer";
            // Format date
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
            String dateStr = sdf.format(new java.util.Date());
            // Notify admin about the order
            NotificationUtils.notifyOrderPlaced(
                CartActivity.this,
                customerName,
                productNames,
                totalQuantity,
                dateStr
            );
            dbHelper.clearCart(customerId);
            cartItems.clear();
            adapter.notifyDataSetChanged();
            updateTotal();
            Snackbar.make(v, "Order placed! Cart cleared.", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void updateTotal() {
        double total = 0;
        for (Product product : cartItems) {
            total += product.getPrice() * product.getQuantity();
        }
        tvTotalAmount.setText(String.format("$%.2f", total));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 