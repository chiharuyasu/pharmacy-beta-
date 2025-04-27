package com.example.pharmacyl3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class AdminEditProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 101;
    private ImageView imageViewProfileEdit;
    private EditText editTextName, editTextPhone, editTextLicenseNumber, editTextPharmacyName, editTextPharmacyAddress, editTextExperience, editTextEmail;
    private Button btnChangePhoto, btnSaveProfile;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_profile);

        imageViewProfileEdit = findViewById(R.id.imageViewProfileEdit);
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextLicenseNumber = findViewById(R.id.editTextLicenseNumber);
        editTextPharmacyName = findViewById(R.id.editTextPharmacyName);
        editTextPharmacyAddress = findViewById(R.id.editTextPharmacyAddress);
        editTextExperience = findViewById(R.id.editTextExperience);
        editTextEmail = findViewById(R.id.editTextEmail);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Load admin data from ProfileManager
        String name = ProfileManager.getName(this);
        String phone = ProfileManager.getPhone(this);
        String licenseNumber = ProfileManager.getLicenseNumber(this);
        String pharmacyName = ProfileManager.getPharmacyName(this);
        String pharmacyAddress = ProfileManager.getPharmacyAddress(this);
        String experience = ProfileManager.getExperience(this);
        String email = ProfileManager.getEmail(this);
        Uri profilePhotoUri = ProfileManager.getProfilePicUri(this);

        editTextName.setText(name);
        editTextPhone.setText(phone);
        editTextLicenseNumber.setText(licenseNumber);
        editTextPharmacyName.setText(pharmacyName);
        editTextPharmacyAddress.setText(pharmacyAddress);
        editTextExperience.setText(experience);
        editTextEmail.setText(email);
        if (profilePhotoUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), profilePhotoUri);
                imageViewProfileEdit.setImageBitmap(bitmap);
            } catch (Exception e) {
                imageViewProfileEdit.setImageResource(R.drawable.ic_person);
            }
        }
        selectedImageUri = profilePhotoUri;

        btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        btnSaveProfile.setOnClickListener(v -> {
            String updatedName = editTextName.getText().toString().trim();
            String updatedPhone = editTextPhone.getText().toString().trim();
            String updatedLicenseNumber = editTextLicenseNumber.getText().toString().trim();
            String updatedPharmacyName = editTextPharmacyName.getText().toString().trim();
            String updatedPharmacyAddress = editTextPharmacyAddress.getText().toString().trim();
            String updatedExperience = editTextExperience.getText().toString().trim();
            String updatedEmail = editTextEmail.getText().toString().trim();
            if (updatedName.isEmpty() || updatedPhone.isEmpty()) {
                Toast.makeText(this, "Name and phone cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            ProfileManager.saveProfile(
                this,
                updatedName,
                updatedPhone,
                selectedImageUri,
                updatedLicenseNumber,
                updatedPharmacyName,
                updatedPharmacyAddress,
                updatedExperience,
                updatedEmail
            );
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri pickedImageUri = data.getData();
            if (pickedImageUri != null) {
                try {
                    selectedImageUri = pickedImageUri;
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageViewProfileEdit.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save profile photo", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
