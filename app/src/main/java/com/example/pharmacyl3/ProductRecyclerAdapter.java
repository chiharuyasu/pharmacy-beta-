package com.example.pharmacyl3;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ViewHolder> {

    private ArrayList<Product> products;
    private OnItemClickListener itemClickListener;
    private ProductInteractionListener productInteractionListener;
    private boolean isCustomerView;

    // Interface for admin view
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    // Interface for customer view
    public interface ProductInteractionListener {
        void onItemClick(Product product);
        void onAddToCart(Product product);
    }

    // Constructor for admin view
    public ProductRecyclerAdapter(ArrayList<Product> products, OnItemClickListener listener) {
        this.products = products;
        this.itemClickListener = listener;
        this.isCustomerView = false;
    }

    // Constructor for customer view
    public ProductRecyclerAdapter(ArrayList<Product> products, ProductInteractionListener listener) {
        this.products = products;
        this.productInteractionListener = listener;
        this.isCustomerView = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(String.format("$%.2f", product.getPrice()));
        holder.tvProductDescription.setText(product.getDescription());
        
        ChipGroup chipGroup = holder.itemView.findViewById(R.id.chipGroupProductCategories);
        chipGroup.removeAllViews();
        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            String[] categories = product.getCategory().split(",");
            for (String cat : categories) {
                Chip chip = new Chip(holder.itemView.getContext());
                chip.setText(cat.trim());
                chip.setCheckable(false);
                chip.setClickable(false);
                chipGroup.addView(chip);
            }
        }

        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            try {
                // If it's a file path, use file:// URI
                Uri uri = product.getImageUri().startsWith("/") ? Uri.fromFile(new java.io.File(product.getImageUri())) : Uri.parse(product.getImageUri());
                holder.ivProductImage.setImageURI(uri);
            } catch (Exception e) {
                holder.ivProductImage.setImageResource(R.drawable.ic_add_photo);
            }
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_add_photo);
        }

        // Show/hide add to cart button based on view type
        if (isCustomerView) {
            holder.btnAddToCart.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(v -> productInteractionListener.onItemClick(product));
            // Disable add to cart if out of stock
            if (product.getStock() <= 0) {
                holder.btnAddToCart.setEnabled(false);
                holder.btnAddToCart.setText("Out of Stock");
                holder.btnAddToCart.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
                holder.btnAddToCart.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.primaryRed));
            } else {
                holder.btnAddToCart.setEnabled(true);
                holder.btnAddToCart.setText("Add to Cart");
                holder.btnAddToCart.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
                holder.btnAddToCart.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
                holder.btnAddToCart.setOnClickListener(v -> {
                    // Show quantity picker dialog
                    showQuantityPickerDialog(holder, product);
                });
            }
        } else {
            holder.btnAddToCart.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> itemClickListener.onItemClick(product));
        }
    }

    private void showQuantityPickerDialog(ViewHolder holder, Product product) {
        View dialogView = LayoutInflater.from(holder.itemView.getContext())
                .inflate(R.layout.dialog_quantity_picker, null);
        TextView tvProductName = dialogView.findViewById(R.id.tvDialogProductName);
        TextView tvProductStock = dialogView.findViewById(R.id.tvDialogProductStock);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        tvProductName.setText(product.getName());
        tvProductStock.setText("Stock: " + product.getStock());
        etQuantity.setText("1");
        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Select Quantity")
                .setView(dialogView)
                .setPositiveButton("Add to Cart", (dialog, which) -> {
                    int qty = 1;
                    try {
                        qty = Integer.parseInt(etQuantity.getText().toString());
                    } catch (Exception ignored) {}
                    if (qty < 1) qty = 1;
                    if (qty > product.getStock()) qty = product.getStock();
                    Product productCopy = new Product(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getStock(), product.getExpiryDate(), product.getManufacturer(), product.getImageUri(), product.getBarcode(), product.getCategory());
                    productCopy.setQuantity(qty);
                    productInteractionListener.onAddToCart(productCopy);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(ArrayList<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductDescription;
        MaterialButton btnAddToCart;
        ImageView ivProductImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
