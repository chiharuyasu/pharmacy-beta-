package com.example.pharmacyl3;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
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
    private ImageButton cartButton;
    private ArrayList<Product> cartItems;
    private View cartButtonContainer;

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
        cartButton = findViewById(R.id.cartButton);
        cartButtonContainer = findViewById(R.id.cartButtonContainer);

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
                    intent.putExtra("imageUri", product.getImageUri());
                    startActivity(intent);
                }

                @Override
                public void onAddToCart(Product product) {
                    addToCart(product);
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
            if (cartItems.isEmpty()) {
                Snackbar.make(v, "Your cart is empty", Snackbar.LENGTH_SHORT).show();
                return;
            }
            try {
                Intent intent = new Intent(CustomerActivity.this, CartActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("cartItems", cartItems);
                intent.putExtras(bundle);
                startActivity(intent);
            } catch (Exception e) {
                Snackbar.make(v, "Error opening cart", Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    private void addToCart(Product product) {
        cartItems.add(product);
        updateCartBadge();
        
        // Animate cart button
        cartButtonContainer.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.shake_animation));
        
        // Show success message with undo option
        Snackbar.make(findViewById(android.R.id.content), 
                     product.getName() + " added to cart", 
                     Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> {
                    cartItems.remove(cartItems.size() - 1);
                    updateCartBadge();
                })
                .show();
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
