package org.techtown.boda;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
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
    private int correctCount = 0;

    private boolean isQuitDialogShowing = false; // 팝업 창이 열려있는지 여부를 저장하는 변수

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
                    speakFirstWord();
                    showNextWord();
                } else {
                    showNoWordsDialog();  // 단어가 없을 때 팝업 창 표시
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
                showQuitDialog();
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
    protected void onStart() {
        super.onStart();
        textToSpeech = new TextToSpeech(StudyActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tv_word.setText("이 언어는 지원하지 않습니다.");
                    } else {
                        speakFirstWord();
                    }
                } else {
                    tv_word.setText("TTS 초기화에 실패했습니다.");
                }
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

    @Override
    public void onBackPressed() {
        if (isQuitDialogShowing) { // 팝업 창이 열려있을 때
            super.onBackPressed(); // 뒤로 가기 처리를 팝업 창에서 진행하도록 함
            isQuitDialogShowing = false; // 팝업 창 닫음
        } else {
            // 뒤로 가기를 눌렀을 때 Quit 다이얼로그를 표시
            showQuitDialog();
        }
    }

    private void speakFirstWord() {
        if (!words.isEmpty()) {
            String word = getRandomWord();
            if (textToSpeech != null) {
                if (textToSpeech.getLanguage() != null) {
                    textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    Toast.makeText(StudyActivity.this, "단어가 나오고 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
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
            correctCount++;
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
            correctCount++;
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

    private void showQuitDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_quit_study, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        isQuitDialogShowing = true; // 팝업 창이 열려있음을 표시

        Button btnCancel = dialogView.findViewById(R.id.buttonCancel);
        Button btnConfirm = dialogView.findViewById(R.id.buttonConfirm);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isQuitDialogShowing = false; // 팝업 창을 닫음
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudyActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                dialog.dismiss();
                isQuitDialogShowing = false; // 팝업 창을 닫음
            }
        });

        dialog.show();
    }

    // 단어가 없을 때 팝업 창을 표시하는 메서드
    private void showNoWordsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("단어 없음");
        builder.setMessage("학습할 단어가 없습니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(StudyActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }



}
