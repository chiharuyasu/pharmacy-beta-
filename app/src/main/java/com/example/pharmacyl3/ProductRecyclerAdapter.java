package com.example.pharmacyl3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ViewHolder> {

    private ArrayList<Product> products;
    private OnItemClickListener listener;

    // Define an interface for item clicks
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    // Constructor for adapter
    public ProductRecyclerAdapter(ArrayList<Product> products, OnItemClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    // ViewHolder class holds references to each item's views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName, tvPrice, tvStock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
        }

        // Bind method sets the product data and click listener
        public void bind(final Product product, final OnItemClickListener listener) {
            tvName.setText(product.getName());
            tvPrice.setText("$" + product.getPrice());
            tvStock.setText("Stock: " + product.getStock());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(product);
                }
            });
        }
    }

    // Inflate the item layout and create the ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_item, parent, false);
        return new ViewHolder(view);
    }

    // Bind the data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener);
    }

    // Return the total number of items
    @Override
    public int getItemCount() {
        return products.size();
    }
}
