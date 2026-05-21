package com.example.contapp.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.contapp.R;
import com.example.contapp.models.Counter;
import com.example.contapp.models.CounterDetailResponse;
import com.example.contapp.network.ApiClient;
import com.example.contapp.network.ApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCounterActivity extends AppCompatActivity {
    private int counterId;
    private EditText etEditTitle;
    private EditText etEditDescription;
    private TextView tvEditSelectedDate;
    private Button btnEditPickDate;
    private Button btnEditPickImage;
    private Button btnSaveChanges;
    private ImageView ivEditPreview;

    private Calendar calendar;
    private Uri selectedImageUri = null;
    private String currentImageUrl = null;
    private ApiService apiService;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivEditPreview.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_counter);

        counterId = getIntent().getIntExtra("COUNTER_ID", -1);
        if (counterId == -1){
            Toast.makeText(this, "Error al cargar el contador", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient(this).create(ApiService.class);
        calendar = Calendar.getInstance();

        etEditTitle = findViewById(R.id.etEditTitle);
        etEditDescription = findViewById(R.id.etEditDescription);
        tvEditSelectedDate = findViewById(R.id.tvEditSelectedDate);
        btnEditPickDate = findViewById(R.id.btnEditPickDate);
        btnEditPickImage = findViewById(R.id.btnEditPickImage);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        ivEditPreview = findViewById(R.id.ivEditPreview);

        btnEditPickDate.setOnClickListener(v -> showDateTimePicker());

        btnEditPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnSaveChanges.setOnClickListener(v -> uploadChanges());

        loadCurrentData();
    }

    private void loadCurrentData() {
        apiService.getCounterDetail(counterId).enqueue(new Callback<CounterDetailResponse>() {
            @Override
            public void onResponse(Call<CounterDetailResponse> call, Response<CounterDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CounterDetailResponse counter = response.body();
                    etEditTitle.setText(counter.getTitle());
                    etEditDescription.setText(counter.getDescription());

                    if (counter.getClosedAt() != null) {
                        tvEditSelectedDate.setText(counter.getClosedAt());
                    }

                    if (counter.getImageUrl() != null && !counter.getImageUrl().isEmpty()) {
                        currentImageUrl = counter.getImageUrl();
                        Glide.with(EditCounterActivity.this).load(currentImageUrl).into(ivEditPreview);
                    }
                } else {
                    Toast.makeText(EditCounterActivity.this, "No se pudieron cargar los datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CounterDetailResponse> call, Throwable t) {
                Toast.makeText(EditCounterActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDateTimePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvEditSelectedDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void uploadChanges() {
        String title = etEditTitle.getText().toString().trim();
        String description = etEditDescription.getText().toString().trim();
        String dateString = tvEditSelectedDate.getText().toString().trim();

        if (title.isEmpty()) {
            etEditTitle.setError("El título es obligatorio");
            return;
        }

        btnSaveChanges.setEnabled(false);

        RequestBody titlePart = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody descPart = RequestBody.create(MediaType.parse("text/plain"), description);

        if (dateString.equals("No seleccionada")) dateString = "";
        RequestBody datePart = RequestBody.create(MediaType.parse("text/plain"), dateString);

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null){
            try{
                File file = getFileFromUri(selectedImageUri);
                RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
                imagePart = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            }
        }

        apiService.updateCounter(counterId, titlePart, descPart, datePart, imagePart).enqueue(new Callback<Counter>() {
            @Override
            public void onResponse(Call<Counter> call, Response<Counter> response) {
                btnSaveChanges.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(EditCounterActivity.this, "¡Contador actualizado con éxito!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditCounterActivity.this, "Error al actualizar el contador", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Counter> call, Throwable t) {
                btnSaveChanges.setEnabled(true);
                Toast.makeText(EditCounterActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File file = File.createTempFile("counter_img_edit_", ".jpg", getCacheDir());
        FileOutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        out.close();
        if (inputStream != null) inputStream.close();
        return file;
    }

}
