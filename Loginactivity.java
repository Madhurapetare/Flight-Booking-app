package com.example.flightapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.flightapp.network.ApiClient;
import com.example.flightapp.network.ApiService;
import com.example.flightapp.util.PrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnLogin, btnRegister;
    ApiService api;
    PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        api = ApiClient.getApiService();
        pref = new PrefManager(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        // auto-login if token exists (optional)
        if (pref.getToken() != null) {
            startActivity(new Intent(this, SearchActivity.class));
            finish();
        }
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) return;

        Map<String,String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", pass);

        api.login(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                Map<String, Object> b = response.body();
                String token = (String) b.get("token");
                if (token != null) {
                    pref.saveToken(token);
                    startActivity(new Intent(LoginActivity.this, SearchActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) { t.printStackTrace(); }
        });
    }
}
