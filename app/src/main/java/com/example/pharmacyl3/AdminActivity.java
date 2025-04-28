package com.example.pharmacyl3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import com.example.pharmacyl3.R;
import android.app.AlertDialog;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ArrayList<Product> productsList;
    private AdminProductAdapter adapter;
    private DBHelper dbHelper;
    private FloatingActionButton fabAddProduct;
    private TextInputEditText searchEditText;
    private DrawerLayout admindrawer;
    private NavigationView navView;
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_BARCODE_SCAN = 2002;
    private static final int REQUEST_IMPORT_CSV = 3001;
    private static final int REQUEST_EXPORT_CSV = 3002;
    private static final int REQUEST_IMPORT_EXCEL = 4001;
    private static final int REQUEST_EXPORT_EXCEL = 4002;
    private Uri selectedImageUri = null;
    private AlertDialog addProductDialog;
    private ImageView addDialogImageView;
    private AlertDialog editProductDialog;
    private ImageView editDialogImageView;
    private String pendingBarcode = null;
    private AdminProductViewModel productViewModel;

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
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_menu_overflow_material);
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
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryFilter() {
        // Remove all content from this method, as we are not using AutoCompleteTextView or ChipGroup for admin filtering yet
    }

    private void filterProducts(String query) {
        ArrayList<Product> filteredList = new ArrayList<>();
        for (Product product : productsList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        adapter.updateProducts(filteredList);
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
                    showSnackbar("Product deleted");
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
        String msg = "Order placed: " + customerName + " bought " + totalQuantity + " items.";
        Snackbar.make(rvProducts, msg, Snackbar.LENGTH_LONG)
            .setAction("VIEW", v -> {
                Intent intent = new Intent(AdminActivity.this, AdminNotificationsActivity.class);
                startActivity(intent);
            })
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            admindrawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    private void updateProfileHeader() {
        // Get header view from NavigationView
        View headerView = navView.getHeaderView(0);
        ImageView imageViewProfile = headerView.findViewById(R.id.imageViewProfile);
        TextView textViewName = headerView.findViewById(R.id.textViewName);
        TextView textViewEmail = headerView.findViewById(R.id.textViewEmail);

        String name = ProfileManager.getName(this);
        String phone = ProfileManager.getPhone(this);
        Uri profilePicUri = ProfileManager.getProfilePicUri(this);
        if (!name.isEmpty()) textViewName.setText(name);
        if (!phone.isEmpty()) textViewEmail.setText(phone);
        if (profilePicUri != null) {
            try {
                Bitmap bitmap;
                if (profilePicUri.getScheme() != null && profilePicUri.getScheme().startsWith("content")) {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), profilePicUri);
                } else {
                    bitmap = BitmapFactory.decodeFile(profilePicUri.getPath());
                }
                if (bitmap != null) {
                    Bitmap circularBitmap = getCircularBitmap(bitmap);
                    imageViewProfile.setImageBitmap(circularBitmap);
                } else {
                    imageViewProfile.setImageResource(R.drawable.ic_person);
                }
            } catch (Exception e) {
                imageViewProfile.setImageResource(R.drawable.ic_person);
            }
        } else {
            imageViewProfile.setImageResource(R.drawable.ic_person);
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final RectF rect = new RectF(0, 0, size, size);
        float radius = size / 2f;
        paint.setAntiAlias(true);
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        return output;
    }

    private void launchBarcodeScanner() {
        Intent intent = new Intent(this, BarcodeScannerActivity.class);
        startActivityForResult(intent, REQUEST_BARCODE_SCAN);
    }

    private void startImportProductsFlow() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/csv");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select CSV File"), REQUEST_IMPORT_CSV);
    }

    private void startImportProductsExcelFlow() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Excel File"), REQUEST_IMPORT_EXCEL);
    }

    private void startExportProductsFlow() {
        // Export products to CSV and share
        try {
            ArrayList<Product> products = dbHelper.getAllProducts();
            StringBuilder csvBuilder = new StringBuilder();
            csvBuilder.append("name,description,price,stock,expiryDate,manufacturer,imageUri,barcode,category\n");
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
            String csv = csvBuilder.toString();
            java.io.File file = new java.io.File(getExternalFilesDir(null), "products_export.csv");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            fos.write(csv.getBytes());
            fos.close();
            // Share the file
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".provider", file));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share CSV File"));
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "Export failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void startExportProductsExcelFlow() {
        try {
            ArrayList<Product> products = dbHelper.getAllProducts();
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Products");
            Row header = sheet.createRow(0);
            String[] columns = {"name","description","price","stock","expiryDate","manufacturer","imageUri","barcode","category"};
            for (int i = 0; i < columns.length; i++) header.createCell(i).setCellValue(columns[i]);
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(p.getName());
                row.createCell(1).setCellValue(p.getDescription());
                row.createCell(2).setCellValue(p.getPrice());
                row.createCell(3).setCellValue(p.getStock());
                row.createCell(4).setCellValue(p.getExpiryDate());
                row.createCell(5).setCellValue(p.getManufacturer());
                row.createCell(6).setCellValue(p.getImageUri());
                row.createCell(7).setCellValue(p.getBarcode());
                row.createCell(8).setCellValue(p.getCategory());
            }
            java.io.File file = new java.io.File(getExternalFilesDir(null), "products_export.xlsx");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            shareIntent.putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".provider", file));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Excel File"));
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "Export failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\n") || v.contains("\"")) {
            v = '"' + v + '"';
        }
        return v;
    }

    private void importProductsFromCsv(Uri uri) {
        try {
            java.io.InputStream is = getContentResolver().openInputStream(uri);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
            String line;
            boolean isHeader = true;
            int imported = 0, failed = 0;
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
                String imageUri = fields[6];
                String barcode = fields[7];
                String category = fields[8];
                // Insert or update product by barcode
                Product existing = dbHelper.getProductByBarcode(barcode);
                if (existing != null) {
                    existing.setName(name);
                    existing.setDescription(description);
                    existing.setPrice(price);
                    existing.setStock(stock);
                    existing.setExpiryDate(expiryDate);
                    existing.setManufacturer(manufacturer);
                    existing.setImageUri(imageUri);
                    existing.setCategory(category);
                    dbHelper.updateProduct(existing);
                } else {
                    Product p = new Product(name, description, price, stock, expiryDate, manufacturer, imageUri, barcode, category);
                    dbHelper.insertProduct(p);
                }
                imported++;
            }
            reader.close();
            Snackbar.make(findViewById(android.R.id.content), "Import complete: " + imported + " added/updated, " + failed + " failed.", Snackbar.LENGTH_LONG).show();
            refreshData();
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "Import failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void importProductsFromExcel(Uri uri) {
        try {
            java.io.InputStream is = getContentResolver().openInputStream(uri);
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            int imported = 0, failed = 0;
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
                } catch (Exception e) { failed++; continue; }
                String expiryDate = getCellString(row, 4);
                String manufacturer = getCellString(row, 5);
                String imageUri = getCellString(row, 6);
                String barcode = getCellString(row, 7);
                String category = getCellString(row, 8);
                Product existing = dbHelper.getProductByBarcode(barcode);
                if (existing != null) {
                    existing.setName(name);
                    existing.setDescription(description);
                    existing.setPrice(price);
                    existing.setStock(stock);
                    existing.setExpiryDate(expiryDate);
                    existing.setManufacturer(manufacturer);
                    existing.setImageUri(imageUri);
                    existing.setCategory(category);
                    dbHelper.updateProduct(existing);
                } else {
                    Product p = new Product(name, description, price, stock, expiryDate, manufacturer, imageUri, barcode, category);
                    dbHelper.insertProduct(p);
                }
                imported++;
            }
            workbook.close();
            Snackbar.make(findViewById(android.R.id.content), "Import complete: " + imported + " added/updated, " + failed + " failed.", Snackbar.LENGTH_LONG).show();
            refreshData();
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "Import failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
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
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
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
            .setItems(new String[]{"CSV", "Excel (.xlsx)"}, (dialog, which) -> {
                if (which == 0) {
                    startExportProductsFlow();
                } else {
                    startExportProductsExcelFlow();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
