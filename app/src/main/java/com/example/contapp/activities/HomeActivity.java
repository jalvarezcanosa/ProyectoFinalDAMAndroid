package com.example.contapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contapp.R;
import com.example.contapp.adapters.CounterAdapter;
import com.example.contapp.models.Counter;
import com.example.contapp.network.ApiClient;
import com.example.contapp.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CounterAdapter adapter;
    private FloatingActionButton fabCreate;
    private Button btnJoin;
    private ApiService apiService;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        recyclerView = findViewById(R.id.recyclerViewCounters);
        fabCreate = findViewById(R.id.fabCreate);
        btnJoin = findViewById(R.id.btnJoin);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CounterAdapter(this);
        recyclerView.setAdapter(adapter);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        fabCreate.setOnClickListener(v-> {
        Intent intent = new Intent(HomeActivity.this, CreateCounterActivity.class);
        startActivity(intent);
        });

        btnJoin.setOnClickListener(v -> showJoinDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCounters();
    }

    private void loadCounters() {
        apiService.getMyCounters().enqueue(new Callback<List<Counter>>() {
            @Override
            public void onResponse(Call<List<Counter>> call, Response<List<Counter>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setCounterList(response.body());
                } else {
                    Toast.makeText(HomeActivity.this, "Error al cargar contadores", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Counter>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showJoinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unirse a un Contador");
        builder.setMessage("Introduce el código de invitación:");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Unirme", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                joinCounter(code);
            } else {
                Toast.makeText(HomeActivity.this, "El código no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void joinCounter(String inviteCode) {
        HashMap<String, String> body = new HashMap<>();
        body.put("invite_code", inviteCode);

        apiService.joinCounter(body).enqueue(new Callback<Counter>() {
            @Override
            public void onResponse(Call<Counter> call, Response<Counter> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "¡Te has unido con éxito!", Toast.LENGTH_SHORT).show();
                    loadCounters();
                } else {
                    Toast.makeText(HomeActivity.this, "Código inválido o ya eres miembro", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Counter> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error de red al unirse: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
