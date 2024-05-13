package org.techtown.boda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class CameraActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private LinearLayout wordLayout;
    private ProgressDialog progressDialog;
    private DatabaseReference mDatabase;
    private DatabaseReference expRef;

    private String sentence;
    private ArrayList<String> words = new ArrayList<>();
    private ArrayList<String> meanings = new ArrayList<>();

    private int newWordsCount = 0; // 새로운 단어의 수를 세는 변수

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Firebase Realtime Database의 "BODA/UserAccount/userId/collection" 경로에 대한 참조 가져오기
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(userId).child("collection");
            expRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(userId);

            // 사용자 노드가 없는 경우에만 노드를 생성하고 컬렉션을 추가
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        // 사용자 노드가 없는 경우, 새로운 노드 및 컬렉션 추가
                        mDatabase.setValue(new HashMap<>())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // 사용자 노드와 컬렉션 생성 성공
                                        Toast.makeText(CameraActivity.this, "사용자 노드 및 컬렉션 생성 완료", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // 생성 실패 처리
                                        Toast.makeText(CameraActivity.this, "사용자 노드 및 컬렉션 생성 실패", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        // 사용자 노드가 없는 경우, exp 및 lv 추가
                        expRef.child("exp").setValue(0);
                        expRef.child("lv").setValue(1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 취소 처리
                    Toast.makeText(CameraActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 로그인하지 않은 사용자 처리
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        wordLayout = findViewById(R.id.wordLayout);

        // Display progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("결과를 가져오는 중...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Get the result from MainActivity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("result")) {
            String result = intent.getStringExtra("result");
            // Process the result
            processResult(result);
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "결과를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Get the image from MainActivity
        if (intent != null && intent.hasExtra("imagePath")) {
            String imagePath = intent.getStringExtra("imagePath");
            // Load image from file path
            Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
            if (imageBitmap != null) {
                // Display the image
                displayImage(imageBitmap);
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "1단계 이미지를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "2단계 이미지를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initialize TextToSpeech
        initializeTextToSpeech();

        progressDialog.dismiss();

        // Initialize moveHomeButton
        Button moveHomeButton = findViewById(R.id.button2);
        moveHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 저장 버튼 클릭 시 호출되는 메서드 등록
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToFirebase();
            }
        });
    }

    private void processResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            sentence = jsonObject.getString("sentence");
            JSONObject wordsObject = jsonObject.getJSONObject("words");

            // Display the sentence
            displaySentence(sentence);

            // Display the words and meanings
            Iterator<String> keys = wordsObject.keys();
            while (keys.hasNext()) {
                String word = keys.next();
                String meaning = wordsObject.getString(word);
                words.add(word);
                meanings.add(meaning);
                displayWordAndMeaning(word, meaning);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "결과를 처리하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        } finally {
            progressDialog.dismiss();
        }
    }


    // saveDataToFirebase() 메서드 내부 수정
    private void saveDataToFirebase() {
        // 현재 시간을 yyyy-MM-dd HH:mm:ss 형식으로 가져오기
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // 데이터 생성
        Map<String, Object> newData = new HashMap<>();
        for (int i = 0; i < words.size(); i++) {
            Map<String, String> wordData = new HashMap<>();
            wordData.put("meanings", meanings.get(i));
            wordData.put("sentence", sentence);
            wordData.put("date_time", currentDateTime);
            newData.put(words.get(i), wordData);
        }

        // 중복 단어를 제외하고 새로운 데이터만 필터링
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 기존 데이터베이스에 데이터가 있는 경우
                    Map<String, Object> existingData = (Map<String, Object>) dataSnapshot.getValue();

                    // 중복 단어를 제외하고 새로운 데이터만 필터링
                    Map<String, Object> filteredData = new HashMap<>();
                    for (String word : newData.keySet()) {
                        if (!existingData.containsKey(word)) {
                            filteredData.put(word, newData.get(word));
                        }
                    }

                    // 새로운 데이터가 있는 경우에만 데이터베이스에 추가
                    if (!filteredData.isEmpty()) {
                        mDatabase.updateChildren(filteredData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // 데이터베이스에 추가 성공한 후 exp 값을 업데이트
                                        updateExp(filteredData.size());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CameraActivity.this, "데이터 추가 실패", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(CameraActivity.this, "새로운 단어가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 기존 데이터베이스에 데이터가 없는 경우
                    mDatabase.setValue(newData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // 데이터베이스에 추가 성공한 후 exp 값을 업데이트
                                    updateExp(newData.size());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CameraActivity.this, "데이터 추가 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CameraActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 새로운 단어의 수에 따라 exp 값을 업데이트하는 메서드
    private void updateExp(int newWordsCount) {
        // 새로운 단어에 대한 exp 증가
        expRef.child("exp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 기존 exp 값이 있는 경우
                    int currentExp = dataSnapshot.getValue(Integer.class);
                    // 새로 추가된 단어의 갯수만큼 exp를 증가시킴
                    int addedExp = newWordsCount * 10;
                    currentExp += addedExp;
                    // exp 값을 업데이트
                    expRef.child("exp").setValue(currentExp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CameraActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void displaySentence(String sentence) {
        // Display the sentence
        TextView sentenceTextView = new TextView(this);
        sentenceTextView.setText("문장: " + sentence);
        wordLayout.addView(sentenceTextView);

        // Add spacing after displaying the sentence
        addSpacing(wordLayout);

        // Set click listener to speak the sentence
        sentenceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textToSpeech != null && !textToSpeech.isSpeaking()) {
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });
    }

    private void displayWordAndMeaning(String word, String meaning) {
        // Display a word and its meaning
        TextView wordTextView = new TextView(this);
        wordTextView.setText("단어: " + word);
        wordLayout.addView(wordTextView);

        // Add spacing after displaying the word
        addSpacing(wordLayout);

        TextView meaningTextView = new TextView(this);
        meaningTextView.setText("뜻: " + meaning);
        wordLayout.addView(meaningTextView);

        // Add spacing after displaying the meaning
        addSpacing(wordLayout);

        // Set click listener to speak the word
        wordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textToSpeech != null && !textToSpeech.isSpeaking()) {
                    textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });

        // Set click listener to speak the meaning
        meaningTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textToSpeech != null && !textToSpeech.isSpeaking()) {
                    textToSpeech.speak(meaning, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });
    }

    private void displayImage(Bitmap imageBitmap) {
        // Display the image
        ImageView imageView = findViewById(R.id.imageViewNext);
        imageView.setImageBitmap(imageBitmap);
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(CameraActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CameraActivity.this, "TTS 초기화에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the TextToSpeech resources
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        // 이전 화면으로 이동
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // 현재 액티비티 종료
    }

    // Method to add spacing
    private void addSpacing(LinearLayout layout) {
        TextView spacingTextView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16); // Add top and bottom margins
        spacingTextView.setLayoutParams(params);
        layout.addView(spacingTextView);
    }
}
