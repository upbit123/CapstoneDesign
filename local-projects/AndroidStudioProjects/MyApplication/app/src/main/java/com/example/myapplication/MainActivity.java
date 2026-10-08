package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "버튼 눌림!", Toast.LENGTH_LONG).show();
        });
    }
}