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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
    private Uri selectedImageUri = null;
    private AlertDialog addProductDialog;
    private ImageView addDialogImageView;
    private AlertDialog editProductDialog;
    private ImageView editDialogImageView;
    private String pendingBarcode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize views
        initializeViews();
        setupToolbar();
        setupDrawer();
        setupRecyclerView();
        setupSearchFunctionality();
        setupCategoryFilter();

        // Setup FAB for adding new products
        fabAddProduct.setOnClickListener(v -> showAddProductDialog(null));

        // Update header on launch
        updateProfileHeader();
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
                Intent intent = new Intent(AdminActivity.this, EditProfileActivity.class);
                startActivityForResult(intent, 2001);
            } else if (id == R.id.nav_products) {
                // Handle Products
            } else if (id == R.id.nav_dashboard) {
                // Show dashboard activity
                Intent intent = new Intent(AdminActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_barcode_scanner) {
                launchBarcodeScanner();
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
        productsList.clear();
        productsList.addAll(dbHelper.getAllProducts());
        adapter.notifyDataSetChanged();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
        updateProfileHeader();
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
                // Use file path for image
                Bitmap bitmap = BitmapFactory.decodeFile(profilePicUri.getPath());
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
}
