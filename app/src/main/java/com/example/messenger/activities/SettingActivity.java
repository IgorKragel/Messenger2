package com.example.messenger.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.messenger.databinding.ActivityChatBinding;
import com.example.messenger.databinding.ActivitySettingBinding;
import com.example.messenger.tools.Constants;
import com.example.messenger.tools.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    private ActivitySettingBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding.backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.saveSettingButton.setOnClickListener(v -> settingSave());
    }

    private void settingSave(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        String newName = binding.inputName.getText().toString().trim();
        String newPassword = binding.inputPassword.getText().toString().trim();
        String newPasswordConfirm = binding.inputConfirmPassword.getText().toString().trim();

        // Создаем объект HashMap с полями для обновления
        HashMap<String, Object> user = new HashMap<>();
        if(!newName.isEmpty()) {
            user.put(Constants.KEY_NAME, newName);
        }
        if(newPassword.equals(newPasswordConfirm) && !newPassword.isEmpty()){
            user.put(Constants.KEY_PASSWORD, newPassword);
        }
        if(encodedImage != null){
            user.put(Constants.KEY_IMAGE, encodedImage);
        }

        // Обновляем документ пользователя в Firestore
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserId)
                .update(user)
                .addOnSuccessListener(aVoid -> {
                    // Обновляем данные в PreferenceManager
                    preferenceManager.putString(Constants.KEY_NAME, newName);
                    preferenceManager.putString(Constants.KEY_PASSWORD, newPassword);
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Toast.makeText(getApplicationContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Ошибка сохранения настроек: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Ошибка при обновлении документа: ", e);
                });
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch(FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayInputStream);
        byte[] bytes = byteArrayInputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

}