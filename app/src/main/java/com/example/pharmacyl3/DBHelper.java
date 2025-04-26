package com.example.pharmacyl3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import com.example.pharmacyl3.Product;

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
                "password TEXT)";
        db.execSQL(CREATE_CUSTOMERS_TABLE);
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

    public boolean validateCustomer(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM Customers WHERE email=? AND password=?", new String[]{email, password});
        boolean valid = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return valid;
    }
}
