package com.example.pharmacyl3;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import android.app.AlertDialog;
import android.net.Uri;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.stream.Collectors;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class CustomerActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ArrayList<Product> productsList;
    private ArrayList<Product> filteredList;
    private ProductRecyclerAdapter adapter;
    private DBHelper dbHelper;
    private TextInputEditText searchEditText;
    private TextView cartItemCount;
    private ImageButton cartButton;
    private ArrayList<Product> cartItems;
    private View cartButtonContainer;
    private TextInputEditText etCategoryFilter;
    private String[] categories;
    private ArrayList<String> selectedCategories = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private int customerId;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        // Initialize cart
        cartItems = new ArrayList<>();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        rvProducts = findViewById(R.id.rvProducts);
        searchEditText = findViewById(R.id.searchEditText);
        cartItemCount = findViewById(R.id.cartItemCount);
        cartButton = findViewById(R.id.cartButton);
        cartButtonContainer = findViewById(R.id.cartButtonContainer);
        etCategoryFilter = findViewById(R.id.etCategoryFilter);
        drawerLayout = findViewById(R.id.drawer_layout_customer);
        navView = findViewById(R.id.nav_view_customer);

        if (searchEditText != null) {
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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup RecyclerView
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setHasFixedSize(true);

        // Initialize database and load products
        dbHelper = new DBHelper(this);
        // Get customerId from intent or session (replace with actual logic)
        customerId = getIntent().getIntExtra("customerId", -1);
        productsList = dbHelper.getAllProducts();
        filteredList = new ArrayList<>(productsList);
        // Load cart from DB
        cartItems = dbHelper.getCartItems(customerId);

        // Setup adapter
        adapter = new ProductRecyclerAdapter(filteredList, 
            new ProductRecyclerAdapter.ProductInteractionListener() {
                @Override
                public void onItemClick(Product product) {
                    Intent intent = new Intent(CustomerActivity.this, ProductDetailActivity.class);
                    intent.putExtra("name", product.getName());
                    intent.putExtra("description", product.getDescription());
                    intent.putExtra("price", product.getPrice());
                    intent.putExtra("stock", product.getStock());
                    intent.putExtra("imageUri", product.getImageUri());
                    startActivity(intent);
                }

                @Override
                public void onAddToCart(Product product) {
                    addToCart(product);
                }
            });
        
        rvProducts.setAdapter(adapter);

        // Setup cart button click
        cartButton.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerActivity.this, CartActivity.class);
            intent.putExtra("customerId", customerId);
            startActivityForResult(intent, 101);
        });

        categories = getResources().getStringArray(R.array.pharmacy_categories);
        String[] allCategories = new String[categories.length + 1];
        allCategories[0] = "All";
        System.arraycopy(categories, 0, allCategories, 1, categories.length);
        selectedCategories.clear();
        selectedCategories.add("All");
        etCategoryFilter.setText("All");
        etCategoryFilter.setOnClickListener(v -> showCategoryMultiSelectDialog(allCategories));

        // --- Load profile info into nav header ---
        View headerView = navView.getHeaderView(0);
        ImageView imgProfilePhoto = headerView.findViewById(R.id.imgProfilePhoto);
        TextView tvProfileName = headerView.findViewById(R.id.tvProfileName);
        TextView tvProfilePhone = headerView.findViewById(R.id.tvProfilePhone);
        Customer customer = dbHelper.getCustomerById(customerId);
        if (customer != null) {
            tvProfileName.setText(customer.name);
            tvProfilePhone.setText(customer.phone);
            if (customer.profilePhotoUri != null && !customer.profilePhotoUri.isEmpty()) {
                try {
                    // Use file path for image (admin header logic)
                    String path = Uri.parse(customer.profilePhotoUri).getPath();
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if (bitmap != null) {
                        // Make the bitmap circular like in admin header
                        Bitmap circularBitmap = getCircularBitmap(bitmap);
                        imgProfilePhoto.setImageBitmap(circularBitmap);
                    } else {
                        imgProfilePhoto.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } catch (Exception e) {
                    imgProfilePhoto.setImageResource(R.drawable.ic_profile_placeholder);
                }
            } else {
                imgProfilePhoto.setImageResource(R.drawable.ic_profile_placeholder);
            }
        }

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_edit_profile) {
                    Intent intent = new Intent(CustomerActivity.this, CustomerEditProfileActivity.class);
                    intent.putExtra("customerId", customerId);
                    startActivity(intent);
                } else if (id == R.id.nav_order_history) {
                    Intent intent = new Intent(CustomerActivity.this, OrderHistoryActivity.class);
                    intent.putExtra("customerId", customerId);
                    startActivity(intent);
                } else if (id == R.id.nav_scan_to_sell) {
                    Intent intent = new Intent(CustomerActivity.this, BarcodeScannerActivity.class);
                    startActivityForResult(intent, 2002);
                } else if (id == R.id.nav_logout) {
                    // Clear saved login (if Remember Me), return to LoginActivity
                    getSharedPreferences("loginPrefs", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(CustomerActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void addToCart(Product product) {
        // Check if product already in cart
        boolean found = false;
        for (Product p : cartItems) {
            if (p.getId() == product.getId()) {
                // Only allow adding up to available stock
                int totalDesired = p.getQuantity() + product.getQuantity();
                int maxAllowed = 0;
                for (Product prod : productsList) {
                    if (prod.getId() == product.getId()) {
                        maxAllowed = prod.getStock(); // Only check current DB stock, don't reduce yet
                        break;
                    }
                }
                if (totalDesired > maxAllowed) {
                    Snackbar.make(findViewById(android.R.id.content),
                            "You can only add up to " + maxAllowed + " items to your cart.",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }
                p.setQuantity(totalDesired);
                dbHelper.addOrUpdateCartItem(customerId, p.getId(), p.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) {
            // Only allow adding up to available stock
            int maxAllowed = 0;
            for (Product prod : productsList) {
                if (prod.getId() == product.getId()) {
                    maxAllowed = prod.getStock();
                    break;
                }
            }
            if (product.getQuantity() > maxAllowed) {
                Snackbar.make(findViewById(android.R.id.content),
                        "You can only add up to " + maxAllowed + " items to your cart.",
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            cartItems.add(product);
            dbHelper.addOrUpdateCartItem(customerId, product.getId(), product.getQuantity());
        }

        // --- DO NOT update stock in product list here ---
        // Stock will only be reduced after checkout
        adapter.notifyDataSetChanged();

        updateCartBadge();
        cartButtonContainer.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.shake_animation));
        Snackbar.make(findViewById(android.R.id.content), 
                     product.getName() + " added to cart", 
                     Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> {
                    undoAddToCart(product);
                })
                .show();
    }

    // --- UNDO add to cart: remove from cart ---
    private void undoAddToCart(Product product) {
        // Remove from cart
        for (int i = 0; i < cartItems.size(); i++) {
            Product p = cartItems.get(i);
            if (p.getId() == product.getId()) {
                int newQty = p.getQuantity() - product.getQuantity();
                if (newQty <= 0) {
                    dbHelper.removeCartItem(customerId, p.getId());
                    cartItems.remove(i);
                } else {
                    p.setQuantity(newQty);
                    dbHelper.addOrUpdateCartItem(customerId, p.getId(), newQty);
                }
                break;
            }
        }
        // --- DO NOT restore stock in product list here ---
        // Stock is only managed after checkout
        adapter.notifyDataSetChanged();
        updateCartBadge();
    }

    private void removeFromCart(Product product) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getId() == product.getId()) {
                int newQty = cartItems.get(i).getQuantity() - 1;
                if (newQty <= 0) {
                    dbHelper.removeCartItem(customerId, product.getId());
                    cartItems.remove(i);
                } else {
                    cartItems.get(i).setQuantity(newQty);
                    dbHelper.addOrUpdateCartItem(customerId, product.getId(), newQty);
                }
                break;
            }
        }
        updateCartBadge();
    }

    private void filterByCategories(ArrayList<String> categories) {
        filteredList.clear();
        if (categories.contains("All") || categories.isEmpty()) {
            filteredList.addAll(productsList);
        } else {
            for (Product product : productsList) {
                boolean matched = false;
                for (String cat : categories) {
                    if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                        String[] prodCats = product.getCategory().split(",");
                        for (String prodCat : prodCats) {
                            if (prodCat.trim().equalsIgnoreCase(cat.trim())) {
                                matched = true;
                                break;
                            }
                        }
                    }
                    if (matched) break;
                }
                if (matched) {
                    filteredList.add(product);
                }
            }
        }
        adapter.updateProducts(filteredList);
    }

    private void applyCombinedFilters() {
        filteredList.clear();
        for (Product product : productsList) {
            boolean matchesCategory = selectedCategories.isEmpty() || selectedCategories.contains("All");
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
                filteredList.add(product);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showCategoryMultiSelectDialog(String[] allCategories) {
        boolean[] checked = new boolean[allCategories.length];
        for (int i = 0; i < allCategories.length; i++) {
            checked[i] = selectedCategories.contains(allCategories[i]);
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Categories")
                .setMultiChoiceItems(allCategories, checked, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (allCategories[which].equals("All")) {
                            // If 'All' is checked, uncheck all others
                            selectedCategories.clear();
                            selectedCategories.add("All");
                            // Uncheck all others in the dialog
                            ListView list = ((AlertDialog) dialog).getListView();
                            for (int i = 1; i < allCategories.length; i++) {
                                list.setItemChecked(i, false);
                            }
                        } else {
                            // If any other category is checked, remove 'All' if present
                            selectedCategories.remove("All");
                            if (!selectedCategories.contains(allCategories[which]))
                                selectedCategories.add(allCategories[which]);
                            // Uncheck 'All' in the dialog
                            ListView list = ((AlertDialog) dialog).getListView();
                            list.setItemChecked(0, false);
                        }
                    } else {
                        selectedCategories.remove(allCategories[which]);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    if (selectedCategories.isEmpty()) {
                        selectedCategories.add("All");
                        etCategoryFilter.setText("All");
                        applyCombinedFilters();
                    } else if (selectedCategories.contains("All")) {
                        etCategoryFilter.setText("All");
                        applyCombinedFilters();
                    } else {
                        String cats = String.join(", ", selectedCategories);
                        etCategoryFilter.setText(cats);
                        applyCombinedFilters();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCartBadge() {
        int count = 0;
        for (Product p : cartItems) {
            count += p.getQuantity();
        }
        if (count > 0) {
            cartItemCount.setVisibility(View.VISIBLE);
            cartItemCount.setText(String.valueOf(count));
        } else {
            cartItemCount.setVisibility(View.GONE);
        }
    }

    private ArrayList<String> getUniqueCategories(ArrayList<Product> products) {
        ArrayList<String> categories = new ArrayList<>();
        for (Product product : products) {
            String cat = product.getCategory();
            if (cat != null && !cat.isEmpty() && !categories.contains(cat)) {
                categories.add(cat);
            }
        }
        return categories;
    }

    // TODO: Implement real logic to get current customer ID
    private int getCurrentCustomerId() {
        // Placeholder: return a fixed ID or fetch from SharedPreferences/Intent
        return 1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2002 && resultCode == RESULT_OK && data != null) {
            String scannedBarcode = data.getStringExtra("barcode");
            if (scannedBarcode != null) {
                // Find product by barcode
                for (Product product : productsList) {
                    if (product.getBarcode() != null && product.getBarcode().equals(scannedBarcode)) {
                        // Show quantity picker dialog (reuse add to cart logic)
                        showQuantityPickerDialog(product);
                        return;
                    }
                }
                Snackbar.make(findViewById(android.R.id.content), "Product not found for scanned barcode", Snackbar.LENGTH_LONG).show();
            }
        }
        if (resultCode == RESULT_OK) {
            // Reload products from DB to reflect restored stock
            productsList = dbHelper.getAllProducts();
            filteredList.clear();
            filteredList.addAll(productsList);
            adapter.notifyDataSetChanged();
        }
    }

    private void showQuantityPickerDialog(Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quantity_picker, null);
        TextView tvProductName = dialogView.findViewById(R.id.tvDialogProductName);
        TextView tvProductStock = dialogView.findViewById(R.id.tvDialogProductStock);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        tvProductName.setText(product.getName());
        tvProductStock.setText("Stock: " + product.getStock());
        etQuantity.setText("1");
        new AlertDialog.Builder(this)
            .setTitle("Add to Cart")
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                int qty = 1;
                try {
                    qty = Integer.parseInt(etQuantity.getText().toString());
                } catch (NumberFormatException ignored) {}
                if (qty < 1) qty = 1;
                if (qty > product.getStock()) qty = product.getStock();
                Product productCopy = new Product(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getStock(), product.getExpiryDate(), product.getManufacturer(), product.getImageUri(), product.getBarcode(), product.getCategory());
                productCopy.setQuantity(qty);
                addToCart(productCopy);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh header info after possible profile edit
        Customer customer = dbHelper.getCustomerById(customerId);
        if (customer != null) {
            View headerView = navView.getHeaderView(0);
            TextView tvProfileName = headerView.findViewById(R.id.tvProfileName);
            TextView tvProfilePhone = headerView.findViewById(R.id.tvProfilePhone);
            ImageView imgProfilePhoto = headerView.findViewById(R.id.imgProfilePhoto);
            tvProfileName.setText(customer.name);
            tvProfilePhone.setText(customer.phone);
            if (customer.profilePhotoUri != null && !customer.profilePhotoUri.isEmpty()) {
                try {
                    String path = Uri.parse(customer.profilePhotoUri).getPath();
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if (bitmap != null) {
                        Bitmap circularBitmap = getCircularBitmap(bitmap);
                        imgProfilePhoto.setImageBitmap(circularBitmap);
                    } else {
                        imgProfilePhoto.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } catch (Exception e) {
                    imgProfilePhoto.setImageResource(R.drawable.ic_profile_placeholder);
                }
            } else {
                imgProfilePhoto.setImageResource(R.drawable.ic_profile_placeholder);
            }
        }
        // --- ADDED: Refresh cart from DB and update badge ---
        cartItems = dbHelper.getCartItems(customerId);
        if (cartItems == null) cartItems = new ArrayList<>();
        updateCartBadge();
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        return output;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_customer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
