package com.capstone.practice3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        String id = getIntent().getStringExtra("id");
        String name = getIntent().getStringExtra("name");
        String pw = getIntent().getStringExtra("pw");
        String gender = getIntent().getStringExtra("gender");
        TextView textViewInfo = findViewById(R.id.textViewResult);
        textViewInfo.setText(
                "아이디 : " + id +
                        "\n이름 : " + name +
                        "\n비밀번호 : " + pw +
                        "\n성별 : " + gender
        );
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}