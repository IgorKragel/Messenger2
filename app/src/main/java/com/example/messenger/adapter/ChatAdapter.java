package com.example.messenger.adapter;

import static com.example.messenger.adapter.ChatAdapter.SentMessageViewHolder.getImage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.databinding.MessageReceiveContainerBinding;
import com.example.messenger.databinding.MessageSendContainerBinding;
import com.example.messenger.tools.ChatMessage;
import com.example.messenger.tools.Constants;

import java.util.List;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{



    protected final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImage;
    private final String senderId;
    protected final LayoutInflater inflater;
    public static Context context;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId, Context context) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.inflater = LayoutInflater.from(context);
        ChatAdapter.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(MessageSendContainerBinding.inflate(inflater,parent,false));
        } else{
            return new ReceivedMessageViewHolder(MessageReceiveContainerBinding.inflate(inflater, parent, false));        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else{
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }
        else{
            return VIEW_TYPE_RECEIVED;
        }
    }


    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final MessageSendContainerBinding binding;

        SentMessageViewHolder(MessageSendContainerBinding messageSendContainerBinding){
            super(messageSendContainerBinding.getRoot());
            binding = messageSendContainerBinding;
        }

        void setData(ChatMessage chatMessage) {
            if (chatMessage.image != null) {
                // Если есть только картинка
                if (Objects.equals(chatMessage.message, "")) {
                    binding.textMessage.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = binding.imageContainer.getLayoutParams();
                    layoutParams.height = layoutParams.width;
                    binding.imageContainer.setLayoutParams(layoutParams);
                    binding.imageContainer.setImageBitmap(getImage(chatMessage.image));
                    binding.textDataTime.setText(chatMessage.msgTime);
                }
                // Если есть и картинка, и текст
                else {
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.textMessage.setText(chatMessage.message);
                    ViewGroup.LayoutParams layoutParams = binding.imageContainer.getLayoutParams();
                    layoutParams.height = layoutParams.width;
                    binding.imageContainer.setLayoutParams(layoutParams);
                    binding.imageContainer.setImageBitmap(getImage(chatMessage.image));
                    binding.textDataTime.setText(chatMessage.msgTime);
                }
            } else {
                // Если есть только текст
                if (chatMessage.message != null) {
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.textMessage.setText(chatMessage.message);
                    ViewGroup.LayoutParams layoutParams = binding.imageContainer.getLayoutParams();
                    layoutParams.height = 0;
                    binding.imageContainer.setLayoutParams(layoutParams);
                    binding.textDataTime.setText(chatMessage.msgTime);
                }
                // Если нет ни картинки, ни текста
                else {
                    binding.textMessage.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = binding.imageContainer.getLayoutParams();
                    layoutParams.height = 0;
                    binding.imageContainer.setLayoutParams(layoutParams);
                    binding.textDataTime.setText(chatMessage.msgTime);
                }
            }
        }
        static public Bitmap getImage(String img) {
            if (img == null) {
                return null;
            }
            byte[] bytes = Base64.decode(img, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final MessageReceiveContainerBinding binding;

        ReceivedMessageViewHolder(MessageReceiveContainerBinding messageReceiveContainerBinding){
            super(messageReceiveContainerBinding.getRoot());
            binding = messageReceiveContainerBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            if (receiverProfileImage != null) {
      //          binding.textDataTime.setText(chatMessage.msgTime);
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
            if (chatMessage.image != null) {
                // Если есть только картинка
                if (Objects.equals(chatMessage.message, "")) {
                    binding.textMessage.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = binding.imageContainer.getLayoutParams();
                    layoutParams.height = layoutParams.width;
                    binding.imageContainer.setLayoutParams(layoutParams);
                    binding.imageContainer.setImageBitmap(getImage(chatMessage.image));
                    binding.textDataTime.setText(chatMessage.msgTime);
                }
                // Если есть и картинка, и текст
                else {
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.textMessage.setText(chatMessage.message);
                    ViewGroup.LayoutParams layoutParams = binding.imageContainer.getLayoutParams();
                    layoutParams.height = layoutParams.width;
                    binding.imageContainer.setLayoutParams(layoutParams);
                    binding.imageContainer.setImageBitmap(getImage(chatMessage.image));
                    binding.textDataTime.setText(chatMessage.msgTime);
                }
            } else {
                // Если есть только текст
                if (chatMessage.message != null) {
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.textMessage.setText(chatMessage.message);
                    ViewGroup.LayoutParams layoutParams = binding.imageContainer.getLayoutParams();
                    layoutParams.height = 0;
                    binding.imageContainer.setLayoutParams(layoutParams);
                    binding.textDataTime.setText(chatMessage.msgTime);
                }
                // Если нет ни картинки, ни текста
                else {
                    binding.textMessage.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = binding.imageContainer.getLayoutParams();
                    layoutParams.height = 0;
                    binding.imageContainer.setLayoutParams(layoutParams);
                    binding.textDataTime.setText(chatMessage.msgTime);
                }
            }
        }

        private void setLayoutHeight(boolean check){
            ConstraintLayout layout = binding.ConstraintLayout;
            int height;
            if(check){
                height = 163;
                layout.setMaxHeight(height);
            }
            else {
                height = 60;
                layout.setMaxHeight(height);
            }
        }
    }
}
