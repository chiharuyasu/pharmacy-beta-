package com.example.pharmacyl3;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import android.app.AlertDialog;
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
    private TextInputEditText etCategoryFilter;
    private String[] categories;
    private ArrayList<String> selectedCategories = new ArrayList<>();

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
        searchEditText = null; // No search field in layout
        cartItemCount = findViewById(R.id.cartItemCount);
        cartButton = findViewById(R.id.cartButton);
        cartButtonContainer = findViewById(R.id.cartButtonContainer);
        etCategoryFilter = findViewById(R.id.etCategoryFilter);

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

        categories = getResources().getStringArray(R.array.pharmacy_categories);
        String[] allCategories = new String[categories.length + 1];
        allCategories[0] = "All";
        System.arraycopy(categories, 0, allCategories, 1, categories.length);
        selectedCategories.clear();
        selectedCategories.add("All");
        etCategoryFilter.setText("All");
        etCategoryFilter.setOnClickListener(v -> showCategoryMultiSelectDialog(allCategories));
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

    private void filterByCategories(ArrayList<String> categories) {
        filteredList.clear();
        if (categories.contains("All") || categories.isEmpty()) {
            filteredList.addAll(productsList);
        } else {
            for (Product product : productsList) {
                boolean matched = false;
                for (String cat : categories) {
                    if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                        String[] prodCats = product.getCategory().split(",");
                        for (String prodCat : prodCats) {
                            if (prodCat.trim().equalsIgnoreCase(cat.trim())) {
                                matched = true;
                                break;
                            }
                        }
                    }
                    if (matched) break;
                }
                if (matched) {
                    filteredList.add(product);
                }
            }
        }
        adapter.updateProducts(filteredList);
    }

    private void showCategoryMultiSelectDialog(String[] allCategories) {
        boolean[] checked = new boolean[allCategories.length];
        for (int i = 0; i < allCategories.length; i++) {
            checked[i] = selectedCategories.contains(allCategories[i]);
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Categories")
                .setMultiChoiceItems(allCategories, checked, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (allCategories[which].equals("All")) {
                            // If 'All' is checked, uncheck all others
                            selectedCategories.clear();
                            selectedCategories.add("All");
                            // Uncheck all others in the dialog
                            ListView list = ((AlertDialog) dialog).getListView();
                            for (int i = 1; i < allCategories.length; i++) {
                                list.setItemChecked(i, false);
                            }
                        } else {
                            // If any other category is checked, remove 'All' if present
                            selectedCategories.remove("All");
                            if (!selectedCategories.contains(allCategories[which]))
                                selectedCategories.add(allCategories[which]);
                            // Uncheck 'All' in the dialog
                            ListView list = ((AlertDialog) dialog).getListView();
                            list.setItemChecked(0, false);
                        }
                    } else {
                        selectedCategories.remove(allCategories[which]);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    if (selectedCategories.isEmpty()) {
                        selectedCategories.add("All");
                        etCategoryFilter.setText("All");
                        filterByCategories(selectedCategories);
                    } else if (selectedCategories.contains("All")) {
                        etCategoryFilter.setText("All");
                        filterByCategories(selectedCategories);
                    } else {
                        String cats = String.join(", ", selectedCategories);
                        etCategoryFilter.setText(cats);
                        filterByCategories(selectedCategories);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCartBadge() {
        if (cartItems.size() > 0) {
            cartItemCount.setVisibility(View.VISIBLE);
            cartItemCount.setText(String.valueOf(cartItems.size()));
        } else {
            cartItemCount.setVisibility(View.GONE);
        }
    }

    private ArrayList<String> getUniqueCategories(ArrayList<Product> products) {
        ArrayList<String> categories = new ArrayList<>();
        for (Product product : products) {
            String cat = product.getCategory();
            if (cat != null && !cat.isEmpty() && !categories.contains(cat)) {
                categories.add(cat);
            }
        }
        return categories;
    }
}
