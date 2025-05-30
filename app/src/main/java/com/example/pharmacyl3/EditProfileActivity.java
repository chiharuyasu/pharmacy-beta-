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

public class EditProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 101;
    private ImageView imageViewProfileEdit;
    private EditText editTextName, editTextPhone, editTextLicenseNumber, editTextPharmacyName, editTextPharmacyAddress, editTextExperience, editTextEmail;
    private Button btnChangePhoto, btnSaveProfile;
    private Uri selectedImageUri;
    private int customerId;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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

        customerId = getIntent().getIntExtra("customerId", -1);
        dbHelper = new DBHelper(this);
        Customer customer = dbHelper.getCustomerById(customerId);
        if (customer != null) {
            editTextName.setText(customer.name);
            editTextPhone.setText(customer.phone);
            editTextEmail.setText(customer.email);
            editTextLicenseNumber.setText(customer.licenseNumber);
            editTextPharmacyName.setText(customer.pharmacyName);
            editTextPharmacyAddress.setText(customer.pharmacyAddress);
            editTextExperience.setText(customer.experience);
            if (customer.profilePhotoUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(customer.profilePhotoUri));
                    imageViewProfileEdit.setImageBitmap(bitmap);
                } catch (Exception e) {
                    imageViewProfileEdit.setImageResource(R.drawable.ic_person);
                }
            }
        }
        selectedImageUri = customer != null ? Uri.parse(customer.profilePhotoUri) : null;

        btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        btnSaveProfile.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            String licenseNumber = editTextLicenseNumber.getText().toString().trim();
            String pharmacyName = editTextPharmacyName.getText().toString().trim();
            String pharmacyAddress = editTextPharmacyAddress.getText().toString().trim();
            String experience = editTextExperience.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Name and phone cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            dbHelper.updateCustomerProfile(customerId, name, phone, selectedImageUri != null ? selectedImageUri.toString() : null, licenseNumber, pharmacyName, pharmacyAddress, experience, email);
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
                    // Copy to internal storage for persistence
                    String fileName = "profile_photo.jpg";
                    String filePath = FileUtils.copyUriToInternalStorage(this, pickedImageUri, fileName);
                    selectedImageUri = Uri.fromFile(new java.io.File(filePath));
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
