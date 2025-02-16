package com.example.pharmacyl3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmacyl3.AdminActivity;
import com.example.pharmacyl3.CustomerActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Set the layout we created

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Simple authentication logic:
                if(username.equals("admin") && password.equals("admin123")){
                    // Admin mode: Navigate to AdminActivity
                    startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                } else {
                    // Customer mode: Navigate to CustomerActivity
                    startActivity(new Intent(LoginActivity.this, CustomerActivity.class));
                }
            }
        });
    }
}
