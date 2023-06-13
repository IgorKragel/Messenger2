package com.example.messenger.tools;

import java.util.Date;

public class ChatMessage {
    public String senderId, receiverId, message, msgTime, image;
    public Date date;

      public ChatMessage(String user_id, String input_msg, Date date) {
        this.receiverId = user_id;
        this.message = input_msg;
        this.msgTime = date.toString();
    }

    public ChatMessage() {
    }

    public String getTime(){
          return msgTime;
    }

    public void setTime(String msgTime){
          this.msgTime = msgTime;
    }
}
