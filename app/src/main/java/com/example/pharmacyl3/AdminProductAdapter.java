package com.example.pharmacyl3;

import com.example.pharmacyl3.R;
import com.example.pharmacyl3.Product;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.io.File;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    private ArrayList<Product> products;
    private AdminProductListener listener;

    public interface AdminProductListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
        void onItemClick(Product product);
    }

    public AdminProductAdapter(ArrayList<Product> products, AdminProductListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(String.format("DZD %.2f", product.getPrice()));
        holder.tvProductDescription.setText(product.getDescription());
        holder.tvProductExpiryDate.setText("Expiry Date: " + product.getExpiryDate());
        holder.tvProductManufacturer.setText("Manufacturer: " + product.getManufacturer());
        
        ChipGroup chipGroup = holder.itemView.findViewById(R.id.chipGroupProductCategoriesAdmin);
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
                Uri uri = product.getImageUri().startsWith("/") ? Uri.fromFile(new File(product.getImageUri())) : Uri.parse(product.getImageUri());
                holder.ivProductImage.setImageURI(uri);
            } catch (Exception e) {
                holder.ivProductImage.setImageResource(R.drawable.ic_add_photo);
            }
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_add_photo);
        }
        
        Chip tvProductStock = holder.itemView.findViewById(R.id.tvProductStock);
        tvProductStock.setText("Stock: " + product.getStock());
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(product));
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
        TextView tvProductName, tvProductPrice, tvProductDescription, tvProductExpiryDate, tvProductManufacturer, tvProductStock;
        MaterialButton btnEdit, btnDelete;
        ImageView ivProductImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductExpiryDate = itemView.findViewById(R.id.tvProductExpiryDate);
            tvProductManufacturer = itemView.findViewById(R.id.tvProductManufacturer);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
        }
    }
} 