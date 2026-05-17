package com.capstone.taxiApp.chat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.capstone.taxiApp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class ChatActivity extends AppCompatActivity {

    private final String roomId = "testRoom1";
    private final String senderUserId = "user1";
    private final String senderName = "테스트 사용자";

    private TextView messageView;
    private EditText input;
    private Button sendBtn;

    private ChatRepository repo;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageView = findViewById(R.id.messageTextView);
        input = findViewById(R.id.messageEditText);
        sendBtn = findViewById(R.id.sendButton);

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.remove();
        }
    }
}
