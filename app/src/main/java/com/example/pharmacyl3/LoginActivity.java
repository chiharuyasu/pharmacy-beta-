package com.example.pharmacyl3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmacyl3.AdminActivity;
import com.example.pharmacyl3.CustomerActivity;
import com.example.pharmacyl3.CustomerSignUpActivity;
import com.example.pharmacyl3.DBHelper;
import com.example.pharmacyl3.Customer;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private CheckBox cbRememberMe;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Set the layout we created

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvSignUp   = findViewById(R.id.tvSignUp);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        // Autofill if remembered
        String savedEmail = prefs.getString("REMEMBERED_EMAIL", "");
        String savedPassword = prefs.getString("REMEMBERED_PASSWORD", "");
        boolean isRemembered = prefs.getBoolean("REMEMBER_ME", false);
        if (isRemembered) {
            etUsername.setText(savedEmail);
            etPassword.setText(savedPassword);
            cbRememberMe.setChecked(true);
        }

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Admin login
                if(username.equals("admin") && password.equals("admin123")){
                    if (cbRememberMe.isChecked()) {
                        prefs.edit()
                                .putString("REMEMBERED_EMAIL", username)
                                .putString("REMEMBERED_PASSWORD", password)
                                .putBoolean("REMEMBER_ME", true)
                                .apply();
                    } else {
                        prefs.edit()
                                .remove("REMEMBERED_EMAIL")
                                .remove("REMEMBERED_PASSWORD")
                                .putBoolean("REMEMBER_ME", false)
                                .apply();
                    }
                    startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                } else {
                    // Customer login: check DB
                    DBHelper dbHelper = new DBHelper(LoginActivity.this);
                    if (dbHelper.validateCustomer(username, password)) {
                        if (cbRememberMe.isChecked()) {
                            prefs.edit()
                                    .putString("REMEMBERED_EMAIL", username)
                                    .putString("REMEMBERED_PASSWORD", password)
                                    .putBoolean("REMEMBER_ME", true)
                                    .apply();
                        } else {
                            prefs.edit()
                                    .remove("REMEMBERED_EMAIL")
                                    .remove("REMEMBERED_PASSWORD")
                                    .putBoolean("REMEMBER_ME", false)
                                    .apply();
                        }
                        // Fetch customer and pass ID to CustomerActivity
                        Customer customer = dbHelper.getCustomerByEmail(username);
                        if (customer != null) {
                            Intent intent = new Intent(LoginActivity.this, CustomerActivity.class);
                            intent.putExtra("customerId", customer.id);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CustomerSignUpActivity.class));
            }
        });
    }
}
