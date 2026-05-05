package com.capstone.taxiApp.chat;

import com.google.firebase.firestore.*;
import java.util.HashMap;
import java.util.Map;

public class ChatService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void sendMessage(String chatRoomId, String senderUserId, String senderName, String messageContent) {

        Map<String, Object> message = new HashMap<>();
        message.put("senderUserId", senderUserId);
        message.put("senderName", senderName);
        message.put("messageContent", messageContent);
        message.put("messageType", "TEXT");
        message.put("sentAt", FieldValue.serverTimestamp());

        db.collection("chatRooms")
                .document(chatRoomId)
                .collection("messages")
                .add(message);
    }

    public ListenerRegistration listenMessages(
            String chatRoomId,
            EventListener<QuerySnapshot> listener
    ) {
        return db.collection("chatRooms")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("sentAt", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }
}
