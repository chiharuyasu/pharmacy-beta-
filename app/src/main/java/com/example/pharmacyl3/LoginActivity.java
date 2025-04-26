package com.example.pharmacyl3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmacyl3.AdminActivity;
import com.example.pharmacyl3.CustomerActivity;
import com.example.pharmacyl3.CustomerSignUpActivity;
import com.example.pharmacyl3.DBHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Set the layout we created

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvSignUp   = findViewById(R.id.tvSignUp);

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Admin login
                if(username.equals("admin") && password.equals("admin123")){
                    startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                } else {
                    // Customer login: check DB
                    DBHelper dbHelper = new DBHelper(LoginActivity.this);
                    if (dbHelper.validateCustomer(username, password)) {
                        startActivity(new Intent(LoginActivity.this, CustomerActivity.class));
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
