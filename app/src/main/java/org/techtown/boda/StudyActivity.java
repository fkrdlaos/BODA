package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudyActivity extends AppCompatActivity {

    private TextView tv_word;
    private Button btn_next;
    private Button btn_home; // 버튼 추가
    private List<String> words;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        words = getIntent().getStringArrayListExtra("words");

        tv_word = findViewById(R.id.tv_word);
        btn_next = findViewById(R.id.btn_next);
        btn_home = findViewById(R.id.button4); // 버튼 초기화

        showNextWord();

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentIndex < words.size() - 1) {
                    currentIndex++;
                    showNextWord();
                } else {
                    // If all words have been studied, you can handle it here
                    // For example, display a message or return to the main activity
                    // This is just a sample implementation
                    finish();
                }
            }
        });

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 홈 화면으로 이동
                Intent intent = new Intent(StudyActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showNextWord() {
        String word = words.get(currentIndex);
        tv_word.setText(word);
    }
}

