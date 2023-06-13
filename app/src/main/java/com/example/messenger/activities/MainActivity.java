package com.example.messenger.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.messenger.R;
import com.example.messenger.UserListener;
import com.example.messenger.adapter.UserAdapter;
import com.example.messenger.databinding.ActivityMainBinding;
import com.example.messenger.tools.Constants;
import com.example.messenger.tools.PreferenceManager;
import com.example.messenger.tools.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements UserListener {

    private PreferenceManager preferenceManager;
    private ActivityMainBinding binding;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getUsers();
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding.logoutButton.setOnClickListener(v -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(intent);
                    finish();
                }
        );
        binding.settingButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void getUsers() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.chat_list);
        FirebaseFirestore databse = FirebaseFirestore.getInstance();
        databse.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        int n = 0;
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                             if (Objects.equals(currentUserId, queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                         //   user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            recyclerView.setAdapter(userAdapter);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(MainActivity.this, "Нет пользователей", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void OnUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}