package com.example.pharmacyl3;

import com.example.pharmacyl3.Product;
import com.example.pharmacyl3.DBHelper;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {
    private RecyclerView rvDashboardProducts;
    private AdminProductAdapter adapter;
    private DBHelper dbHelper;
    private ArrayList<Product> productsList;
    private TextView tvTotalProducts, tvTotalMoney, tvLowStock, tvExpiringSoon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        rvDashboardProducts = findViewById(R.id.rvDashboardProducts);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvTotalMoney = findViewById(R.id.tvTotalMoney);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvExpiringSoon = findViewById(R.id.tvExpiringSoon);

        rvDashboardProducts.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new DBHelper(this);
        productsList = dbHelper.getAllProducts();
        adapter = new AdminProductAdapter(productsList, null);
        rvDashboardProducts.setAdapter(adapter);

        updateDashboardInfo();
    }

    private void updateDashboardInfo() {
        int totalProducts = productsList.size();
        tvTotalProducts.setText("Total Products: " + totalProducts);

        double totalMoney = 0.0;
        for (Product p : productsList) {
            totalMoney += p.getPrice() * p.getStock();
        }
        tvTotalMoney.setText(String.format("Total Money: DZD %.2f", totalMoney));

        ArrayList<String> lowStockNames = new ArrayList<>();
        for (Product p : productsList) {
            if (p.getStock() < 5) {
                lowStockNames.add(p.getName());
            }
        }
        tvLowStock.setText("Low Stock Products: " + (lowStockNames.isEmpty() ? "None" : joinNames(lowStockNames)));

        ArrayList<String> expiringNames = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        Date oneMonthLater = cal.getTime();
        Date today = new Date();
        for (Product p : productsList) {
            try {
                Date expiry = sdf.parse(p.getExpiryDate());
                if (expiry != null && expiry.after(today) && expiry.before(oneMonthLater)) {
                    expiringNames.add(p.getName());
                }
            } catch (ParseException e) {
                // Ignore invalid date
            }
        }
        tvExpiringSoon.setText("Expiring Soon: " + (expiringNames.isEmpty() ? "None" : joinNames(expiringNames)));
    }

    private String joinNames(ArrayList<String> names) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            sb.append(names.get(i));
            if (i < names.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
