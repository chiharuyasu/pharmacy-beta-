package com.example.pharmacyl3;

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

        // Get cart items from intent
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                cartItems = (ArrayList<Product>) bundle.getSerializable("cartItems");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            // TODO: Implement checkout process
            Snackbar.make(v, "Proceeding to checkout...", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void updateTotal() {
        double total = 0;
        for (Product product : cartItems) {
            total += product.getPrice();
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