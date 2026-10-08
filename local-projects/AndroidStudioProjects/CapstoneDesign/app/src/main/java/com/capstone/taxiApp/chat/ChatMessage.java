package com.capstone.taxiApp.chat;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String senderUserId;
    private String senderName;
    private String messageContent;
    private String messageType;
    private Timestamp sentAt;

    public ChatMessage() {
    }

    public ChatMessage(String senderUserId, String senderName, String messageContent, String messageType, Timestamp sentAt) {
        this.senderUserId = senderUserId;
        this.senderName = senderName;
        this.messageContent = messageContent;
        this.messageType = messageType;
        this.sentAt = sentAt;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getMessageType() {
        return messageType;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }
}
