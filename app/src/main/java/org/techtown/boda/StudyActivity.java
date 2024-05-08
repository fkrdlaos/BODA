package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private Button btn_home;
    private Button btn_listen;
    private Button[] btn_choices;
    private LinearLayout spellingLayout;
    private LinearLayout multipleChoiceLayout;
    private List<String> words;
    private int currentIndex = 0;
    private Random random = new Random();
    private TextToSpeech textToSpeech;
    private Map<String, Boolean> wordAnswerMap = new HashMap<>();
    private boolean isMultipleChoice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        words = new ArrayList<>();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("BODA")
                .child("UserAccount").child(getUserId()).child("collection");

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String word = snapshot.getKey();
                    words.add(word);
                }
                if (!words.isEmpty()) {
                    showNextWord();
                } else {
                    tv_word.setText("학습할 단어가 없습니다. 단어를 추가하세요.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StudyActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });

        spellingLayout = findViewById(R.id.spelling_layout);
        multipleChoiceLayout = findViewById(R.id.multiple_choice_layout);
        tv_word = findViewById(R.id.tv_word);
        et_input = findViewById(R.id.et_input);
        btn_check = findViewById(R.id.btn_check);
        btn_home = findViewById(R.id.button4);
        btn_listen = findViewById(R.id.btn_listen);
        btn_choices = new Button[]{findViewById(R.id.btn_choice1), findViewById(R.id.btn_choice2),
                findViewById(R.id.btn_choice3), findViewById(R.id.btn_choice4)};

        textToSpeech = new TextToSpeech(StudyActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tv_word.setText("이 언어는 지원하지 않습니다.");
                    }
                } else {
                    tv_word.setText("TTS 초기화에 실패했습니다.");
                }
            }
        });

        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMultipleChoice) {
                    checkMultipleChoice();
                } else {
                    checkSpelling();
                }
            }
        });

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudyActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenWord();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private void showNextWord() {
        // 최대 10개의 단어만 표시
        if (currentIndex < 10 && currentIndex < words.size()) {
            String word = getRandomWord();

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
            // 최대 단어 수에 도달하면 학습이 끝난 것으로 간주하고 결과 표시
            showResult();
        }
    }

    private String getRandomWord() {
        // 단어 리스트에서 랜덤하게 단어 선택
        return words.get(currentIndex);
    }


    private void checkMultipleChoice() {
        // 정답 버튼의 텍스트와 실제 정답을 비교하여 정답 여부 확인
        String correctAnswer = words.get(currentIndex);
        Button selectedButton = null;
        for (Button btn : btn_choices) {
            if (btn.getText().toString().equals(correctAnswer)) {
                selectedButton = btn;
                break;
            }
        }

        // 선택된 버튼이 정답 버튼인지 확인
        boolean isCorrect = selectedButton != null && selectedButton.getText().toString().equals(correctAnswer);

        // 단어별 정답 여부 기록
        wordAnswerMap.put(correctAnswer, isCorrect);

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
        String correctAnswer = words.get(currentIndex);
        boolean isCorrect = et_input.getText().toString().equalsIgnoreCase(correctAnswer);
        // 단어별 정답 여부 기록
        wordAnswerMap.put(correctAnswer, isCorrect);

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
        int correctCount = 0;
        for (Boolean isCorrect : wordAnswerMap.values()) {
            if (isCorrect) {
                correctCount++;
            }
        }
        Intent intent = new Intent(StudyActivity.this, StudyResultActivity.class);
        intent.putExtra("totalWords", Math.min(words.size(), 10)); // 총 단어 갯수를 최대 10개로 제한
        intent.putExtra("correctCount", correctCount);
        startActivity(intent);
        finish();
    }

    // Firebase Realtime Database에서 사용자 ID 가져오기
    private String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            // 사용자가 로그인되어 있지 않은 경우 빈 문자열 반환
            return "";
        }
    }
}
