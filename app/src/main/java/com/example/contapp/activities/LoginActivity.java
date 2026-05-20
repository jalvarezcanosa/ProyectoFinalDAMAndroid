package com.example.contapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.contapp.R;
import com.example.contapp.models.AuthResponse;
import com.example.contapp.network.ApiClient;
import com.example.contapp.network.ApiService;
import com.example.contapp.utils.SessionManager;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvToggleMode;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etTelephone;
    private EditText etPassword;
    private Button btnAction;

    private SessionManager sessionManager;
    private ApiService apiService;

    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(this).create(ApiService.class);

        if (sessionManager.isLoggedIn()) {
            goToHome();
            return;
        }

        tvTitle = findViewById(R.id.tvTitle);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etTelephone = findViewById(R.id.etTelephone);
        etPassword = findViewById(R.id.etPassword);
        btnAction = findViewById(R.id.btnAction);
        tvToggleMode = findViewById(R.id.tvToggleMode);

        tvToggleMode.setOnClickListener(v -> toggleMode());
        btnAction.setOnClickListener(v -> handleAuthAction());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode){
            tvTitle.setText("¡Hola de Nuevo!");
            btnAction.setText("Entrar");
            tvToggleMode.setText("¿No tienes cuenta? Regístrate aquí");
            etUsername.setVisibility(View.GONE);
            etTelephone.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Crear cuenta");
            btnAction.setText("Registrarse");
            tvToggleMode.setText("¿Ya tienes cuenta? Inicia Sesión");
            etUsername.setVisibility(View.VISIBLE);
            etTelephone.setVisibility(View.VISIBLE);
        }
    }

    private void handleAuthAction() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String telephone = etTelephone.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAction.setEnabled(false);

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        Call<AuthResponse> call;

        if (isLoginMode) {
            call = apiService.login(requestBody);
        } else {
            if(username.isEmpty()) {
                Toast.makeText(this, "El nombre de usuario es obligatorio para crear una cuenta", Toast.LENGTH_SHORT).show();
                btnAction.setEnabled(true);
                return;
            }
            requestBody.put("username", username);
            requestBody.put("telephone", telephone);
            call = apiService.register(requestBody);
        }

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnAction.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    sessionManager.saveAuthToken(token);
                    Toast.makeText(LoginActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                    goToHome();
                } else {
                    if (isLoginMode) {
                        Toast.makeText(LoginActivity.this, "Error en las credenciales", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Usuario existente o error en el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnAction.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
