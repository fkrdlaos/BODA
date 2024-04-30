package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    private Button[] btn_choices; // 4지선다 버튼 배열
    private LinearLayout spellingLayout; // 받아쓰기 유형 레이아웃
    private LinearLayout multipleChoiceLayout; // 4지선다 유형 레이아웃
    private List<String> words;
    private int currentIndex = 0;
    private int correctCount = 0;
    private Random random = new Random();
    private TextToSpeech textToSpeech;
    private Map<String, Integer> wordCountMap = new HashMap<>();
    private boolean isMultipleChoice = false; // 4지선다 문제 여부

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        words = getIntent().getStringArrayListExtra("words");

        // 여기서 레이아웃 참조 가져오기
        spellingLayout = findViewById(R.id.spelling_layout);
        multipleChoiceLayout = findViewById(R.id.multiple_choice_layout);

        tv_word = findViewById(R.id.tv_word); // 여기로 이동
        et_input = findViewById(R.id.et_input);
        btn_check = findViewById(R.id.btn_check);
        btn_next = findViewById(R.id.btn_next);
        btn_home = findViewById(R.id.button4);
        btn_listen = findViewById(R.id.btn_listen); // 버튼 연결
        btn_choices = new Button[]{findViewById(R.id.btn_choice1), findViewById(R.id.btn_choice2),
                findViewById(R.id.btn_choice3), findViewById(R.id.btn_choice4)}; // 4지선다 버튼 초기화

        // TextToSpeech 초기화 및 기본 언어 설정
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
                if (isMultipleChoice) {
                    // 4지선다 문제일 때는 선택된 답을 확인
                    checkMultipleChoice();
                } else {
                    // 받아쓰기 문제일 때는 입력된 단어와 정답을 비교
                    checkSpelling();
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

            // wordCountMap 초기화
            if (wordCountMap == null) {
                wordCountMap = new HashMap<>();
            }

            // 단어 카운트 증가
            Integer count = wordCountMap.get(word);
            if (count == null) {
                // 단어가 맵에 없으면 초기값 0으로 설정
                count = 0;
            }
            wordCountMap.put(word, count + 1);

            // 자주 학습된 단어는 건너뜀 (카운트가 3 이상인 경우)
            if (count >= 3) {
                currentIndex++;
                showNextWord();
                return;
            }

            // 4지선다 문제를 랜덤으로 선택
            isMultipleChoice = random.nextBoolean();

            if (isMultipleChoice) {
                // 4지선다 문제일 때는 랜덤하게 뜻을 선택하여 버튼에 배치
                setMultipleChoice(word);
                // 받아쓰기 유형 레이아웃을 숨김
                spellingLayout.setVisibility(View.GONE);
                // 4지선다 유형 레이아웃을 보임
                multipleChoiceLayout.setVisibility(View.VISIBLE);
            } else {
                // 받아쓰기 문제일 때는 화면에 단어 표시
                tv_word.setText(word);
                et_input.setText("");
                btn_check.setEnabled(true);
                btn_next.setEnabled(false);
                // 받아쓰기 유형 레이아웃을 보임
                spellingLayout.setVisibility(View.VISIBLE);
                // 4지선다 유형 레이아웃을 숨김
                multipleChoiceLayout.setVisibility(View.GONE);
            }

            // 단어 발음 듣기
            if (textToSpeech != null) {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {
            // 모든 단어를 학습한 경우 결과 표시
            showResult();
        }
    }

    private void checkMultipleChoice() {
        // 선택된 버튼의 텍스트와 정답을 비교하여 정답을 맞췄는지 확인
        boolean isCorrect = false;
        for (int i = 0; i < 4; i++) {
            if (btn_choices[i].getText().toString().equals(words.get(currentIndex))) {
                isCorrect = true;
                break;
            }
        }

        // 정답을 맞췄는지에 따라 다음 문제로 넘어가거나 결과를 표시하지 않고 다음 문제로 이동
        if (isCorrect) {
            correctCount++; // 정답 카운트 증가
        }

        // 다음 문제로 넘어가는 작업 추가
        if (currentIndex < words.size() - 1) {
            currentIndex++;
            showNextWord();
        } else {
            // 학습이 끝난 후 결과 표시
            showResult();
        }
    }

    private void setMultipleChoice(String word) {
        // 정답의 위치를 랜덤하게 결정
        int correctPosition = random.nextInt(btn_choices.length);
        // 정답을 버튼에 설정
        btn_choices[correctPosition].setText(word);
        // 정답 버튼에 대한 클릭 리스너 설정
        btn_choices[correctPosition].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼이 클릭되었을 때 실행되는 내용 추가
                checkMultipleChoice();
            }
        });

        // 나머지 버튼에 랜덤하게 다른 단어를 설정
        List<String> otherWords = new ArrayList<>(words);
        otherWords.remove(word);
        Collections.shuffle(otherWords);
        for (int i = 0; i < btn_choices.length; i++) {
            if (i != correctPosition && i < otherWords.size()) {
                btn_choices[i].setText(otherWords.get(i));
                // 오답 버튼에 대한 클릭 리스너 설정
                btn_choices[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 버튼이 클릭되었을 때 실행되는 내용 추가
                        checkMultipleChoice();
                    }
                });
            }
        }
    }

    private void checkSpelling() {
        // 입력된 단어와 정답을 비교하여 결과 표시
        boolean isCorrect = et_input.getText().toString().equalsIgnoreCase(words.get(currentIndex));
        if (isCorrect) {
            correctCount++; // 정답 카운트 증가
        }

        // 다음 문제로 넘어가는 작업 추가
        if (currentIndex < words.size() - 1) {
            currentIndex++;
            showNextWord();
        } else {
            // 학습이 끝난 후 결과 표시
            showResult();
        }
    }


    private void listenWord() {
        if (textToSpeech != null) {
            String word = words.get(currentIndex);
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void showResult() {
        // 학습 완료 후 결과를 표시하는 액티비티로 이동
        Intent intent = new Intent(StudyActivity.this, StudyResultActivity.class);
        intent.putExtra("totalWords", words.size());
        intent.putExtra("correctCount", correctCount);
        startActivity(intent);
        finish();
    }
}
