package com.example.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminLogActivity extends AppCompatActivity {

    private RecyclerView logRecyclerView;
    private LogAdapter adapter;
    private DatabaseReference mDatabase;
    private List<LogEntry> logList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_log);

        logRecyclerView = findViewById(R.id.logRecyclerView);
        logRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogAdapter(logList);
        logRecyclerView.setAdapter(adapter);

        Button backButton = findViewById(R.id.backToMainButton);
        backButton.setOnClickListener(v -> finish());

        mDatabase = FirebaseDatabase.getInstance().getReference("login_logs");
        fetchLogs();
    }

    private void fetchLogs() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                logList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    LogEntry log = postSnapshot.getValue(LogEntry.class);
                    logList.add(log);
                }
                Collections.reverse(logList); // Show latest first
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    public static class LogEntry {
        public String email;
        public String timestamp;

        public LogEntry() {} // Required for Firebase

        public LogEntry(String email, String timestamp) {
            this.email = email;
            this.timestamp = timestamp;
        }
    }

    private static class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
        private List<LogEntry> logs;

        public LogAdapter(List<LogEntry> logs) {
            this.logs = logs;
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_item, parent, false);
            return new LogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            LogEntry log = logs.get(position);
            holder.emailTextView.setText(log.email);
            holder.timeTextView.setText(log.timestamp);
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        static class LogViewHolder extends RecyclerView.ViewHolder {
            TextView emailTextView, timeTextView;

            public LogViewHolder(@NonNull View itemView) {
                super(itemView);
                emailTextView = itemView.findViewById(R.id.logEmail);
                timeTextView = itemView.findViewById(R.id.logTime);
            }
        }
    }
}