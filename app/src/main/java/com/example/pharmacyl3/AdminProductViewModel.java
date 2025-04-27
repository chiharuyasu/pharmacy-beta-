package com.example.pharmacyl3;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;

public class AdminProductViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Product>> productsLiveData = new MutableLiveData<>();

    public LiveData<ArrayList<Product>> getProductsLiveData() {
        return productsLiveData;
    }

    public void setProducts(ArrayList<Product> products) {
        productsLiveData.setValue(products);
    }
}
