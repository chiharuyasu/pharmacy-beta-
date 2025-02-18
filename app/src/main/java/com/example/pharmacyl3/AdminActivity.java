package com.example.pharmacyl3;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ArrayList<Product> productsList;
    private AdminProductAdapter adapter;
    private DBHelper dbHelper;
    private FloatingActionButton fabAddProduct;
    private TextInputEditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize views
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchFunctionality();

        // Setup FAB for adding new products
        fabAddProduct.setOnClickListener(v -> showAddProductDialog());
    }

    private void initializeViews() {
        rvProducts = findViewById(R.id.rvProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        searchEditText = findViewById(R.id.searchEditText);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new DBHelper(this);
        productsList = dbHelper.getAllProducts();

        adapter = new AdminProductAdapter(productsList, new AdminProductAdapter.AdminProductListener() {
            @Override
            public void onEditClick(Product product) {
                showEditProductDialog(product);
            }

            @Override
            public void onDeleteClick(Product product) {
                showDeleteConfirmationDialog(product);
            }

            @Override
            public void onItemClick(Product product) {
                showProductDetails(product);
            }
        });

        rvProducts.setAdapter(adapter);
    }

    private void setupSearchFunctionality() {
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
    }

    private void filterProducts(String query) {
        ArrayList<Product> filteredList = new ArrayList<>();
        for (Product product : productsList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        adapter.updateProducts(filteredList);
    }

    private void showAddProductDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etProductDescription);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etStock = dialogView.findViewById(R.id.etProductStock);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add New Product")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    try {
                        String name = etName.getText().toString();
                        String description = etDescription.getText().toString();
                        double price = Double.parseDouble(etPrice.getText().toString());
                        int stock = Integer.parseInt(etStock.getText().toString());

                        Product newProduct = new Product(name, description, price, stock);
                        dbHelper.insertProduct(newProduct);
                        refreshData();
                        showSnackbar("Product added successfully");
                    } catch (Exception e) {
                        showSnackbar("Error adding product. Please check your inputs.");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditProductDialog(Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etProductDescription);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etStock = dialogView.findViewById(R.id.etProductStock);

        // Pre-fill current values
        etName.setText(product.getName());
        etDescription.setText(product.getDescription());
        etPrice.setText(String.valueOf(product.getPrice()));
        etStock.setText(String.valueOf(product.getStock()));

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Product")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        Product updatedProduct = new Product(
                            product.getId(),
                            etName.getText().toString(),
                            etDescription.getText().toString(),
                            Double.parseDouble(etPrice.getText().toString()),
                            Integer.parseInt(etStock.getText().toString())
                        );
                        dbHelper.updateProduct(updatedProduct);
                        refreshData();
                        showSnackbar("Product updated successfully");
                    } catch (Exception e) {
                        showSnackbar("Error updating product. Please check your inputs.");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmationDialog(Product product) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteProduct(product.getId());
                    refreshData();
                    showSnackbar("Product deleted");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showProductDetails(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("name", product.getName());
        intent.putExtra("description", product.getDescription());
        intent.putExtra("price", product.getPrice());
        intent.putExtra("stock", product.getStock());
        startActivity(intent);
    }

    private void refreshData() {
        productsList.clear();
        productsList.addAll(dbHelper.getAllProducts());
        adapter.notifyDataSetChanged();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }
}
