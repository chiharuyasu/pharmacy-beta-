package com.example.pharmacyl3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
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
        holder.tvProductDescription.setText(product.getDescription());
        holder.tvProductPrice.setText(String.format("$%.2f", product.getPrice()));

        // Show/hide add to cart button based on view type
        if (isCustomerView) {
            holder.btnAddToCart.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(v -> productInteractionListener.onItemClick(product));
            holder.btnAddToCart.setOnClickListener(v -> productInteractionListener.onAddToCart(product));
        } else {
            holder.btnAddToCart.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> itemClickListener.onItemClick(product));
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvProductDescription;
        TextView tvProductPrice;
        MaterialButton btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
