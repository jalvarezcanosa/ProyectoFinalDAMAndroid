package com.example.contapp.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

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
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivPreview.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_create_counter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        calendar = Calendar.getInstance();

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        btnPickDate = view.findViewById(R.id.btnPickDate);
        btnPickImage = view.findViewById(R.id.btnPickImage);
        btnSaveCounter = view.findViewById(R.id.btnSaveCounter);
        ivPreview = view.findViewById(R.id.ivPreview);

        btnPickDate.setOnClickListener(v -> showDateTimePicker());

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnSaveCounter.setOnClickListener(v -> uploadCounter());
    }

    private void showDateTimePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
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
            Toast.makeText(requireContext(), "El título es obligatorio", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            }
        }

        apiService.createCounter(titlePart, descPart, datePart, imagePart).enqueue(new Callback<Counter>() {
            @Override
            public void onResponse(Call<Counter> call, Response<Counter> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "¡Contador creado!", Toast.LENGTH_SHORT).show();
                    if (getView() != null) {
                        Navigation.findNavController(getView()).popBackStack();
                    }
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
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error al crear: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Counter> call, Throwable t) {
                if (!isAdded()) return;
                btnSaveCounter.setEnabled(true);
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) throws IOException{
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        File file = File.createTempFile("counter_img_", ".jpg", requireContext().getCacheDir());
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
