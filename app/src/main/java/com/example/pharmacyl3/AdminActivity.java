package com.example.pharmacyl3;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ArrayList<Product> productsList;
    private ProductRecyclerAdapter adapter;
    private DBHelper dbHelper;
    private FloatingActionButton fabAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        rvProducts = findViewById(R.id.rvProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        productsList = dbHelper.getAllProducts();

        // Create adapter with the OnItemClickListener interface from ProductRecyclerAdapter
        adapter = new ProductRecyclerAdapter(productsList, new ProductRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(AdminActivity.this, ProductDetailActivity.class);
                intent.putExtra("name", product.getName());
                intent.putExtra("description", product.getDescription());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("stock", product.getStock());
                startActivity(intent);
            }
        });

        rvProducts.setAdapter(adapter);

        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ProductDetailActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        productsList.clear();
        productsList.addAll(dbHelper.getAllProducts());
        adapter.notifyDataSetChanged();
    }
}
