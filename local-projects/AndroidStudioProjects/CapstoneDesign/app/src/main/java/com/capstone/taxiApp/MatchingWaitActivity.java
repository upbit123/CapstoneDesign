package com.capstone.taxiApp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.capstone.taxiApp.chat.ChatActivity;

public class MatchingWaitActivity extends AppCompatActivity {

    public static final String EXTRA_START_ZONE_NAME = "start_zone_name";
    public static final String EXTRA_TARGET_ZONE_NAME = "target_zone_name";
    public static final String EXTRA_DIRECTION_TYPE = "direction_type";
    public static final String EXTRA_REQUEST_ID = "request_id";
    public static final String EXTRA_REQUEST_STATUS = "request_status";
    public static final String EXTRA_MATCHED = "matched";
    public static final String EXTRA_CHAT_ROOM_ID = "chat_room_id";
    public static final String EXTRA_ROOM_TITLE = "room_title";

    private static final long POLL_INTERVAL_MILLIS = 3000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MatchStatusRepository matchStatusRepository = new MatchStatusRepository();

    private long requestId;
    private long chatRoomId;
    private boolean matched;
    private String roomTitle;
    private String startZoneName;
    private String targetZoneName;
    private String directionType;
    private TextView summaryTextView;
    private Button enterChatButton;
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            pollMatchStatus();
            handler.postDelayed(this, POLL_INTERVAL_MILLIS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_wait);

        startZoneName = getIntent().getStringExtra(EXTRA_START_ZONE_NAME);
        targetZoneName = getIntent().getStringExtra(EXTRA_TARGET_ZONE_NAME);
        directionType = getIntent().getStringExtra(EXTRA_DIRECTION_TYPE);
        requestId = getIntent().getLongExtra(EXTRA_REQUEST_ID, -1L);
        String requestStatus = getIntent().getStringExtra(EXTRA_REQUEST_STATUS);
        matched = getIntent().getBooleanExtra(EXTRA_MATCHED, false);
        chatRoomId = getIntent().getLongExtra(EXTRA_CHAT_ROOM_ID, -1L);
        roomTitle = getIntent().getStringExtra(EXTRA_ROOM_TITLE);

        summaryTextView = findViewById(R.id.matchingSummaryTextView);
        enterChatButton = findViewById(R.id.enterChatButton);
        Button cancelButton = findViewById(R.id.cancelMatchingButton);

        renderSummary(requestStatus);

        enterChatButton.setOnClickListener(v -> {
            openChatRoom();
        });

        enterChatButton.setEnabled(matched);

        cancelButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!matched && requestId > 0L) {
            handler.post(pollRunnable);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(pollRunnable);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "미정" : value;
    }

    private void renderSummary(String requestStatus) {
        String resolvedRoomTitle = roomTitle == null || roomTitle.isBlank()
                ? safeText(startZoneName) + " -> " + safeText(targetZoneName)
                : roomTitle;

        summaryTextView.setText(
                "출발지: " + safeText(startZoneName) +
                        "\n목적지: " + safeText(targetZoneName) +
                        "\n방향: " + safeText(directionType) +
                        "\n요청 ID: " + (requestId > 0 ? requestId : "미생성") +
                        "\n상태: " + safeText(requestStatus) +
                        "\n채팅방 ID: " + (chatRoomId > 0 ? chatRoomId : "대기 중") +
                        "\n방 제목: " + resolvedRoomTitle +
                        "\n\n" + (matched
                        ? "4인 매칭이 완료되었습니다. 바로 채팅방으로 입장할 수 있습니다."
                        : "매칭 대기 중입니다. 4인이 모이면 자동으로 채팅방을 열어 줍니다.")
        );
    }

    private void pollMatchStatus() {
        FirebaseTokenHelper.fetchIdToken(new FirebaseTokenHelper.TokenCallback() {
            @Override
            public void onSuccess(@androidx.annotation.NonNull String idToken) {
                new Thread(() -> {
                    try {
                        MatchStatusRepository.MatchStatusResult result =
                                matchStatusRepository.fetchMatchStatus(idToken, requestId);
                        runOnUiThread(() -> {
                            matched = result.matched();
                            chatRoomId = result.chatRoomId();
                            roomTitle = result.roomTitle();
                            enterChatButton.setEnabled(matched);
                            renderSummary(result.requestStatus());
                            if (matched && chatRoomId > 0L) {
                                handler.removeCallbacks(pollRunnable);
                                openChatRoom();
                            }
                        });
                    } catch (Exception ignored) {
                    }
                }).start();
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull String message) {
            }
        });
    }

    private void openChatRoom() {
        SessionManager sessionManager = new SessionManager(this);
        Intent intent = new Intent(this, ChatActivity.class);
        String resolvedRoomTitle = roomTitle == null || roomTitle.isBlank()
                ? safeText(startZoneName) + " -> " + safeText(targetZoneName)
                : roomTitle;
        intent.putExtra(ChatActivity.EXTRA_ROOM_ID, "chat-room-" + chatRoomId);
        intent.putExtra(ChatActivity.EXTRA_ROOM_TITLE, resolvedRoomTitle);
        intent.putExtra(ChatActivity.EXTRA_SENDER_USER_ID, String.valueOf(sessionManager.getUserId()));
        intent.putExtra(ChatActivity.EXTRA_SENDER_NAME, sessionManager.getDisplayName());
        startActivity(intent);
    }
}
