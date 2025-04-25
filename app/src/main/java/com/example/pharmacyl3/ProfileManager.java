package com.example.pharmacyl3;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class ProfileManager {
    private static final String PREFS_NAME = "pharmacist_profile";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PROFILE_PIC = "profile_pic";
    private static final String KEY_LICENSE_NUMBER = "license_number";
    private static final String KEY_PHARMACY_NAME = "pharmacy_name";
    private static final String KEY_PHARMACY_ADDRESS = "pharmacy_address";
    private static final String KEY_EXPERIENCE = "experience";
    private static final String KEY_EMAIL = "email";

    public static void saveProfile(Context context, String name, String phone, Uri profilePicUri, String licenseNumber, String pharmacyName, String pharmacyAddress, String experience, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_PROFILE_PIC, profilePicUri != null ? profilePicUri.toString() : null);
        editor.putString(KEY_LICENSE_NUMBER, licenseNumber);
        editor.putString(KEY_PHARMACY_NAME, pharmacyName);
        editor.putString(KEY_PHARMACY_ADDRESS, pharmacyAddress);
        editor.putString(KEY_EXPERIENCE, experience);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public static String getName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_NAME, "");
    }

    public static String getPhone(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PHONE, "");
    }

    public static Uri getProfilePicUri(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uri = prefs.getString(KEY_PROFILE_PIC, null);
        return uri != null ? Uri.parse(uri) : null;
    }

    public static String getLicenseNumber(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LICENSE_NUMBER, "");
    }

    public static String getPharmacyName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PHARMACY_NAME, "");
    }

    public static String getPharmacyAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PHARMACY_ADDRESS, "");
    }

    public static String getExperience(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EXPERIENCE, "");
    }

    public static String getEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EMAIL, "");
    }
}
