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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contapp.R;
import com.example.contapp.models.Counter;
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

public class CreateCounterFragment extends Fragment {
    private EditText etTitle;
    private EditText etDescription;
    private TextView tvSelectedDate;
    private Button btnPickDate;
    private Button btnPickImage;
    private Button btnSaveCounter;
    private ImageView ivPreview;

    private Calendar calendar;
    private Uri selectedImageUri = null;
    private ApiService apiService;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivPreview.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_counter);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        calendar = Calendar.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnSaveCounter = findViewById(R.id.btnSaveCounter);
        ivPreview = findViewById(R.id.ivPreview);

        btnPickDate.setOnClickListener(v -> showDateTimePicker());

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnSaveCounter.setOnClickListener(v -> uploadCounter());
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
                tvSelectedDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void uploadCounter() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveCounter.setEnabled(false);

        RequestBody titlePart = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody descPart = RequestBody.create(MediaType.parse("text/plain"), description);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String dateString = tvSelectedDate.getText().toString().equals("No seleccionada") ? "" : isoFormat.format(calendar.getTime());
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

        apiService.createCounter(titlePart, descPart, datePart, imagePart).enqueue(new Callback<Counter>() {
            @Override
            public void onResponse(Call<Counter> call, Response<Counter> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateCounterFragment.this, "¡Contador creado!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnSaveCounter.setEnabled(true);

                    try {
                        String errorJsonString = response.errorBody().string();

                        org.json.JSONObject jsonObject = new org.json.JSONObject(errorJsonString);
                        String errorMessage = "Error desconocido";

                        if (jsonObject.has("error")) {
                            errorMessage = jsonObject.getString("error");
                        } else if (jsonObject.has("message")) {
                            errorMessage = jsonObject.getString("message");
                        }
                        Toast.makeText(CreateCounterFragment.this, errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(CreateCounterFragment.this, "Error al crear: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Counter> call, Throwable t) {
                btnSaveCounter.setEnabled(true);
                Toast.makeText(CreateCounterFragment.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) throws IOException{
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File file = File.createTempFile("counter_img_", ".jpg", getCacheDir());
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
