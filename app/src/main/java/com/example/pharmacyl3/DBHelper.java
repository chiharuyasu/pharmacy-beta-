package com.example.pharmacyl3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import com.example.pharmacyl3.Product;
import com.example.pharmacyl3.Order;
import com.example.pharmacyl3.Customer;
import com.example.pharmacyl3.Notification;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "Pharmacy.db";
    private static final int DATABASE_VERSION = 4;

    // Table and column names
    public static final String TABLE_PRODUCTS = "Products";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_STOCK = "stock";
    public static final String COLUMN_EXPIRY_DATE = "expiryDate";
    public static final String COLUMN_MANUFACTURER = "manufacturer";
    public static final String COLUMN_IMAGE_URI = "imageUri";
    public static final String COLUMN_BARCODE = "barcode";
    public static final String COLUMN_CATEGORY = "category";

    // --- ORDERS TABLE ---
    private static final String TABLE_ORDERS = "Orders";
    private static final String COLUMN_ORDER_ID = "id";
    private static final String COLUMN_CUSTOMER_ID = "customerId";
    private static final String COLUMN_ORDER_PRODUCT_NAME = "productName";
    private static final String COLUMN_ORDER_QUANTITY = "quantity";
    private static final String COLUMN_ORDER_TOTAL_PRICE = "totalPrice";
    private static final String COLUMN_ORDER_DATE = "orderDate";

    private static final String CREATE_ORDERS_TABLE = "CREATE TABLE IF NOT EXISTS Orders (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "customerId INTEGER, " +
            "productName TEXT, " +
            "quantity INTEGER, " +
            "totalPrice REAL, " +
            "orderDate TEXT, " +
            "FOREIGN KEY(customerId) REFERENCES Customers(id));";

    // --- CART TABLE ---
    public static final String TABLE_CART = "Cart";
    public static final String COLUMN_CART_ID = "id";
    public static final String COLUMN_CART_CUSTOMER_ID = "customerId";
    public static final String COLUMN_CART_PRODUCT_ID = "productId";
    public static final String COLUMN_CART_QUANTITY = "quantity";
    public static final String COLUMN_CART_ADDED_AT = "addedAt";

    // --- NOTIFICATIONS TABLE ---
    public static final String TABLE_NOTIFICATIONS = "Notifications";
    public static final String COLUMN_NOTIFICATION_ID = "id";
    public static final String COLUMN_NOTIFICATION_TYPE = "type";
    public static final String COLUMN_NOTIFICATION_MESSAGE = "message";
    public static final String COLUMN_NOTIFICATION_TIMESTAMP = "timestamp";
    public static final String COLUMN_NOTIFICATION_IS_READ = "isRead";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_PRICE + " REAL, "
                + COLUMN_STOCK + " INTEGER, "
                + COLUMN_EXPIRY_DATE + " TEXT, "
                + COLUMN_MANUFACTURER + " TEXT, "
                + COLUMN_IMAGE_URI + " TEXT, "
                + COLUMN_BARCODE + " TEXT, "
                + COLUMN_CATEGORY + " TEXT)";
        db.execSQL(CREATE_PRODUCTS_TABLE);
        // Create Customers table
        String CREATE_CUSTOMERS_TABLE = "CREATE TABLE IF NOT EXISTS Customers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "email TEXT UNIQUE, " +
                "password TEXT, " +
                "phone TEXT, " +
                "profilePhotoUri TEXT, " +
                "licenseNumber TEXT, " +
                "pharmacyName TEXT, " +
                "pharmacyAddress TEXT, " +
                "experience TEXT)";
        db.execSQL(CREATE_CUSTOMERS_TABLE);
        db.execSQL(CREATE_ORDERS_TABLE);
        String CREATE_CART_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CART + " ("
                + COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CART_CUSTOMER_ID + " INTEGER, "
                + COLUMN_CART_PRODUCT_ID + " INTEGER, "
                + COLUMN_CART_QUANTITY + " INTEGER, "
                + COLUMN_CART_ADDED_AT + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_CART_CUSTOMER_ID + ") REFERENCES Customers(id), "
                + "FOREIGN KEY(" + COLUMN_CART_PRODUCT_ID + ") REFERENCES Products(id)"
                + ")";
        db.execSQL(CREATE_CART_TABLE);
        String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS + " ("
                + COLUMN_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NOTIFICATION_TYPE + " TEXT, "
                + COLUMN_NOTIFICATION_MESSAGE + " TEXT, "
                + COLUMN_NOTIFICATION_TIMESTAMP + " TEXT, "
                + COLUMN_NOTIFICATION_IS_READ + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
    }

    // Upgrade method (if database version changes)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + COLUMN_IMAGE_URI + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + COLUMN_BARCODE + " TEXT");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + COLUMN_CATEGORY + " TEXT");
            db.execSQL("ALTER TABLE Customers ADD COLUMN phone TEXT");
            db.execSQL("ALTER TABLE Customers ADD COLUMN profilePhotoUri TEXT");
            db.execSQL("ALTER TABLE Customers ADD COLUMN licenseNumber TEXT");
            db.execSQL("ALTER TABLE Customers ADD COLUMN pharmacyName TEXT");
            db.execSQL("ALTER TABLE Customers ADD COLUMN pharmacyAddress TEXT");
            db.execSQL("ALTER TABLE Customers ADD COLUMN experience TEXT");
        }
    }

    // Insert a new product into the database
    public void insertProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_DESCRIPTION, product.getDescription());
        values.put(COLUMN_PRICE, product.getPrice());
        values.put(COLUMN_STOCK, product.getStock());
        values.put(COLUMN_EXPIRY_DATE, product.getExpiryDate());
        values.put(COLUMN_MANUFACTURER, product.getManufacturer());
        values.put(COLUMN_IMAGE_URI, product.getImageUri());
        values.put(COLUMN_BARCODE, product.getBarcode());
        values.put(COLUMN_CATEGORY, product.getCategory());
        db.insert(TABLE_PRODUCTS, null, values);
        db.close();
    }

    // Update an existing product
    public void updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_DESCRIPTION, product.getDescription());
        values.put(COLUMN_PRICE, product.getPrice());
        values.put(COLUMN_STOCK, product.getStock());
        values.put(COLUMN_EXPIRY_DATE, product.getExpiryDate());
        values.put(COLUMN_MANUFACTURER, product.getManufacturer());
        values.put(COLUMN_IMAGE_URI, product.getImageUri());
        values.put(COLUMN_BARCODE, product.getBarcode());
        values.put(COLUMN_CATEGORY, product.getCategory());
        db.update(TABLE_PRODUCTS, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(product.getId())});
        db.close();
    }

    // Delete a product by ID
    public void deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCTS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Retrieve all products from the database
    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOCK));
                String expiryDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE));
                String manufacturer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
                String barcode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BARCODE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                products.add(new Product(id, name, description, price, stock, expiryDate, manufacturer, imageUri, barcode, category));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }

    // Retrieve a product by barcode
    public Product getProductByBarcode(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, COLUMN_BARCODE + "=?", new String[]{barcode}, null, null, null);
        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
            int stock = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOCK));
            String expiryDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE));
            String manufacturer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
            String barcodeValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BARCODE));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
            product = new Product(id, name, description, price, stock, expiryDate, manufacturer, imageUri, barcodeValue, category);
        }
        if (cursor != null) cursor.close();
        db.close();
        return product;
    }

    // Retrieve a product by name (case-insensitive) when no barcode is provided
    public Product getProductByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, 
                COLUMN_NAME + " COLLATE NOCASE =? AND (" + COLUMN_BARCODE + " IS NULL OR " + COLUMN_BARCODE + " = '')", 
                new String[]{name}, null, null, null);
        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
            int stock = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOCK));
            String expiryDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE));
            String manufacturer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
            String barcodeValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BARCODE));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
            product = new Product(id, productName, description, price, stock, expiryDate, manufacturer, imageUri, barcodeValue, category);
        }
        if (cursor != null) cursor.close();
        db.close();
        return product;
    }

    // Retrieve a product by id
    public Product getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
            int stock = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOCK));
            String expiryDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE));
            String manufacturer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
            String barcodeValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BARCODE));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
            product = new Product(id, name, description, price, stock, expiryDate, manufacturer, imageUri, barcodeValue, category);
        }
        if (cursor != null) cursor.close();
        db.close();
        return product;
    }

    // --- CUSTOMER ACCOUNT METHODS ---
    public boolean customerExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM Customers WHERE email=?", new String[]{email});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    public boolean insertCustomer(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        long result = db.insert("Customers", null, values);
        db.close();
        return result != -1;
    }

    public boolean insertCustomer(String name, String email, String password, String phone, String profilePhotoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        values.put("phone", phone);
        values.put("profilePhotoUri", profilePhotoUri);
        long result = db.insert("Customers", null, values);
        db.close();
        return result != -1;
    }

    public boolean validateCustomer(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM Customers WHERE email=? AND password=?", new String[]{email, password});
        boolean valid = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return valid;
    }

    // Fetch customer by email (for profile info)
    public Customer getCustomerByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Customers WHERE email=?", new String[]{email});
        Customer customer = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String phone = cursor.getColumnIndexOrThrow("phone") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("phone")) : "";
            String profilePhotoUri = cursor.getColumnIndexOrThrow("profilePhotoUri") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("profilePhotoUri")) : null;
            String licenseNumber = cursor.getColumnIndexOrThrow("licenseNumber") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("licenseNumber")) : null;
            String pharmacyName = cursor.getColumnIndexOrThrow("pharmacyName") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("pharmacyName")) : null;
            String pharmacyAddress = cursor.getColumnIndexOrThrow("pharmacyAddress") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("pharmacyAddress")) : null;
            String experience = cursor.getColumnIndexOrThrow("experience") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("experience")) : null;
            customer = new Customer(id, name, email, phone, profilePhotoUri, licenseNumber, pharmacyName, pharmacyAddress, experience);
        }
        cursor.close();
        db.close();
        return customer;
    }

    // Fetch customer by id (for profile info)
    public Customer getCustomerById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Customers WHERE id=?", new String[]{String.valueOf(id)});
        Customer customer = null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String phone = cursor.getColumnIndexOrThrow("phone") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("phone")) : "";
            String profilePhotoUri = cursor.getColumnIndexOrThrow("profilePhotoUri") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("profilePhotoUri")) : null;
            String licenseNumber = cursor.getColumnIndexOrThrow("licenseNumber") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("licenseNumber")) : null;
            String pharmacyName = cursor.getColumnIndexOrThrow("pharmacyName") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("pharmacyName")) : null;
            String pharmacyAddress = cursor.getColumnIndexOrThrow("pharmacyAddress") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("pharmacyAddress")) : null;
            String experience = cursor.getColumnIndexOrThrow("experience") >= 0 ? cursor.getString(cursor.getColumnIndexOrThrow("experience")) : null;
            customer = new Customer(id, name, email, phone, profilePhotoUri, licenseNumber, pharmacyName, pharmacyAddress, experience);
        }
        cursor.close();
        db.close();
        return customer;
    }

    // Update customer profile photo URI
    public void updateCustomerProfilePhoto(String email, String photoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("profilePhotoUri", photoUri);
        db.update("Customers", values, "email=?", new String[]{email});
        db.close();
    }

    // Update customer profile by id
    public void updateCustomerProfile(int id, String name, String phone, String profilePhotoUri, String licenseNumber, String pharmacyName, String pharmacyAddress, String experience, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        values.put("profilePhotoUri", profilePhotoUri);
        values.put("licenseNumber", licenseNumber);
        values.put("pharmacyName", pharmacyName);
        values.put("pharmacyAddress", pharmacyAddress);
        values.put("experience", experience);
        values.put("email", email);
        db.update("Customers", values, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // --- ORDER HISTORY METHODS ---
    public ArrayList<Order> getOrdersForCustomer(int customerId) {
        ArrayList<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Orders WHERE customerId=? ORDER BY orderDate DESC", new String[]{String.valueOf(customerId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID));
                String productName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_PRODUCT_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_QUANTITY));
                double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_TOTAL_PRICE));
                String orderDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE));
                orders.add(new Order(id, customerId, productName, quantity, totalPrice, orderDate));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return orders;
    }

    public boolean insertOrder(int customerId, String productName, int quantity, double totalPrice, String orderDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_ID, customerId);
        values.put(COLUMN_ORDER_PRODUCT_NAME, productName);
        values.put(COLUMN_ORDER_QUANTITY, quantity);
        values.put(COLUMN_ORDER_TOTAL_PRICE, totalPrice);
        values.put(COLUMN_ORDER_DATE, orderDate);
        long result = db.insert(TABLE_ORDERS, null, values);
        db.close();
        return result != -1;
    }

    // Clear all orders for a customer
    public void clearOrderHistoryForCustomer(int customerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ORDERS, COLUMN_CUSTOMER_ID + "=?", new String[]{String.valueOf(customerId)});
        db.close();
    }

    // --- CART METHODS ---
    public void addOrUpdateCartItem(int customerId, int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CART + " WHERE " + COLUMN_CART_CUSTOMER_ID + "=? AND " + COLUMN_CART_PRODUCT_ID + "=?", new String[]{String.valueOf(customerId), String.valueOf(productId)});
        if (cursor.moveToFirst()) {
            db.execSQL("UPDATE " + TABLE_CART + " SET " + COLUMN_CART_QUANTITY + "=? WHERE " + COLUMN_CART_CUSTOMER_ID + "=? AND " + COLUMN_CART_PRODUCT_ID + "=?", new Object[]{quantity, customerId, productId});
        } else {
            db.execSQL("INSERT INTO " + TABLE_CART + " (" + COLUMN_CART_CUSTOMER_ID + ", " + COLUMN_CART_PRODUCT_ID + ", " + COLUMN_CART_QUANTITY + ", " + COLUMN_CART_ADDED_AT + ") VALUES (?, ?, ?, datetime('now'))", new Object[]{customerId, productId, quantity});
        }
        cursor.close();
        db.close();
    }

    public void removeCartItem(int customerId, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COLUMN_CART_CUSTOMER_ID + "=? AND " + COLUMN_CART_PRODUCT_ID + "=?", new String[]{String.valueOf(customerId), String.valueOf(productId)});
        db.close();
    }

    public void clearCart(int customerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COLUMN_CART_CUSTOMER_ID + "=?", new String[]{String.valueOf(customerId)});
        db.close();
    }

    public ArrayList<Product> getCartItems(int customerId) {
        ArrayList<Product> cartProducts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT p.* , c." + COLUMN_CART_QUANTITY + " FROM " + TABLE_CART + " c JOIN " + TABLE_PRODUCTS + " p ON c." + COLUMN_CART_PRODUCT_ID + " = p." + COLUMN_ID + " WHERE c." + COLUMN_CART_CUSTOMER_ID + "=?", new String[]{String.valueOf(customerId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOCK));
                String expiryDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE));
                String manufacturer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
                String barcode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BARCODE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_QUANTITY));
                Product product = new Product(id, name, description, price, stock, expiryDate, manufacturer, imageUri, barcode, category);
                product.setQuantity(quantity); // Make sure Product has setQuantity()
                cartProducts.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return cartProducts;
    }

    // Insert a new notification
    public void insertNotification(Notification notification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_TYPE, notification.getType());
        values.put(COLUMN_NOTIFICATION_MESSAGE, notification.getMessage());
        values.put(COLUMN_NOTIFICATION_TIMESTAMP, notification.getTimestamp());
        values.put(COLUMN_NOTIFICATION_IS_READ, notification.isRead() ? 1 : 0);
        db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
    }

    // Get all notifications (most recent first)
    public ArrayList<Notification> getAllNotifications() {
        ArrayList<Notification> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATIONS + " ORDER BY " + COLUMN_NOTIFICATION_TIMESTAMP + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_ID));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TYPE));
                String message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_MESSAGE));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TIMESTAMP));
                boolean isRead = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_IS_READ)) == 1;
                notifications.add(new Notification(id, type, message, timestamp, isRead));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notifications;
    }

    // Mark a notification as read
    public void markNotificationAsRead(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_IS_READ, 1);
        db.update(TABLE_NOTIFICATIONS, values, COLUMN_NOTIFICATION_ID + "=?", new String[]{String.valueOf(notificationId)});
        db.close();
    }

    // Mark all notifications as read
    public void markAllNotificationsAsRead() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_IS_READ, 1);
        db.update(TABLE_NOTIFICATIONS, values, null, null);
        db.close();
    }

    // Clear all notifications
    public void clearAllNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTIFICATIONS, null, null);
        db.close();
    }
}
