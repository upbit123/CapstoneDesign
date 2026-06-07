package com.capstone.taxiApp.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.capstone.taxiApp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "room_id";
    public static final String EXTRA_ROOM_TITLE = "room_title";
    public static final String EXTRA_SENDER_USER_ID = "sender_user_id";
    public static final String EXTRA_SENDER_NAME = "sender_name";

    private String roomId;
    private String senderUserId;
    private String senderName;

    private TextView roomTitleView;
    private TextView messageView;
    private EditText input;
    private Button sendBtn;
    private Button finishRideButton;

    private ChatRepository repo;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        senderUserId = getIntent().getStringExtra(EXTRA_SENDER_USER_ID);
        senderName = getIntent().getStringExtra(EXTRA_SENDER_NAME);
        String roomTitle = getIntent().getStringExtra(EXTRA_ROOM_TITLE);

        roomTitleView = findViewById(R.id.roomTitleTextView);
        messageView = findViewById(R.id.messageTextView);
        input = findViewById(R.id.messageEditText);
        sendBtn = findViewById(R.id.sendButton);
        finishRideButton = findViewById(R.id.finishRideButton);

        if (roomId == null || roomId.isBlank()) {
            roomId = "testRoom1";
        }
        if (senderUserId == null || senderUserId.isBlank()) {
            senderUserId = "user1";
        }
        if (senderName == null || senderName.isBlank()) {
            senderName = "테스트 사용자";
        }
        roomTitleView.setText(roomTitle == null || roomTitle.isBlank() ? "합승 채팅방" : roomTitle);

        repo = new ChatRepository();

        sendBtn.setOnClickListener(v -> {
            repo.sendMessage(roomId, senderUserId, senderName, input.getText().toString());
            input.setText("");
        });

        listener = repo.listenMessages(roomId, (snap, e) -> {
            if (snap == null) return;

            StringBuilder sb = new StringBuilder();

            for (DocumentSnapshot doc : snap.getDocuments()) {
                sb.append(doc.getString("senderName"))
                  .append(": ")
                  .append(doc.getString("messageContent"))
                  .append("\n");
            }

            messageView.setText(sb.toString());
        });

        finishRideButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, com.capstone.taxiApp.HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.remove();
        }
    }
}
