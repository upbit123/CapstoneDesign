package com.capstone.taxiApp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.capstone.taxiApp.chat.ChatActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class TaxiFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "match_notifications";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        PushTokenRegistrar.syncIfPossible(getApplicationContext());
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        String title = message.getData().getOrDefault("title", "매칭 알림");
        String body = message.getData().getOrDefault("body", "새 알림이 도착했습니다.");
        String roomTitle = message.getData().getOrDefault("roomTitle", "합승 채팅방");
        String chatRoomId = message.getData().getOrDefault("chatRoomId", "");

        createNotificationChannel();

        SessionManager sessionManager = new SessionManager(this);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_ROOM_ID, "chat-room-" + chatRoomId);
        intent.putExtra(ChatActivity.EXTRA_ROOM_TITLE, roomTitle);
        intent.putExtra(ChatActivity.EXTRA_SENDER_USER_ID, String.valueOf(sessionManager.getUserId()));
        intent.putExtra(ChatActivity.EXTRA_SENDER_NAME, sessionManager.getDisplayName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(this)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "매칭 알림",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("4인 매칭 완료와 채팅방 입장 알림");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
