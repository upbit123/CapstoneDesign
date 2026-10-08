package com.capstone.practice3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private void showToastAndFocus(String message, View view){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        view.requestFocus();
    }
    private void resetFields(){
        editId.setText("");
        editName.setText("");
        editPassword.setText("");
        editConfirm.setText("");
        radioGroupGender.clearCheck();
        checkBox.setChecked(false);
    }
    private void validateAndProceed(){
        String id = editId.getText().toString().trim();
        String name = editName.getText().toString().trim();
        String pw = editPassword.getText().toString().trim();
        String confirm = editConfirm.getText().toString().trim();

        if(id.isEmpty()){
            showToastAndFocus("아이디를 입력해주세요.", editId);
            return;
        }
        if(name.isEmpty()){
            showToastAndFocus("이름을 입력해주세요.", editName);
            return;
        }
        if(pw.isEmpty()){
            showToastAndFocus("비밀번호를 입력해주세요.", editPassword);
            return;
        }
        if(confirm.isEmpty()){
            showToastAndFocus("비밀번호 확인을 입력해주세요.", editConfirm);
            return;
        }
        if(!pw.equals(confirm)){
            showToastAndFocus("비밀번호가 일치하지 않습니다.", editConfirm);
            return;
        }
        if(radioGroupGender.getCheckedRadioButtonId() == -1){
            showToastAndFocus("성별을 선택해주세요.", radioGroupGender);
            return;
        }
        if(!checkBox.isChecked()){
            showToastAndFocus("약관에 동의해주세요.", checkBox);
            return;
        }
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        String gender = selectedId == R.id.radio_male ? "남" : "여";
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("name", name);
        intent.putExtra("pw", pw);
        intent.putExtra("gender", gender);
        startActivity(intent);
    }
    private EditText editId, editName, editPassword, editConfirm;
    private RadioGroup radioGroupGender;
    private CheckBox checkBox;
    private Button btnRegister, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editId = findViewById(R.id.editId);
        editName = findViewById(R.id.editName);
        editPassword = findViewById(R.id.editPassword);
        editConfirm = findViewById(R.id.editConfirm);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        checkBox = findViewById(R.id.checkBox);
        btnRegister = findViewById(R.id.btnRegister);
        btnReset = findViewById(R.id.btnReset);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndProceed();
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

