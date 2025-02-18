package com.example.pharmacyl3;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Retrieve product data from Intent extras
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        double price = getIntent().getDoubleExtra("price", 0.0);
        int stock = getIntent().getIntExtra("stock", 0);

        // Find the TextViews in the layout and set their text
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvStock = findViewById(R.id.tvDetailStock);

        tvName.setText(name);
        tvDescription.setText(description);
        tvPrice.setText("Price: $" + price);
        tvStock.setText("Stock: " + stock);
    }
}
