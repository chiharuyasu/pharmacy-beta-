package com.example.pharmacyl3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
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

public class CustomerEditProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 101;
    private ImageView imageViewProfileEdit;
    private EditText editTextName, editTextPhone, editTextEmail;
    private Button btnChangePhoto, btnSaveProfile;
    private Uri selectedImageUri;
    private int customerId;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_edit_profile);

        imageViewProfileEdit = findViewById(R.id.imageViewProfileEdit);
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
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
            if (customer.profilePhotoUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(customer.profilePhotoUri));
                    Bitmap circularBitmap = getCircularBitmap(bitmap);
                    imageViewProfileEdit.setImageBitmap(circularBitmap);
                } catch (Exception e) {
                    imageViewProfileEdit.setImageResource(R.drawable.ic_person);
                }
            }
            selectedImageUri = customer.profilePhotoUri != null ? Uri.parse(customer.profilePhotoUri) : null;
        }

        btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        btnSaveProfile.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Name and phone cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            dbHelper.updateCustomerProfile(customerId, name, phone, selectedImageUri != null ? selectedImageUri.toString() : null, null, null, null, null, email);
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
                    String fileName = "profile_photo_" + customerId + ".jpg";
                    String filePath = FileUtils.copyUriToInternalStorage(this, pickedImageUri, fileName);
                    selectedImageUri = Uri.fromFile(new java.io.File(filePath));
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    Bitmap circularBitmap = getCircularBitmap(bitmap);
                    imageViewProfileEdit.setImageBitmap(circularBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save profile photo", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        return output;
    }
}
