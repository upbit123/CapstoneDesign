package com.capstone.practice2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private Button[] BtnNum = new Button[10];
    private int[] BtnId = {
            R.id.BtnNum0, R.id.BtnNum1, R.id.BtnNum2,
            R.id.BtnNum3, R.id.BtnNum4, R.id.BtnNum5,
            R.id.BtnNum6, R.id.BtnNum7, R.id.BtnNum8, R.id.BtnNum9
    };
    private int count;
    private String num1, num2;
    private EditText edit1, edit2;
    private Button BtnAdd, BtnMinus, BtnMulti, BtnDivide;
    private TextView TextResult;
    private int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        edit1 = findViewById(R.id.edit1);
        edit2 = findViewById(R.id.edit2);
        BtnAdd = findViewById(R.id.BtnAdd);
        BtnMinus = findViewById(R.id.BtnMinus);
        BtnMulti = findViewById(R.id.BtnMulti);
        BtnDivide = findViewById(R.id.BtnDivide);
        TextResult = findViewById(R.id.TextResult);
        for(int i = 0; i < BtnNum.length; i++){
            BtnNum[i] = (Button)findViewById(BtnId[i]);
        }
        for(int i = 0; i < BtnNum.length; i++){
            final int index;
            index = i;
            BtnNum[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(edit1.isFocused()){
                        num1 = edit1.getText().toString() +
                                BtnNum[index].getText().toString();
                        edit1.setText(num1);
                    } else if (edit2.isFocused()){
                        num2 = edit2.getText().toString() +
                                BtnNum[index].getText().toString();
                        edit2.setText(num2);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "먼저 에디트텍스트를 선택하세요", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        BtnAdd.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                result = Integer.parseInt(num1) + Integer.parseInt(num2);
                TextResult.setText("계산 결과 : " + result);
            }
        });
        BtnMinus.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                result = Integer.parseInt(num1) - Integer.parseInt(num2);
                TextResult.setText("계산 결과 : " + result);
            }
        });
        BtnMulti.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                result = Integer.parseInt(num1) * Integer.parseInt(num2);
                TextResult.setText("계산 결과 : " + result);
            }
        });
        BtnDivide.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                result = Integer.parseInt(num1) / Integer.parseInt(num2);
                TextResult.setText("계산 결과 : " + result);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

}