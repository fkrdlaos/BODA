package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class StudyActivity extends AppCompatActivity {

    private TextView tv_word;
    private EditText et_input;
    private Button btn_check;
    private Button btn_next;
    private Button btn_home;
    private Button btn_listen; // 추가된 버튼
    private List<String> words;
    private int currentIndex = 0;
    private int correctCount = 0;
    private Random random = new Random();
    private TextToSpeech textToSpeech;
    private Map<String, Integer> wordCountMap = new HashMap<>();

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
        btn_listen = findViewById(R.id.btn_listen); // 버튼 연결

        // 선택된 단어를 랜덤으로 섞음
        Collections.shuffle(words);

        // 단어 카운트 초기화
        for (String word : words) {
            wordCountMap.put(word, 0);
        }

        // TextToSpeech 초기화
        textToSpeech = new TextToSpeech(StudyActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // 언어 지원되지 않을 경우 메시지 표시
                        tv_word.setText("이 언어는 지원하지 않습니다.");
                    } else {
                        // TextToSpeech가 초기화된 후에 showNextWord 호출
                        showNextWord();
                    }
                } else {
                    // TTS 초기화 실패 시 메시지 표시
                    tv_word.setText("TTS 초기화에 실패했습니다.");
                }
            }
        });

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

        btn_listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 발음 듣기
                listenWord();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TextToSpeech 종료
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private void showNextWord() {
        if (currentIndex < words.size()) {
            String word = words.get(currentIndex);
            // 단어 카운트 증가
            int count = wordCountMap.get(word);
            wordCountMap.put(word, count + 1);

            // 자주 학습된 단어는 건너뜀 (카운트가 3 이상인 경우)
            if (count >= 3) {
                currentIndex++;
                showNextWord();
                return;
            }

            // 단어 발음 듣기
            if (textToSpeech != null) {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            }

            // 화면에 단어 표시
            tv_word.setText(word);
            et_input.setText("");
            btn_check.setEnabled(true);
            btn_next.setEnabled(false);
        } else {
            // 모든 단어를 학습한 경우 결과 표시
            showResult();
        }
    }

    private void showResult() {
        // StudyResultActivity로 이동
        Intent intent = new Intent(StudyActivity.this, StudyResultActivity.class);
        intent.putExtra("totalWords", words.size());
        intent.putExtra("correctCount", correctCount);
        startActivity(intent);
    }

    private void listenWord() {
        if (currentIndex < words.size()) {
            String word = words.get(currentIndex);
            // 단어 발음 듣기
            if (textToSpeech != null) {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }
}

