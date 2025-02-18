package com.example.pharmacyl3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvProductsAdmin;
    private ProductRecyclerAdapter adapter;
    private ArrayList<Product> productsList;
    private DBHelper dbHelper;
    private Button btnAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        // Apply window insets for edge-to-edge layout using the root view with id "main"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DBHelper(this);

        // Initialize views
        rvProductsAdmin = findViewById(R.id.rvProductsAdmin);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        // Load existing products from the database
        productsList = dbHelper.getAllProducts();
        adapter = new ProductRecyclerAdapter(productsList, new ProductRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                // For admin, you might want to open an edit screen or show product details.
                // For now, we'll just show a simple toast.
                // You can update this as needed.
                // Example: Toast.makeText(AdminActivity.this, "Clicked: " + product.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        rvProductsAdmin.setLayoutManager(new LinearLayoutManager(this));
        rvProductsAdmin.setAdapter(adapter);

        // Set up the button to add a new product when clicked
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // For demonstration, create a new product with default values.
                // In a real app, you'd likely show a dialog or a new screen to enter product details.
                Product newProduct = new Product("New Medicine", "Sample description", 20.0, 50);

                // Insert the new product into the database
                dbHelper.insertProduct(newProduct);

                // Refresh the product list by reloading data from the database
                productsList.clear();
                productsList.addAll(dbHelper.getAllProducts());
                adapter.notifyDataSetChanged();
            }
        });
    }
}
