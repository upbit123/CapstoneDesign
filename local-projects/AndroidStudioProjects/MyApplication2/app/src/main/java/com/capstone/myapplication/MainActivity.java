package com.capstone.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private EditText editTextId;
    private EditText editTextName;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private RadioGroup radioGroupGender;
    private CheckBox checkBoxAgree;
    private Button buttonRegister;
    private Button buttonReset;

    private void resetField(){
        editTextId.setText("");
        editTextName.setText("");
        editTextPassword.setText("");
        editTextConfirmPassword.setText("");
        radioGroupGender.clearCheck();
        checkBoxAgree.setChecked(false);
    }
    private void validateAndProceed(){
        String id = editTextId.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String pw = editTextPassword.getText().toString().trim();
        String confirm = editTextConfirmPassword.getText().toString().trim();
        if(id.isEmpty()){
            showToastAndFocus("아이디를 입력해주세요", editTextId);
            return;
        }
        if(name.isEmpty()){
            showToastAndFocus("이름을 입력해주세요", editTextName);
            return;
        }
        if(pw.isEmpty()){
            showToastAndFocus("비밀번호를 입력해주세요", editTextPassword);
            return;
        }
        if(confirm.isEmpty()){
            showToastAndFocus("비밀번호를 확인해주세요", editTextConfirmPassword);
            return;
        }
        if(radioGroupGender.getCheckedRadioButtonId() == -1){
            showToastAndFocus("성별을 선택해주세요", radioGroupGender);
            return;
        }
        if(!checkBoxAgree.isChecked()){
            showToastAndFocus("가입에 동의해주세요", checkBoxAgree);
            return;
        }
        String gender;
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        if(selectedId == R.id.radio_male){
            Log.d("test", "남자 선택");
            gender = "남";
        }else {
            Log.d("Test", "여자 선택");
            gender = "여";
        }
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("name", name);
        intent.putExtra("pw", pw);
        intent.putExtra("gender", gender);
        startActivity(intent);
    }
    private void showToastAndFocus(String message, View view){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        view.requestFocus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextId = findViewById(R.id.edit_text_id);
        editTextName = findViewById(R.id.edit_text_name);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_check);
        radioGroupGender = findViewById(R.id.radio_group_gender);
        checkBoxAgree = findViewById(R.id.check_box_agree);
        buttonRegister = findViewById(R.id.button_register);
        buttonReset = findViewById(R.id.button_reset);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndProceed();
            }
        });
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetField();
            }
        });
    }
}
