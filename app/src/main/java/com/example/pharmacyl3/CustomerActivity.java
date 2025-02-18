package com.example.pharmacyl3;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CustomerActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ArrayList<Product> productsList;
    private ArrayList<Product> filteredList;
    private ProductRecyclerAdapter adapter;
    private DBHelper dbHelper;
    private TextInputEditText searchEditText;
    private TextView cartItemCount;
    private ArrayList<Product> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        // Initialize cart
        cartItems = new ArrayList<>();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        rvProducts = findViewById(R.id.rvProducts);
        searchEditText = findViewById(R.id.searchEditText);
        cartItemCount = findViewById(R.id.cartItemCount);
        ImageButton cartButton = findViewById(R.id.cartButton);

        // Setup RecyclerView
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setHasFixedSize(true);

        // Initialize database and load products
        dbHelper = new DBHelper(this);
        productsList = dbHelper.getAllProducts();
        filteredList = new ArrayList<>(productsList);

        // Setup adapter
        adapter = new ProductRecyclerAdapter(filteredList, 
            new ProductRecyclerAdapter.ProductInteractionListener() {
                @Override
                public void onItemClick(Product product) {
                    Intent intent = new Intent(CustomerActivity.this, ProductDetailActivity.class);
                    intent.putExtra("name", product.getName());
                    intent.putExtra("description", product.getDescription());
                    intent.putExtra("price", product.getPrice());
                    intent.putExtra("stock", product.getStock());
                    startActivity(intent);
                }

                @Override
                public void onAddToCart(Product product) {
                    cartItems.add(product);
                    updateCartBadge();
                }
            });
        
        rvProducts.setAdapter(adapter);

        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup cart button click
        cartButton.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerActivity.this, CartActivity.class);
            intent.putExtra("cartItems", cartItems);
            startActivity(intent);
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productsList);
        } else {
            String lowercaseQuery = query.toLowerCase();
            filteredList.addAll(productsList.stream()
                    .filter(product -> product.getName().toLowerCase().contains(lowercaseQuery))
                    .collect(Collectors.toList()));
        }
        adapter.notifyDataSetChanged();
    }

    private void updateCartBadge() {
        if (cartItems.size() > 0) {
            cartItemCount.setVisibility(View.VISIBLE);
            cartItemCount.setText(String.valueOf(cartItems.size()));
        } else {
            cartItemCount.setVisibility(View.GONE);
        }
    }
}
