package com.example.contapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
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

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private CounterAdapter adapter;
    private FloatingActionButton fabCreate;
    private Spinner spinnerFilter;
    private Button btnJoin;
    private ApiService apiService;
    private String currentFilter = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // requireContext() sustituye a 'this'
        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);

        // view.findViewById para buscar en el trozo de pantalla
        recyclerView = view.findViewById(R.id.recyclerViewCounters);
        fabCreate = view.findViewById(R.id.fabCreate);
        btnJoin = view.findViewById(R.id.btnJoin);
        spinnerFilter = view.findViewById(R.id.spinnerFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CounterAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        setupSpinner();

        fabCreate.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_home_to_create);
        });

        btnJoin.setOnClickListener(v -> showJoinDialog());
    }

    private void setupSpinner() {
        // Las opciones que verá el usuario
        String[] options = {"Todos", "Abiertos", "Cerrados"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, options);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentFilter = null;
                } else if (position == 1) {
                    currentFilter = "open";
                } else if (position == 2) {
                    currentFilter = "closed";
                }

                loadCounters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        loadCounters();
    }

    private void loadCounters() {
        apiService.getMyCounters(currentFilter).enqueue(new Callback<List<Counter>>() {
            @Override
            public void onResponse(@NonNull Call<List<Counter>> call, @NonNull Response<List<Counter>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    adapter.setCounterList(response.body());
                } else {
                    Toast.makeText(requireContext(), "Error al cargar contadores", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Counter>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showJoinDialog() {
        // Los diálogos y componentes visuales creados desde código necesitan requireContext()
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Unirse a un Contador");
        builder.setMessage("Introduce el código de invitación:");

        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("Unirme", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                joinCounter(code);
            } else {
                Toast.makeText(requireContext(), "El código no puede estar vacío", Toast.LENGTH_SHORT).show();
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
            public void onResponse(@NonNull Call<Counter> call, @NonNull Response<Counter> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "¡Te has unido con éxito!", Toast.LENGTH_SHORT).show();
                    loadCounters();
                } else {
                    Toast.makeText(requireContext(), "Código inválido o ya eres miembro", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Counter> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red al unirse: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
