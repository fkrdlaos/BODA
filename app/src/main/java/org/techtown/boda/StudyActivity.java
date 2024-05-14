package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

    private ProgressBar progressBar;
    private TextView textViewCon;

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
    private List<Integer> randomIndexes;
    private int maxProgress = 10;
    private int correctCount = 0; // 새로 추가된 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        progressBar = findViewById(R.id.progress_bar);
        textViewCon = findViewById(R.id.TextView_con);
        progressBar.setMax(maxProgress);

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
                    setRandomIndex();
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
                checkSpelling();
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
        if (currentIndex < maxProgress && currentIndex < words.size()) {
            String word = getRandomWord();

            isMultipleChoice = random.nextBoolean();

            if (isMultipleChoice) {
                setMultipleChoice(word);
                spellingLayout.setVisibility(View.GONE);
                multipleChoiceLayout.setVisibility(View.VISIBLE);
            } else {
                tv_word.setText(word);
                et_input.setText("");
                btn_check.setEnabled(true);
                spellingLayout.setVisibility(View.VISIBLE);
                multipleChoiceLayout.setVisibility(View.GONE);
            }

            if (textToSpeech != null) {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {
            showResult();
        }
        updateProgress();
    }

    private void setRandomIndex() {
        int size = words.size();
        int count = 10;

        if (size < count) {
            count = size;
        }

        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes);

        randomIndexes = indexes.subList(0, count);
    }

    private String getRandomWord() {
        return words.get(randomIndexes.get(currentIndex));
    }

    private void updateProgress() {
        int totalWords = Math.min(words.size(), maxProgress);
        String progressText = (currentIndex + 1) + "/" + totalWords;
        textViewCon.setText(progressText);

        int progress = currentIndex + 1;
        progressBar.setProgress(progress);
    }

    private void setMultipleChoice(String word) {
        int correctPosition = random.nextInt(btn_choices.length);
        btn_choices[correctPosition].setText(word);
        btn_choices[correctPosition].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkMultipleChoice(true);
            }
        });

        List<String> otherWords = new ArrayList<>(words);
        otherWords.remove(word);
        Collections.shuffle(otherWords);
        for (int i = 0; i < btn_choices.length; i++) {
            if (i != correctPosition && i < otherWords.size()) {
                btn_choices[i].setText(otherWords.get(i));
                btn_choices[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkMultipleChoice(false);
                    }
                });
            }
        }
    }

    private void checkMultipleChoice(boolean isCorrect) {
        String correctAnswer = words.get(randomIndexes.get(currentIndex));
        wordAnswerMap.put(correctAnswer, isCorrect);

        if (isCorrect) {
            correctCount++; // 맞춘 문제 수 증가

        }

        if (currentIndex < words.size() - 1) {
            currentIndex++;
            showNextWord();
        } else {
            showResult();
        }
    }

    private void checkSpelling() {
        if (et_input.getText().toString().equalsIgnoreCase(words.get(randomIndexes.get(currentIndex)))) {
            correctCount++; // 맞춘 문제 수 증가

        }
        wordAnswerMap.put(words.get(randomIndexes.get(currentIndex)), et_input.getText().toString().equalsIgnoreCase(words.get(randomIndexes.get(currentIndex))));
        if (currentIndex < words.size() - 1) {
            currentIndex++;
            showNextWord();
        } else {
            showResult();
        }
    }



    private void listenWord() {
        if (textToSpeech != null) {
            String word = words.get(randomIndexes.get(currentIndex));
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void showResult() {
        Intent intent = new Intent(StudyActivity.this, StudyResultActivity.class);
        intent.putExtra("totalWords", Math.min(words.size(), 10));
        intent.putExtra("correctCount", correctCount);
        startActivity(intent);
        finish();
    }

    private String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return "";
        }
    }
}
