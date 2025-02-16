package com.example.pharmacyl3;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmacyl3.DBHelper;
import com.example.pharmacyl3.Product;
import com.example.pharmacyl3.ProductAdapter;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private ListView lvProductsAdmin;
    private ArrayList<Product> productsList;
    private ProductAdapter adapter;
    private DBHelper dbHelper;
    private Button btnAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        lvProductsAdmin = findViewById(R.id.lvProductsAdmin);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        dbHelper = new DBHelper(this);

        // Load products from the database
        productsList = dbHelper.getAllProducts();
        adapter = new ProductAdapter(this, productsList);
        lvProductsAdmin.setAdapter(adapter);

        // Button to add a new product (for demonstration, a hardcoded product)
        btnAddProduct.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Product newProduct = new Product("Medicine A", "For headaches", 15.0, 50);
                dbHelper.insertProduct(newProduct);
                refreshProducts();
                Toast.makeText(AdminActivity.this, "Product added", Toast.LENGTH_SHORT).show();
            }
        });

        // Long click on a product to delete it
        lvProductsAdmin.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                Product product = productsList.get(position);
                dbHelper.deleteProduct(product.getId());
                refreshProducts();
                Toast.makeText(AdminActivity.this, "Deleted " + product.getName(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    // Helper method to refresh the product list
    private void refreshProducts() {
        productsList.clear();
        productsList.addAll(dbHelper.getAllProducts());
        adapter.notifyDataSetChanged();
    }
}
