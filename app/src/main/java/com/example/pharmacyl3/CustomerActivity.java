package com.example.pharmacyl3;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmacyl3.DBHelper;
import com.example.pharmacyl3.Product;
import com.example.pharmacyl3.ProductAdapter;

import java.util.ArrayList;

public class CustomerActivity extends AppCompatActivity {

    private ListView lvProducts;
    private ArrayList<Product> productsList;
    private ProductAdapter adapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer); // Layout with ListView

        lvProducts = findViewById(R.id.lvProducts);
        dbHelper = new DBHelper(this);

        // Retrieve products from the database
        productsList = dbHelper.getAllProducts();
        adapter = new ProductAdapter(this, productsList);
        lvProducts.setAdapter(adapter);

        // When a customer clicks a product, simulate a purchase by reducing stock
        lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Product product = productsList.get(position);
                if(product.getStock() > 0){
                    // Reduce stock by one
                    product.setStock(product.getStock() - 1);
                    dbHelper.updateProduct(product);
                    Toast.makeText(CustomerActivity.this,
                            "Purchased: " + product.getName(), Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CustomerActivity.this,
                            "Sorry, " + product.getName() + " is out of stock", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
