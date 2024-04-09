package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StudyActivity extends AppCompatActivity {

    private TextView tv_word;
    private EditText et_input;
    private Button btn_check;
    private Button btn_next;
    private Button btn_home;
    private List<String> words;
    private int currentIndex = 0;
    private int correctCount = 0;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        words = getIntent().getStringArrayListExtra("words");

        tv_word = findViewById(R.id.tv_word);
        et_input = findViewById(R.id.et_input);
        btn_check = findViewById(R.id.btn_check);
        btn_next = findViewById(R.id.btn_next);
        btn_home = findViewById(R.id.button4);

        // 학습할 단어 수를 최대 5개로 제한
        int studyCount = Math.min(words.size(), 5);

        // 학습할 단어를 무작위로 선택
        Collections.shuffle(words);

        // 선택된 단어 목록을 5개로 제한
        words = words.subList(0, studyCount);

        showNextWord();

        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = et_input.getText().toString().trim();
                String displayedWord = tv_word.getText().toString();
                if (userInput.equalsIgnoreCase(displayedWord)) {
                    tv_word.setText("정답!");
                    correctCount++; // 정답일 경우 정답 개수 증가
                } else {
                    tv_word.setText("오답!");
                }
                btn_check.setEnabled(false);
                btn_next.setEnabled(true);
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentIndex < words.size() - 1) {
                    currentIndex++;
                    showNextWord();
                } else {
                    // 학습이 끝난 후 결과 표시
                    showResult();
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
        et_input.setText("");
        btn_check.setEnabled(true);
        btn_next.setEnabled(false);
    }

    private void showResult() {
        // 총 단어 수와 정답 개수를 보여줌
        String resultMessage = "총 " + words.size() + "개의 단어 중 " + correctCount + "개를 맞췄습니다.";
        tv_word.setText(resultMessage);
        et_input.setVisibility(View.GONE);
        btn_check.setVisibility(View.GONE);
        btn_next.setVisibility(View.GONE);
        btn_home.setVisibility(View.VISIBLE);
    }
}

/* 4/9 16:45
 1. StudyActivity.java 수정
  -단어 랜덤 순서로 표시하기
  -특정 단어에 해당하는 스펠링(대/소문자 구분 안함)입력 후 버튼을 클릭하면 "정답"인지 "오답"인지 체크
  -정답을 확인하면 다음으로 넘어가는 버튼 활성화
  -모든 문제가 끝나면 몇 문제를 맞추었는지 확인
  -문제 수를 다섯 문제로 제한

*/