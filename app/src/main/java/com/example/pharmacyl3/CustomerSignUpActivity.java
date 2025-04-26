package com.example.pharmacyl3;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CustomerSignUpActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_sign_up);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        dbHelper = new DBHelper(this);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                        TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(CustomerSignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(CustomerSignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (dbHelper.customerExists(email)) {
                    Toast.makeText(CustomerSignUpActivity.this, "Email already registered", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean inserted = dbHelper.insertCustomer(name, email, password);
                if (inserted) {
                    Toast.makeText(CustomerSignUpActivity.this, "Sign up successful! Please log in.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CustomerSignUpActivity.this, "Sign up failed. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
