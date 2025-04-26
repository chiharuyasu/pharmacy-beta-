package com.example.pharmacyl3;

public class Customer {
    public int id;
    public String name;
    public String email;
    public String phone;
    public String profilePhotoUri;
    public String licenseNumber;
    public String pharmacyName;
    public String pharmacyAddress;
    public String experience;

    public Customer(int id, String name, String email, String phone, String profilePhotoUri, String licenseNumber, String pharmacyName, String pharmacyAddress, String experience) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profilePhotoUri = profilePhotoUri;
        this.licenseNumber = licenseNumber;
        this.pharmacyName = pharmacyName;
        this.pharmacyAddress = pharmacyAddress;
        this.experience = experience;
    }
}
