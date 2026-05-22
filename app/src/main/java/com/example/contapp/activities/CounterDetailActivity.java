package com.example.contapp.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.contapp.R;
import com.example.contapp.models.Counter;
import com.example.contapp.models.CounterDetailResponse;
import com.example.contapp.network.ApiClient;
import com.example.contapp.network.ApiService;
import com.example.contapp.adapters.ParticipantAdapter;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CounterDetailActivity extends AppCompatActivity {

    private int counterId;
    private ApiService apiService;
    private ImageView ivCover;
    private TextView tvCounterTitle;
    private TextView tvCounterStatus;
    private TextView tvCounterDescription;
    private TextView tvInviteCode;
    private ImageButton btnCopyCode;
    private String currentInviteCode = "";
    private TextView tvCounterDates;
    private TextView tvCounterGlobalCount;
    private TextView tvCounterIndividualCount;
    private Button btnEdit;
    private Button btnDelete;
    private ExtendedFloatingActionButton fabIncrement;
    private LinearLayout llAdminActions;
    private RecyclerView rvRanking;
    private ParticipantAdapter participantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter_detail);

        counterId = getIntent().getIntExtra("COUNTER_ID", -1);
        if (counterId == -1) {
            Toast.makeText(this, "Error: Contador no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient(this).create(ApiService.class);

        ivCover = findViewById(R.id.ivCover);
        tvCounterTitle = findViewById(R.id.tvCounterTitle);
        tvCounterStatus = findViewById(R.id.tvCounterStatus);
        tvCounterDescription = findViewById(R.id.tvCounterDescription);
        tvInviteCode = findViewById(R.id.tvInviteCode);
        btnCopyCode = findViewById(R.id.btnCopyCode);
        tvCounterDates = findViewById(R.id.tvCounterDates);
        tvCounterGlobalCount = findViewById(R.id.tvCounterGlobalCount);
        tvCounterIndividualCount = findViewById(R.id.tvCounterIndividualCount);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        fabIncrement = findViewById(R.id.fabIncrement);
        llAdminActions = findViewById(R.id.llAdminActions);
        rvRanking = findViewById(R.id.rvRanking);

        rvRanking.setLayoutManager(new LinearLayoutManager(this));
        participantAdapter = new ParticipantAdapter();
        rvRanking.setAdapter(participantAdapter);

       fabIncrement.setOnClickListener(v -> incrementCounter());

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(CounterDetailActivity.this, EditCounterActivity.class);
            intent.putExtra("COUNTER_ID", counterId);
            startActivity(intent);
        });

        btnCopyCode.setOnClickListener(v -> {
            if (!currentInviteCode.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Código de Invitación", currentInviteCode);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Código copiado al portapapeles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCounterDetails();
    }

    private void loadCounterDetails() {
        apiService.getCounterDetail(counterId).enqueue(new Callback<CounterDetailResponse>() {
            @Override
            public void onResponse(Call<CounterDetailResponse> call, Response<CounterDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CounterDetailResponse counter = response.body();
                    updateUI(counter);
                } else {
                    Toast.makeText(CounterDetailActivity.this, "Error al cargar detalles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CounterDetailResponse> call, Throwable t) {
                Toast.makeText(CounterDetailActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(CounterDetailResponse counter) {
        tvCounterTitle.setText(counter.getTitle());
        tvCounterDescription.setText(counter.getDescription() != null ? counter.getDescription() : "Sin descripción");
        tvCounterGlobalCount.setText(String.valueOf(counter.getGlobalCount()));
        tvCounterIndividualCount.setText(String.valueOf(counter.getIndividualCount()));
        currentInviteCode = counter.getInviteCode();
        if (currentInviteCode != null && !currentInviteCode.isEmpty()) {
            tvInviteCode.setText(currentInviteCode);
            btnCopyCode.setVisibility(View.VISIBLE);
        } else {
            tvInviteCode.setText("No disponible");
            btnCopyCode.setVisibility(View.GONE);
        }

        String closedAt = counter.getClosedAt();
        if (closedAt != null && !closedAt.isEmpty()) {
            try {
                java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault());
                java.util.Date date = isoFormat.parse(closedAt);

                java.text.SimpleDateFormat friendlyFormat = new java.text.SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", java.util.Locale.getDefault());

                tvCounterDates.setText(String.format("Cierre el: " + friendlyFormat.format(date)));
            } catch (Exception e){
                tvCounterDates.setText("Cierre el: "+ closedAt);
            }
        } else {
            tvCounterDates.setText("Sin límite de fecha");
        }

        if ("closed".equalsIgnoreCase(counter.getStatus())) {
            tvCounterStatus.setText("Estado: CERRADO");
            tvCounterStatus.setTextColor(Color.RED);
            fabIncrement.setEnabled(false);
            fabIncrement.setVisibility(View.GONE);
            btnEdit.setEnabled(false);
        } else {
            tvCounterStatus.setText("Estado: ABIERTO");
            tvCounterStatus.setTextColor(Color.parseColor("#2E7D32"));
            fabIncrement.setEnabled(true);
            fabIncrement.setVisibility(View.VISIBLE);
            btnEdit.setEnabled(true);
        }

        if (counter.getImageUrl() != null && !counter.getImageUrl().isEmpty()) {
            Glide.with(this).load(counter.getImageUrl()).into(ivCover);
        }

        if (counter.getRanking() != null) {
            participantAdapter.setParticipantList(counter.getRanking());
        }

        if(counter.isCreator()) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            if (llAdminActions != null) {
                llAdminActions.setVisibility(View.VISIBLE);
            }
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            if (llAdminActions != null) {
                llAdminActions.setVisibility(View.GONE);
            }
        }
    }

    private void incrementCounter() {
        fabIncrement.setEnabled(false);

        apiService.incrementCounter(counterId).enqueue(new Callback<Counter>() {
            @Override
            public void onResponse(Call<Counter> call, Response<Counter> response) {
                fabIncrement.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    tvCounterIndividualCount.setText(String.valueOf(response.body().getIndividualCount()));
                    tvCounterGlobalCount.setText(String.valueOf(response.body().getGlobalCount()));

                    loadCounterDetails();
                } else {
                    Toast.makeText(CounterDetailActivity.this, "Error al sumar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Counter> call, Throwable t) {
                fabIncrement.setEnabled(true);
                Toast.makeText(CounterDetailActivity.this, "Error de red al incrementar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Borrar Contador")
                .setMessage("¿Estás seguro de que quieres borrar este contador de forma permanente? Se perderán todas las puntuaciones.")
                .setPositiveButton("Borrar", (dialog, which) -> deleteCounter())
                .setNegativeButton("Cancelar", null).show();
    }

    private void deleteCounter() {
        apiService.deleteCounter(counterId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CounterDetailActivity.this, "Contador borrado", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    if (response.code() == 403) {
                        Toast.makeText(CounterDetailActivity.this, "Solo el creador puede borrarlo (Error " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CounterDetailActivity.this, "Error al borrar el contador: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CounterDetailActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
