package com.example.messenger.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;

import com.example.messenger.adapter.ChatAdapter;
import com.example.messenger.databinding.ActivityChatBinding;
import com.example.messenger.tools.ChatMessage;
import com.example.messenger.tools.Constants;
import com.example.messenger.tools.PreferenceManager;
import com.example.messenger.tools.User;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity{

    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String encodedImage;
    private ActivityChatBinding binding;
    private User receiverUser;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        binding.imageBack.setOnClickListener(v -> startActivity(intent));
        binding.layoutSend.setOnClickListener(v -> sendMsg());
        binding.imageSend.setOnClickListener(v -> {
            Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent1);
        });
        binding.cancelButton.setOnClickListener(v ->{
            encodedImage = null;
            binding.setImage.setVisibility(View.INVISIBLE);
        });
        loadData();
        chatAdapter = new ChatAdapter(chatMessages, getImage(receiverUser.image), preferenceManager.getString(Constants.KEY_USER_ID), ChatActivity.this);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        listenMessages();
    }


 /*   @SuppressLint("NotifyDataSetChanged")
    private void loadMessages(){
        List<ChatMessage> messages = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_MSG)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.message = queryDocumentSnapshot.getString(Constants.KEY_MESSAGE);
                        chatMessage.msgTime = getDate(queryDocumentSnapshot.getDate(Constants.KEY_TIME));
                        chatMessage.senderId = queryDocumentSnapshot.getString(Constants.KEY_SENDER_ID);
                        chatMessage.date = queryDocumentSnapshot.getDate(Constants.KEY_TIME);
                        chatMessage.receiverId = queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_ID);
                        messages.add(chatMessage);
                        chatAdapter.notifyDataSetChanged();
                        chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
                    }
                });
        binding.chatRecyclerView.setVisibility(View.VISIBLE);
    }*/


    private void listenMessages() {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        database.collection(Constants.KEY_COLLECTION_MSG)
                .whereIn(Constants.KEY_SENDER_ID, Arrays.asList(userId, receiverUser.id))
                .whereIn(Constants.KEY_RECEIVER_ID, Arrays.asList(userId, receiverUser.id))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            LinkedList<ChatMessage> newMessages = new LinkedList<>();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.msgTime = getDate(documentChange.getDocument().getDate(Constants.KEY_TIME));
                    chatMessage.date = documentChange.getDocument().getDate(Constants.KEY_TIME);
                    chatMessage.image = documentChange.getDocument().getString(Constants.KEY_IMAGE_SEND);
                    newMessages.add(chatMessage);
                }
            }
            chatMessages.addAll(newMessages);
            chatMessages.sort(Comparator.comparing(obj -> obj.date));
            int newMessagesCount = newMessages.size();
            int chatMessagesSize = chatMessages.size();
            if (newMessagesCount > 0) {
                if (chatAdapter == null) {
                    binding.chatRecyclerView.setAdapter(chatAdapter);
                } else {
                    chatAdapter.notifyItemRangeInserted(chatMessagesSize - newMessagesCount, newMessagesCount);
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessagesSize - 1);
                }
            } else if (chatMessagesSize == 0) {
                binding.chatRecyclerView.setAdapter(chatAdapter);
                chatAdapter.notifyDataSetChanged();
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
    };

    private void sendMsg() {
        HashMap<String, Object> message = new HashMap<>();
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        String inputMsg = binding.inputMessage.getText().toString().trim(); // создаем переменную и очищаем от лишних пробелов
        message.put(Constants.KEY_SENDER_ID, userId);
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, inputMsg);
        message.put(Constants.KEY_TIME, new Date());
        if(encodedImage != null){
            message.put(Constants.KEY_IMAGE_SEND, encodedImage);
            binding.setImage.setVisibility(View.INVISIBLE);
        }
        database.collection(Constants.KEY_COLLECTION_MSG).add(message);
        binding.inputMessage.setText(""); // очищаем поле ввода
        encodedImage = null;
    }

    private Bitmap getImage(String img) {
        if (img == null) {
            return null;
        }
        byte[] bytes = Base64.decode(img, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void loadData() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        if (receiverUser != null) { // добавляем проверку на null
            binding.textName.setText(receiverUser.name);
        }
    }

    private String getDate(Date date) {
        return DATE_FORMAT.format(date);
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
                            binding.setImage.setVisibility(View.VISIBLE);
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