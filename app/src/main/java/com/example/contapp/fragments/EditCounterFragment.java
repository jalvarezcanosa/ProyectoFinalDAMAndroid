package com.example.contapp.fragments;

import static android.app.Activity.RESULT_OK;

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

import com.bumptech.glide.Glide;
import com.example.contapp.R;
import com.example.contapp.models.Counter;
import com.example.contapp.models.CounterDetailResponse;
import com.example.contapp.network.ApiClient;
import com.example.contapp.network.ApiService;

import java.io.File;
import java.io.FileNotFoundException;
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

public class EditCounterFragment extends Fragment {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_counter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            counterId = getArguments().getInt("COUNTER_ID", -1);
        }

        if (counterId == -1){
            Toast.makeText(requireContext(), "Error al cargar el contador", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        calendar = Calendar.getInstance();

        etEditTitle = view.findViewById(R.id.etEditTitle);
        etEditDescription = view.findViewById(R.id.etEditDescription);
        tvEditSelectedDate = view.findViewById(R.id.tvEditSelectedDate);
        btnEditPickDate = view.findViewById(R.id.btnEditPickDate);
        btnEditPickImage = view.findViewById(R.id.btnEditPickImage);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        ivEditPreview = view.findViewById(R.id.ivEditPreview);

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
            public void onResponse(@NonNull Call<CounterDetailResponse> call, @NonNull Response<CounterDetailResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    CounterDetailResponse counter = response.body();
                    etEditTitle.setText(counter.getTitle());
                    etEditDescription.setText(counter.getDescription());
                    String closedAt = counter.getClosedAt();

                    if (closedAt != null) {
                        tvEditSelectedDate.setText(closedAt);
                        try {
                            java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault());
                            java.util.Date date = isoFormat.parse(closedAt);
                            if (date != null) calendar.setTime(date);

                            java.text.SimpleDateFormat friendlyFormat = new java.text.SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", java.util.Locale.getDefault());

                            tvEditSelectedDate.setText(String.format("Cierre el: " + friendlyFormat.format(date)));
                        } catch (Exception e){
                            tvEditSelectedDate.setText("Cierre el: "+ closedAt);
                        }
                    }

                    if (counter.getImageUrl() != null && !counter.getImageUrl().isEmpty()) {
                        currentImageUrl = counter.getImageUrl();
                        Glide.with(requireContext()).load(currentImageUrl).into(ivEditPreview);
                    }
                } else {
                    Toast.makeText(requireContext(), "No se pudieron cargar los datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CounterDetailResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
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
                Toast.makeText(requireContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            }
        }

        apiService.updateCounter(counterId, titlePart, descPart, datePart, imagePart).enqueue(new Callback<Counter>() {
            @Override
            public void onResponse(@NonNull Call<Counter> call, @NonNull Response<Counter> response) {
                if (!isAdded()) return;
                btnSaveChanges.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "¡Contador actualizado con éxito!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar el contador", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Counter> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnSaveChanges.setEnabled(true);
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) throws IOException {
        if (getContext() == null) throw new IOException("Context is null");
        InputStream inputStream = null;
        try {
            inputStream = requireContext().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        File file = File.createTempFile("counter_img_edit_", ".jpg", requireContext().getCacheDir());
        try (FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.close();
        }
        inputStream.close();
        return file;
    }

}
