package com.example.pharmacyl3;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class ProfileManager {
    private static final String PREFS_NAME = "pharmacist_profile";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PROFILE_PIC = "profile_pic";

    public static void saveProfile(Context context, String name, String phone, Uri profilePicUri) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_PROFILE_PIC, profilePicUri != null ? profilePicUri.toString() : null);
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
}
