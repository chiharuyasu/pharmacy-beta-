package com.example.pharmacyl3;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationUtils {
    // Thresholds (can be made configurable)
    public static final int LOW_STOCK_THRESHOLD = 5;
    public static final int EXPIRY_DAYS_THRESHOLD = 30;

    // Check for low stock and expiring soon products and insert notifications
    public static void checkAndNotify(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        ArrayList<Product> products = dbHelper.getAllProducts();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        long now = System.currentTimeMillis();
        long expiryThresholdMillis = EXPIRY_DAYS_THRESHOLD * 24L * 60L * 60L * 1000L;

        for (Product p : products) {
            // Low stock
            if (p.getStock() <= LOW_STOCK_THRESHOLD) {
                String msg = "Product '" + p.getName() + "' is low in stock (" + p.getStock() + ")";
                Notification notification = new Notification(
                        "LOW_STOCK",
                        msg,
                        sdf.format(new Date(now)),
                        false
                );
                dbHelper.insertNotification(notification);
            }
            // Expiring soon or expired
            try {
                Date expiry = sdf.parse(p.getExpiryDate());
                if (expiry != null) {
                    long diff = expiry.getTime() - now;
                    if (diff <= expiryThresholdMillis && diff > 0) {
                        String msg = "Product '" + p.getName() + "' is expiring soon (" + p.getExpiryDate() + ")";
                        Notification notification = new Notification(
                                "EXPIRING_SOON",
                                msg,
                                sdf.format(new Date(now)),
                                false
                        );
                        dbHelper.insertNotification(notification);
                    } else if (diff <= 0) {
                        String msg = "Product '" + p.getName() + "' has expired (" + p.getExpiryDate() + ")";
                        Notification notification = new Notification(
                                "EXPIRED",
                                msg,
                                sdf.format(new Date(now)),
                                false
                        );
                        dbHelper.insertNotification(notification);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    // Notify admin when a customer places an order
    public static void notifyOrderPlaced(Context context, String customerName, List<String> productNames, int totalQuantity, String date) {
        String msg = customerName + " bought " + totalQuantity + " products: " + android.text.TextUtils.join(", ", productNames) + " on " + date;
        Notification notification = new Notification(
                "ORDER_PLACED",
                msg,
                date,
                false
        );
        new DBHelper(context).insertNotification(notification);
        // Show order placed snackbar if admin is active
        if (context instanceof com.example.pharmacyl3.AdminActivity) {
            ((com.example.pharmacyl3.AdminActivity) context).showOrderPlacedSnackbar(customerName, totalQuantity);
        }
    }
}
