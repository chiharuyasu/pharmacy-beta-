package com.example.pharmacyl3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.pharmacyl3.utils.NotificationHelper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ArrayList<Product> productsList;
    private AdminProductAdapter adapter;
    private DBHelper dbHelper;
    private FloatingActionButton fabAddProduct;
    private TextInputEditText searchEditText;
    private DrawerLayout admindrawer;
    private NavigationView navView;
    private static final String TAG = "AdminActivity"; // Added missing constant
    private static final String IMAGES_DIR = "images";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_BARCODE_SCAN = 2002;
    private static final int REQUEST_IMPORT_ZIP = 1001;
    private static final int REQUEST_IMPORT_CSV = 3001;
    private static final int REQUEST_EXPORT_CSV = 3002;
    private static final int REQUEST_IMPORT_EXCEL = 4001;
    private static final int REQUEST_EXPORT_EXCEL = 4002;
    private static final int STORAGE_PERMISSION_CODE = 10001;
    private Uri selectedImageUri = null;
    private AlertDialog addProductDialog;
    private ImageView addDialogImageView;
    private AlertDialog editProductDialog;
    private ImageView editDialogImageView;
    private String pendingBarcode = null;
    private AdminProductViewModel productViewModel;
    private TextInputEditText etAdminCategoryFilter;
    private String[] categories;
    private ArrayList<String> selectedCategories = new ArrayList<>();
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        NotificationUtils.checkAndNotify(this);
        initializeViews();
        setupToolbar();
        setupDrawer();
        setupRecyclerView();
        setupSearchFunctionality();
        setupCategoryFilter();
        fabAddProduct.setOnClickListener(v -> showAddProductDialog(null));
        updateProfileHeader();

        etAdminCategoryFilter = findViewById(R.id.etAdminCategoryFilter);
        categories = getResources().getStringArray(R.array.pharmacy_categories);
        String[] allCategories = new String[categories.length + 1];
        allCategories[0] = "All";
        System.arraycopy(categories, 0, allCategories, 1, categories.length);
        selectedCategories.clear();
        selectedCategories.add("All");
        etAdminCategoryFilter.setText("All");
        etAdminCategoryFilter.setOnClickListener(v -> showCategoryMultiSelectDialog(allCategories));

        // --- LiveData & ViewModel integration ---
        productViewModel = new ViewModelProvider(this).get(AdminProductViewModel.class);
        productViewModel.getProductsLiveData().observe(this, new Observer<ArrayList<Product>>() {
            @Override
            public void onChanged(ArrayList<Product> products) {
                productsList = products;
                if (adapter != null) {
                    adapter.updateProducts(products);
                }
                // Show low stock snackbar if any
                showLowStockSnackbarIfNeeded(products);
            }
        });
        // Initial load
        refreshData();
    }

    private void updateProfileHeader() {
        // Update the navigation header with user information
        try {
            View headerView = navView.getHeaderView(0);
            if (headerView != null) {
                // Try to find and update user name
                try {
                    TextView tvUserName = headerView.findViewById(getResources().getIdentifier("tvUserName", "id", getPackageName()));
                    if (tvUserName != null) {
                        tvUserName.setText("Admin User");
                    }
                } catch (Exception e) {
                    // View not found, ignore
                }
                
                // Try to find and update user email
                try {
                    TextView tvUserEmail = headerView.findViewById(getResources().getIdentifier("tvUserEmail", "id", getPackageName()));
                    if (tvUserEmail != null) {
                        tvUserEmail.setText("admin@pharmacy.com");
                    }
                } catch (Exception e) {
                    // View not found, ignore
                }
                
                // Try to find and update profile image
                try {
                    ImageView imgProfile = headerView.findViewById(getResources().getIdentifier("imgProfile", "id", getPackageName()));
                    // You can load profile image here if needed
                } catch (Exception e) {
                    // View not found, ignore
                }
            }
        } catch (Exception e) {
            // Header view not found or other error, ignore
        }
    }

    private void launchBarcodeScanner() {
        // Launch barcode scanner activity
        try {
            Intent intent = new Intent(AdminActivity.this, BarcodeScannerActivity.class);
            startActivityForResult(intent, REQUEST_BARCODE_SCAN);
        } catch (Exception e) {
            Toast.makeText(this, "Barcode scanner not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        rvProducts = findViewById(R.id.rvProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        searchEditText = findViewById(R.id.searchEditText);
        admindrawer = findViewById(R.id.admindrawer);
        navView = findViewById(R.id.nav_view);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_menu_hamburger);
        toolbar.setNavigationOnClickListener(v -> admindrawer.openDrawer(GravityCompat.START));
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Handle Home
            } else if (id == R.id.nav_edit_profile) {
                Intent intent = new Intent(AdminActivity.this, AdminEditProfileActivity.class);
                startActivityForResult(intent, 2001);
            } else if (id == R.id.nav_products) {
                // Handle Products
            } else if (id == R.id.nav_dashboard) {
                // Show dashboard activity
                Intent intent = new Intent(AdminActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_barcode_scanner) {
                launchBarcodeScanner();
            } else if (id == R.id.nav_import_products) {
                showImportFormatDialog();
            } else if (id == R.id.nav_export_products) {
                showExportFormatDialog();
            } else if (id == R.id.nav_notifications) {
                Intent intent = new Intent(AdminActivity.this, AdminNotificationsActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                // Handle Logout
                finish();
            }
            admindrawer.closeDrawer(GravityCompat.START);
            return true;
        });
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
                currentSearchQuery = s.toString();
                applyCombinedFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryFilter() {
        // Remove all content from this method, as we are not using AutoCompleteTextView or ChipGroup for admin filtering yet
    }

    private void applyCombinedFilters() {
        ArrayList<Product> filtered = new ArrayList<>();
        for (Product product : productsList) {
            boolean matchesCategory = selectedCategories.contains("All");
            if (!matchesCategory && product.getCategory() != null) {
                for (String cat : selectedCategories) {
                    if (product.getCategory().contains(cat)) {
                        matchesCategory = true;
                        break;
                    }
                }
            }
            boolean matchesQuery = product.getName().toLowerCase().contains(currentSearchQuery.toLowerCase());
            if (matchesCategory && matchesQuery) {
                filtered.add(product);
            }
        }
        adapter.updateProducts(filtered);
    }

    private void showAddProductDialog(String barcode) {
        // Check for duplicate barcode
        if (barcode != null) {
            Product existingProduct = dbHelper.getProductByBarcode(barcode);
            if (existingProduct != null) {
                // Show warning and product details
                new AlertDialog.Builder(this)
                        .setTitle("Duplicate Barcode")
                        .setMessage("A product with this barcode already exists. Would you like to view or edit it?")
                        .setPositiveButton("View/Edit", (dialog, which) -> showEditProductDialog(existingProduct))
                        .setNegativeButton("Cancel", null)
                        .show();
                return;
            }
        }
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etProductDescription);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etStock = dialogView.findViewById(R.id.etProductStock);
        TextInputEditText etExpiryDate = dialogView.findViewById(R.id.etProductExpiryDate);
        TextInputEditText etManufacturer = dialogView.findViewById(R.id.etProductManufacturer);
        TextInputEditText etBarcode = dialogView.findViewById(R.id.etProductBarcode);
        TextInputEditText etCategory = dialogView.findViewById(R.id.etProductCategory);
        etCategory.setOnClickListener(v -> showCategoryMultiSelectDialog(etCategory));
        ImageView ivProductImage = dialogView.findViewById(R.id.ivProductImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);

        if (barcode != null) {
            etBarcode.setText(barcode);
        }

        selectedImageUri = null;
        addDialogImageView = ivProductImage;
        final String[] savedImagePath = {null};

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Product")
                .setView(dialogView)
                .setPositiveButton("Add", (dialogInterface, which) -> {
                    try {
                        String name = etName.getText().toString();
                        String description = etDescription.getText().toString();
                        double price = Double.parseDouble(etPrice.getText().toString());
                        int stock = Integer.parseInt(etStock.getText().toString());
                        String expiryDate = etExpiryDate.getText().toString();
                        String manufacturer = etManufacturer.getText().toString();
                        String barcodeValue = etBarcode.getText().toString();
                        String category = etCategory.getText().toString();
                        String imageUriStr = null;
                        if (selectedImageUri != null) {
                            imageUriStr = copyImageToInternalStorage(selectedImageUri, "product_" + System.currentTimeMillis() + ".jpg");
                        }
                        // Check again for duplicate just before adding
                        if (dbHelper.getProductByBarcode(barcodeValue) != null) {
                            showSnackbar("A product with this barcode already exists!");
                            return;
                        }
                        Product newProduct = new Product(name, description, price, stock, expiryDate, manufacturer, imageUriStr, barcodeValue, category);
                        dbHelper.insertProduct(newProduct);
                        NotificationUtils.checkAndNotify(this);
                        refreshData();
                        showSnackbar("Product added successfully");
                    } catch (Exception e) {
                        showSnackbar("Error adding product. Please check your inputs.");
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        addProductDialog = dialog;
        dialog.show();
    }

    private void showEditProductDialog(Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etProductDescription);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etStock = dialogView.findViewById(R.id.etProductStock);
        TextInputEditText etExpiryDate = dialogView.findViewById(R.id.etProductExpiryDate);
        TextInputEditText etManufacturer = dialogView.findViewById(R.id.etProductManufacturer);
        TextInputEditText etBarcode = dialogView.findViewById(R.id.etProductBarcode);
        TextInputEditText etCategory = dialogView.findViewById(R.id.etProductCategory);
        etCategory.setOnClickListener(v -> showCategoryMultiSelectDialog(etCategory));
        ImageView ivProductImage = dialogView.findViewById(R.id.ivProductImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);

        // Pre-fill current values
        etName.setText(product.getName());
        etDescription.setText(product.getDescription());
        etPrice.setText(String.valueOf(product.getPrice()));
        etStock.setText(String.valueOf(product.getStock()));
        etExpiryDate.setText(product.getExpiryDate());
        etManufacturer.setText(product.getManufacturer());
        etBarcode.setText(product.getBarcode());
        etCategory.setText(product.getCategory());
        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            try {
                ivProductImage.setImageURI(Uri.parse(product.getImageUri()));
            } catch (Exception e) {
                ivProductImage.setImageResource(R.drawable.ic_add_photo);
            }
        } else {
            ivProductImage.setImageResource(R.drawable.ic_add_photo);
        }
        editDialogImageView = ivProductImage;
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_PICK + 1);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Product")
                .setView(dialogView)
                .setPositiveButton("Save", (dialogInterface, which) -> {
                    try {
                        String imageUriStr = product.getImageUri();
                        if (selectedImageUri != null) {
                            imageUriStr = copyImageToInternalStorage(selectedImageUri, "product_" + System.currentTimeMillis() + ".jpg");
                        }
                        String barcodeValue = "";
                        TextInputEditText etBarcode1 = dialogView.findViewById(R.id.etProductBarcode);
                        if (etBarcode1 != null) barcodeValue = etBarcode1.getText().toString();
                        String category = etCategory.getText().toString();
                        Product updatedProduct = new Product(
                            product.getId(),
                            etName.getText().toString(),
                            etDescription.getText().toString(),
                            Double.parseDouble(etPrice.getText().toString()),
                            Integer.parseInt(etStock.getText().toString()),
                            etExpiryDate.getText().toString(),
                            etManufacturer.getText().toString(),
                            imageUriStr,
                            barcodeValue,
                            category
                        );
                        dbHelper.updateProduct(updatedProduct);
                        NotificationUtils.checkAndNotify(this);
                        refreshData();
                        showSnackbar("Product updated successfully");
                    } catch (Exception e) {
                        showSnackbar("Error updating product. Please check your inputs.");
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        editProductDialog = dialog;
        dialog.show();
    }

    private void showDeleteConfirmationDialog(Product product) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteProduct(product.getId());
                    refreshData();
                    String message = "Deleted product: " + product.getName();
                    showSnackbar(message);
                    // Show a notification for the deleted product
                    NotificationHelper.showNotification(this, "Product Deleted", message);
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
        intent.putExtra("imageUri", product.getImageUri());
        startActivity(intent);
    }

    private void refreshData() {
        if (dbHelper == null) dbHelper = new DBHelper(this);
        ArrayList<Product> latestProducts = dbHelper.getAllProducts();
        if (productViewModel != null) {
            productViewModel.setProducts(latestProducts);
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
        // Also show as notification
        NotificationHelper.showNotification(this, "Pharmacy App", message);
    }

    private String copyImageToInternalStorage(Uri uri, String fileName) {
        try {
            return FileUtils.copyUriToInternalStorage(this, uri, fileName);
        } catch (IOException e) {
            e.printStackTrace();
            showSnackbar("Failed to save image");
            return null;
        }
    }

    private void showCategoryMultiSelectDialog(TextInputEditText etCategory) {
        String[] categories = getResources().getStringArray(R.array.pharmacy_categories);
        boolean[] checkedItems = new boolean[categories.length];
        String current = etCategory.getText().toString();
        if (!current.isEmpty()) {
            String[] selected = current.split(",");
            for (int i = 0; i < categories.length; i++) {
                for (String sel : selected) {
                    if (categories[i].trim().equals(sel.trim())) {
                        checkedItems[i] = true;
                    }
                }
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Categories")
                .setMultiChoiceItems(categories, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < categories.length; i++) {
                        if (checkedItems[i]) {
                            if (sb.length() > 0) sb.append(",");
                            sb.append(categories[i]);
                        }
                    }
                    etCategory.setText(sb.toString());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLowStockSnackbarIfNeeded(ArrayList<Product> products) {
        ArrayList<Product> lowStockProducts = new ArrayList<>();
        ArrayList<Product> expiringProducts = new ArrayList<>();
        ArrayList<Product> expiredProducts = new ArrayList<>();
        long now = System.currentTimeMillis();
        long expiryThresholdMillis = NotificationUtils.EXPIRY_DAYS_THRESHOLD * 24L * 60L * 60L * 1000L;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        for (Product p : products) {
            if (p.getStock() <= NotificationUtils.LOW_STOCK_THRESHOLD) {
                lowStockProducts.add(p);
            }
            // Expiring soon and expired logic
            try {
                if (p.getExpiryDate() != null && !p.getExpiryDate().isEmpty()) {
                    java.util.Date expiry = sdf.parse(p.getExpiryDate());
                    if (expiry != null) {
                        if (expiry.getTime() - now <= expiryThresholdMillis && expiry.getTime() > now) {
                            expiringProducts.add(p);
                        } else if (expiry.getTime() <= now) {
                            expiredProducts.add(p);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        // Show low stock Snackbar
        if (!lowStockProducts.isEmpty()) {
            String msg;
            if (lowStockProducts.size() == 1) {
                msg = "Low stock: " + lowStockProducts.get(0).getName();
            } else {
                msg = "You have " + lowStockProducts.size() + " products with low stock.";
            }
            Snackbar.make(rvProducts, msg, Snackbar.LENGTH_LONG)
                .setAction("VIEW", v -> {
                    Intent intent = new Intent(AdminActivity.this, AdminNotificationsActivity.class);
                    startActivity(intent);
                })
                .show();
        }
        // Show expiring soon Snackbar
        if (!expiringProducts.isEmpty()) {
            String msg;
            if (expiringProducts.size() == 1) {
                msg = "Expiring soon: " + expiringProducts.get(0).getName();
            } else {
                msg = "You have " + expiringProducts.size() + " products expiring soon.";
            }
            Snackbar.make(rvProducts, msg, Snackbar.LENGTH_LONG)
                .setAction("VIEW", v -> {
                    Intent intent = new Intent(AdminActivity.this, AdminNotificationsActivity.class);
                    startActivity(intent);
                })
                .show();
        }
        // Show expired Snackbar
        if (!expiredProducts.isEmpty()) {
            String msg;
            if (expiredProducts.size() == 1) {
                msg = "Expired: " + expiredProducts.get(0).getName();
            } else {
                msg = "You have " + expiredProducts.size() + " expired products.";
            }
            Snackbar.make(rvProducts, msg, Snackbar.LENGTH_LONG)
                .setAction("VIEW", v -> {
                    Intent intent = new Intent(AdminActivity.this, AdminNotificationsActivity.class);
                    startActivity(intent);
                })
                .show();
        }
    }

    public void showOrderPlacedSnackbar(String customerName, int totalQuantity) {
        String message = "Order placed for " + customerName + " with " + totalQuantity + " items";
        showSnackbar(message);
        // Show a more prominent notification for new orders
        NotificationHelper.showNotification(this, "New Order Placed", message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            admindrawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+, check if we have the media permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO
                        },
                        STORAGE_PERMISSION_CODE);
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android 6.0 to 12L, check for READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        STORAGE_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now perform the file operation
                Toast.makeText(this, "Permission granted! Try the import again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Cannot import files.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                // Take persistable URI permission
                int takeFlags = 0;
                // Check for read permission flag
                if ((data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
                    takeFlags |= Intent.FLAG_GRANT_READ_URI_PERMISSION;
                }
                // Check for write permission flag
                if ((data.getFlags() & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0) {
                    takeFlags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                }
                // Only call if we have valid flags
                if (takeFlags != 0) {
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                }

                if (requestCode == REQUEST_IMPORT_ZIP) {
                    importProductsFromZip(uri);
                }
            } catch (Exception e) {
                showSnackbar("Error accessing file: " + e.getMessage());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (addDialogImageView != null && selectedImageUri != null) {
                addDialogImageView.setImageURI(selectedImageUri);
            }
        }
        if (requestCode == REQUEST_IMAGE_PICK + 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (editDialogImageView != null && selectedImageUri != null) {
                editDialogImageView.setImageURI(selectedImageUri);
            }
        }
        if (requestCode == REQUEST_BARCODE_SCAN && resultCode == RESULT_OK && data != null) {
            String scannedBarcode = data.getStringExtra("barcode");
            if (scannedBarcode != null) {
                showAddProductDialog(scannedBarcode);
            }
        }
        if (requestCode == 2001 && resultCode == Activity.RESULT_OK) {
            updateProfileHeader();
        }
        if (requestCode == REQUEST_IMPORT_CSV && resultCode == RESULT_OK && data != null && data.getData() != null) {
            importProductsFromCsv(data.getData());
        }
        if (requestCode == REQUEST_IMPORT_EXCEL && resultCode == RESULT_OK && data != null && data.getData() != null) {
            importProductsFromExcel(data.getData());
        }
    }

    private void importProductsFromZip(Uri zipUri) {
        File tempDir = null;
        try {
            // Extract zip to temporary directory
            tempDir = ExportImportUtils.extractZip(this, zipUri, "temp_import_" + System.currentTimeMillis());
            
            // Look for products.csv in the extracted files
            File csvFile = new File(tempDir, "products.csv");
            if (!csvFile.exists()) {
                showSnackbar("No products.csv found in the ZIP file");
                return;
            }
            
            // Create images directory in app's internal storage if it doesn't exist
            File imagesDir = new File(getFilesDir(), IMAGES_DIR);
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
            
            // First, read the CSV to get all image filenames we need to copy
            java.util.Set<String> imageFilenames = new java.util.HashSet<>();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(csvFile))) {
                String line;
                boolean isHeader = true;
                while ((line = reader.readLine()) != null) {
                    if (isHeader) { 
                        isHeader = false; 
                        continue; 
                    }
                    String[] fields = parseCsvLine(line);
                    if (fields.length > 6 && !fields[6].isEmpty()) {
                        // Extract just the filename from the image path
                        String imagePath = fields[6];
                        String filename = new File(imagePath).getName();
                        if (!filename.isEmpty()) {
                            imageFilenames.add(filename);
                        }
                    }
                }
            }
            
            // Copy only the image files that are referenced in the CSV
            File tempImagesDir = new File(tempDir, ExportImportUtils.IMAGES_DIR);
            Log.d(TAG, "Looking for images in: " + tempImagesDir.getAbsolutePath());
            
            if (tempImagesDir.exists() && tempImagesDir.isDirectory()) {
                File[] imageFiles = tempImagesDir.listFiles();
                Log.d(TAG, "Found " + (imageFiles != null ? imageFiles.length : 0) + " files in temp images directory");
                Log.d(TAG, "Looking for these images: " + imageFilenames);
                
                if (imageFiles != null) {
                    for (File imageFile : imageFiles) {
                        String filename = imageFile.getName();
                        Log.d(TAG, "Checking file: " + filename);
                        
                        if (imageFile.isFile() && imageFilenames.contains(filename)) {
                            File destFile = new File(imagesDir, filename);
                            Log.d(TAG, "Copying image from " + imageFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());
                            
                            try (InputStream in = new java.io.FileInputStream(imageFile);
                                 OutputStream out = new FileOutputStream(destFile)) {
                                byte[] buffer = new byte[8192];
                                int length;
                                long totalBytes = 0;
                                while ((length = in.read(buffer)) > 0) {
                                    out.write(buffer, 0, length);
                                    totalBytes += length;
                                }
                                Log.d(TAG, "Successfully copied " + totalBytes + " bytes for " + filename);
                                
                                // Verify the file was copied correctly
                                if (destFile.exists()) {
                                    Log.d(TAG, "Destination file exists, size: " + destFile.length() + " bytes");
                                } else {
                                    Log.e(TAG, "Failed to verify destination file: " + destFile.getAbsolutePath());
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Error copying image: " + imageFile.getName(), e);
                            }
                        } else if (!imageFilenames.contains(filename)) {
                            Log.d(TAG, "Skipping file not in CSV: " + filename);
                        }
                    }
                }
                
                // List all files in the destination directory for verification
                File[] destFiles = imagesDir.listFiles();
                if (destFiles != null && destFiles.length > 0) {
                    Log.d(TAG, "Files in destination directory after copy:");
                    for (File f : destFiles) {
                        Log.d(TAG, "- " + f.getName() + " (size: " + f.length() + " bytes)");
                    }
                } else {
                    Log.e(TAG, "No files found in destination directory after copy");
                }
            }
            
            // Now import the products
            importProductsFromCsv(Uri.fromFile(csvFile));
            
        } catch (Exception e) {
            showSnackbar("Failed to import from ZIP: " + e.getMessage());
            Log.e(TAG, "Import from ZIP failed", e);
        } finally {
            // Clean up temp files
            if (tempDir != null) {
                ExportImportUtils.cleanupTempFiles(tempDir);
            }
        }
    }

    private void importProductsFromCsv(Uri uri) {
        java.io.InputStream is = null;
        java.io.BufferedReader reader = null;
        int imported = 0, failed = 0;
        
        try {
            is = getContentResolver().openInputStream(uri);
            if (is == null) {
                showSnackbar("Cannot open file. File might be corrupted or inaccessible.");
                return;
            }
            
            // Get the images directory path
            File imagesDir = new File(getFilesDir(), IMAGES_DIR);
            String imagesDirPath = imagesDir.getAbsolutePath() + File.separator;
            
            reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                String[] fields = parseCsvLine(line);
                if (fields.length < 9) { failed++; continue; }
                String name = fields[0];
                String description = fields[1];
                double price;
                int stock;
                try {
                    price = Double.parseDouble(fields[2]);
                    stock = Integer.parseInt(fields[3]);
                } catch (Exception e) { failed++; continue; }
                String expiryDate = fields[4];
                String manufacturer = fields[5];
                
                // Handle image path - if it's a full path, extract just the filename
                String imageUri = "";
                if (fields.length > 6 && !fields[6].isEmpty()) {
                    try {
                        String imagePath = fields[6];
                        Log.d(TAG, "Original image path from CSV: " + imagePath);
                        
                        String filename = new File(imagePath).getName();
                        Log.d(TAG, "Extracted filename: " + filename);
                        
                        if (!filename.isEmpty()) {
                            // Check if the image exists in our internal storage
                            File imageFile = new File(imagesDir, filename);
                            Log.d(TAG, "Looking for image at: " + imageFile.getAbsolutePath());
                            
                            if (imageFile.exists()) {
                                Log.d(TAG, "Image found, creating URI");
                                try {
                                    // Create a content URI using the images directory path
                                    Uri contentUri = Uri.parse("content://" + getApplicationContext().getPackageName() + ".provider/images/" + filename);
                                    Log.d(TAG, "Created content URI: " + contentUri);
                                    imageUri = contentUri.toString();
                                    Log.d(TAG, "Final image URI: " + imageUri);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error creating content URI: " + e.getMessage(), e);
                                    // Fallback to file URI if FileProvider fails
                                    imageUri = Uri.fromFile(imageFile).toString();
                                    Log.d(TAG, "Using file URI as fallback: " + imageUri);
                                }
                            } else {
                                Log.e(TAG, "Image file does not exist: " + imageFile.getAbsolutePath());
                                // List all files in the images directory for debugging
                                File[] files = imagesDir.listFiles();
                                if (files != null) {
                                    Log.d(TAG, "Files in images directory:");
                                    for (File f : files) {
                                        Log.d(TAG, "- " + f.getName() + " (exists: " + f.exists() + ")");
                                    }
                                } else {
                                    Log.e(TAG, "No files found in images directory");
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing image path: " + e.getMessage(), e);
                    }
                }
                
                String barcode = fields[7];
                String category = fields[8];
                
                Product existing = null;
                
                // Check for existing product by barcode if barcode is provided
                if (barcode != null && !barcode.trim().isEmpty()) {
                    existing = dbHelper.getProductByBarcode(barcode);
                } 
                // If no barcode or no product found by barcode, check by name (for products without barcode)
                if (existing == null && (barcode == null || barcode.trim().isEmpty())) {
                    existing = dbHelper.getProductByName(name);
                }
                
                if (existing != null) {
                    // Update existing product
                    existing.setName(name);
                    existing.setDescription(description);
                    existing.setPrice(price);
                    // Add to existing stock instead of replacing it
                    existing.setStock(existing.getStock() + stock);
                    existing.setExpiryDate(expiryDate);
                    existing.setManufacturer(manufacturer);
                    // Only update image if the imported one is not empty
                    if (imageUri != null && !imageUri.trim().isEmpty()) {
                        existing.setImageUri(imageUri);
                    }
                    existing.setCategory(category);
                    dbHelper.updateProduct(existing);
                } else {
                    // Insert new product
                    Product p = new Product(name, description, price, stock, expiryDate, 
                                         manufacturer, imageUri, barcode, category);
                    dbHelper.insertProduct(p);
                }
                imported++;
            }
            Snackbar.make(findViewById(android.R.id.content), 
                "Import complete: " + imported + " added/updated, " + failed + " failed.", 
                Snackbar.LENGTH_LONG).show();
            refreshData();
        } catch (Exception e) {
            showSnackbar("Import failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void importProductsFromExcel(Uri uri) {
        java.io.InputStream is = null;
        Workbook workbook = null;
        int imported = 0, failed = 0;
        
        try {
            is = getContentResolver().openInputStream(uri);
            if (is == null) {
                showSnackbar("Cannot open file. File might be corrupted or inaccessible.");
                return;
            }
            
            workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String name = getCellString(row, 0);
                String description = getCellString(row, 1);
                double price;
                int stock;
                try {
                    price = Double.parseDouble(getCellString(row, 2));
                    stock = Integer.parseInt(getCellString(row, 3));
                } catch (Exception e) { 
                    failed++; 
                    continue; 
                }
                
                String expiryDate = getCellString(row, 4);
                String manufacturer = getCellString(row, 5);
                String imageUri = getCellString(row, 6);
                String barcode = getCellString(row, 7);
                String category = getCellString(row, 8);
                
                Product existing = null;
                
                // Check for existing product by barcode if barcode is provided
                if (barcode != null && !barcode.trim().isEmpty()) {
                    existing = dbHelper.getProductByBarcode(barcode);
                } 
                // If no barcode or no product found by barcode, check by name (for products without barcode)
                if (existing == null && (barcode == null || barcode.trim().isEmpty())) {
                    existing = dbHelper.getProductByName(name);
                }
                
                if (existing != null) {
                    // Update existing product
                    existing.setName(name);
                    existing.setDescription(description);
                    existing.setPrice(price);
                    // Add to existing stock instead of replacing it
                    existing.setStock(existing.getStock() + stock);
                    existing.setExpiryDate(expiryDate);
                    existing.setManufacturer(manufacturer);
                    // Only update image if the imported one is not empty
                    if (imageUri != null && !imageUri.trim().isEmpty()) {
                        existing.setImageUri(imageUri);
                    }
                    existing.setCategory(category);
                    dbHelper.updateProduct(existing);
                } else {
                    // Insert new product
                    Product p = new Product(name, description, price, stock, expiryDate, 
                                         manufacturer, imageUri, barcode, category);
                    dbHelper.insertProduct(p);
                }
                imported++;
            }
            
            Snackbar.make(findViewById(android.R.id.content), 
                "Import complete: " + imported + " added/updated, " + failed + " failed.", 
                Snackbar.LENGTH_LONG).show();
            refreshData();
        } catch (Exception e) {
            showSnackbar("Import failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (workbook != null) workbook.close();
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCellString(Row row, int cellIdx) {
        Cell cell = row.getCell(cellIdx);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    private String[] parseCsvLine(String line) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }
    
    private void startImportProductsFlow() {
        if (checkStoragePermission()) {
            try {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/zip");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | 
                              Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                              Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(Intent.createChooser(intent, "Select Export File"), REQUEST_IMPORT_ZIP);
            } catch (Exception e) {
                showSnackbar("Error opening file picker: " + e.getMessage());
            }
        }
    }
    
    private void startImportProductsExcelFlow() {
        if (checkStoragePermission()) {
            try {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                String[] mimeTypes = {
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-excel.sheet.macroEnabled.12",
                    "application/octet-stream"
                };
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | 
                              Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                              Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(Intent.createChooser(intent, "Select Excel File"), REQUEST_IMPORT_EXCEL);
            } catch (Exception e) {
                showSnackbar("Error opening file picker: " + e.getMessage());
            }
        }
    }
    
    private void startExportProductsZipFlow() {
        try {
            ArrayList<Product> products = dbHelper.getAllProducts();
            if (products == null || products.isEmpty()) {
                showSnackbar("No products to export");
                return;
            }

            // Create temporary export directory
            File exportDir = ExportImportUtils.createTempExportDir(this);
            File imagesDir = ExportImportUtils.createImagesDir(exportDir);
            
            try {
                // Create CSV file
                File csvFile = new File(exportDir, "products.csv");
                StringBuilder csvBuilder = new StringBuilder();
                
                // Header
                csvBuilder.append("name,description,price,stock,expiryDate,manufacturer,imagePath,barcode,category\n");
                
                // Data rows
                for (Product p : products) {
                    String imagePath = "";
                    if (p.getImageUri() != null && !p.getImageUri().isEmpty()) {
                        try {
                            String uriString = p.getImageUri();
                            String imageName = "img_" + System.currentTimeMillis() + ".jpg";
                            
                            // Handle content URIs
                            if (uriString.startsWith("content://")) {
                                try (InputStream in = getContentResolver().openInputStream(Uri.parse(uriString))) {
                                    if (in != null) {
                                        File destFile = new File(imagesDir, imageName);
                                        try (FileOutputStream out = new FileOutputStream(destFile)) {
                                            byte[] buffer = new byte[8192];
                                            int read;
                                            while ((read = in.read(buffer)) != -1) {
                                                out.write(buffer, 0, read);
                                            }
                                            imagePath = IMAGES_DIR + "/" + imageName;
                                            Log.d(TAG, "Exported image to: " + destFile.getAbsolutePath());
                                        }
                                    }
                                }
                            } 
                            // Handle file URIs
                            else if (uriString.startsWith("file://")) {
                                File srcFile = new File(Uri.parse(uriString).getPath());
                                if (srcFile.exists()) {
                                    String ext = uriString.substring(uriString.lastIndexOf('.'));
                                    String newImageName = "img_" + System.currentTimeMillis() + ext;
                                    File destFile = new File(imagesDir, newImageName);
                                    
                                    try (FileInputStream in = new FileInputStream(srcFile);
                                         FileOutputStream out = new FileOutputStream(destFile)) {
                                        byte[] buffer = new byte[8192];
                                        int read;
                                        while ((read = in.read(buffer)) != -1) {
                                            out.write(buffer, 0, read);
                                        }
                                        imagePath = IMAGES_DIR + "/" + newImageName;
                                        Log.d(TAG, "Copied file to: " + destFile.getAbsolutePath());
                                    }
                                }
                            }
                            // Handle direct file paths
                            else if (new File(uriString).exists()) {
                                File srcFile = new File(uriString);
                                String ext = uriString.substring(uriString.lastIndexOf('.'));
                                String newImageName = "img_" + System.currentTimeMillis() + ext;
                                File destFile = new File(imagesDir, newImageName);
                                
                                try (FileInputStream in = new FileInputStream(srcFile);
                                     FileOutputStream out = new FileOutputStream(destFile)) {
                                    byte[] buffer = new byte[8192];
                                    int read;
                                    while ((read = in.read(buffer)) != -1) {
                                        out.write(buffer, 0, read);
                                    }
                                    imagePath = IMAGES_DIR + "/" + newImageName;
                                    Log.d(TAG, "Copied direct file to: " + destFile.getAbsolutePath());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error exporting image: " + e.getMessage(), e);
                        }
                    }
                    
                    // Add product to CSV
                    csvBuilder.append(escapeCsv(p.getName())).append(",")
                            .append(escapeCsv(p.getDescription())).append(",")
                            .append(p.getPrice()).append(",")
                            .append(p.getStock()).append(",")
                            .append(escapeCsv(p.getExpiryDate())).append(",")
                            .append(escapeCsv(p.getManufacturer())).append(",")
                            .append(escapeCsv(imagePath)).append(",")
                            .append(escapeCsv(p.getBarcode())).append(",")
                            .append(escapeCsv(p.getCategory())).append("\n");
                }
                
                // Write CSV to file
                try (FileOutputStream fos = new FileOutputStream(csvFile)) {
                    fos.write(csvBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                
                // Create ZIP file
                String exportName = ExportImportUtils.getExportFileName();
                File zipFile = ExportImportUtils.createZipFromDirectory(exportDir, exportName);
                
                // Share the ZIP file
                Uri contentUri = FileProvider.getUriForFile(this, 
                    getPackageName() + ".provider", 
                    zipFile);
                    
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/zip");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                startActivity(Intent.createChooser(shareIntent, "Export products"));
                showSnackbar("Exported to " + zipFile.getName());
                
            } finally {
                // Clean up temporary files
                ExportImportUtils.cleanupTempFiles(exportDir);
            }
            
        } catch (Exception e) {
            showSnackbar("Export failed: " + e.getMessage());
            Log.e(TAG, "Export failed", e);
        }
    }

    private void startExportProductsCsvFlow() {
        try {
            ArrayList<Product> products = dbHelper.getAllProducts();
            if (products == null || products.isEmpty()) {
                showSnackbar("No products to export");
                return;
            }
            
            StringBuilder csvBuilder = new StringBuilder();
            // Header
            csvBuilder.append("name,description,price,stock,expiryDate,manufacturer,imageUri,barcode,category\n");
            
            // Data rows
            for (Product p : products) {
                csvBuilder.append(escapeCsv(p.getName())).append(",")
                        .append(escapeCsv(p.getDescription())).append(",")
                        .append(p.getPrice()).append(",")
                        .append(p.getStock()).append(",")
                        .append(escapeCsv(p.getExpiryDate())).append(",")
                        .append(escapeCsv(p.getManufacturer())).append(",")
                        .append(escapeCsv(p.getImageUri())).append(",")
                        .append(escapeCsv(p.getBarcode())).append(",")
                        .append(escapeCsv(p.getCategory())).append("\n");
            }
            
            // Create file in app's external files directory
            File exportDir = new File(getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File file = new File(exportDir, "products_" + timeStamp + ".csv");
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(csvBuilder.toString().getBytes(StandardCharsets.UTF_8));
                
                // Share the file
                Uri contentUri = FileProvider.getUriForFile(this, 
                    getPackageName() + ".provider", 
                    file);
                    
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/csv");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                startActivity(Intent.createChooser(shareIntent, "Export products as CSV"));
                showSnackbar("Exported to " + file.getName());
                
            } catch (IOException e) {
                showSnackbar("Export failed: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            showSnackbar("Export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void startExportProductsExcelFlow() {
        // This method is kept for backward compatibility
        showSnackbar("Please use the ZIP export/import for better reliability with images");
    }
    
    private void exportProductsToExcel() {
        // This method is kept for backward compatibility
        showSnackbar("Please use the ZIP export/import for better reliability with images");
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }
    
    private void showImportFormatDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Import Format")
            .setItems(new String[]{"CSV", "Excel (.xlsx)"}, (dialog, which) -> {
                if (which == 0) {
                    startImportProductsFlow();
                } else {
                    startImportProductsExcelFlow();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showExportFormatDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Export Format")
            .setItems(new String[]{"ZIP (Recommended)", "CSV", "Excel"}, (dialog, which) -> {
                if (which == 0) {
                    startExportProductsZipFlow();
                } else if (which == 1) {
                    startExportProductsCsvFlow();
                } else {
                    exportProductsToExcel();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showCategoryMultiSelectDialog(String[] allCategories) {
        boolean[] checkedItems = new boolean[allCategories.length];
        for (int i = 0; i < allCategories.length; i++) {
            checkedItems[i] = selectedCategories.contains(allCategories[i]);
        }
        new AlertDialog.Builder(this)
            .setTitle("Select Categories")
            .setMultiChoiceItems(allCategories, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!selectedCategories.contains(allCategories[which]))
                        selectedCategories.add(allCategories[which]);
                } else {
                    selectedCategories.remove(allCategories[which]);
                }
            })
            .setPositiveButton("OK", (dialog, which) -> {
                if (selectedCategories.isEmpty()) {
                    selectedCategories.add("All");
                }
                etAdminCategoryFilter.setText(android.text.TextUtils.join(", ", selectedCategories));
                applyCombinedFilters();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void filterProductsByCategory() {
        applyCombinedFilters();
    }

    @Deprecated
    private void filterProducts(String query) {
        // Deprecated: use applyCombinedFilters instead
    }
}


