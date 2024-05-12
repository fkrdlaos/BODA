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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private LinearLayout wordLayout;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

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

        // 이미지뷰의 터치 이벤트 처리
        ImageView imageView = findViewById(R.id.imageViewNext);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이미지 회전
                rotateImage();
            }
        });
    }

    private void processResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String sentence = jsonObject.getString("sentence");
            JSONObject wordsObject = jsonObject.getJSONObject("words");

            // Display the sentence
            displaySentence(sentence);

            // Display the words and meanings
            Iterator<String> keys = wordsObject.keys();
            while (keys.hasNext()) {
                String word = keys.next();
                String meaning = wordsObject.getString(word);
                displayWordAndMeaning(word, meaning);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "결과를 처리하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        } finally {
            progressDialog.dismiss();
        }
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

    // 이미지 회전 메서드
    private void rotateImage() {
        ImageView imageView = findViewById(R.id.imageViewNext);
        if (imageView.getDrawable() == null) {
            // 이미지가 없을 때는 회전하지 않음
            Toast.makeText(this, "이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 현재 이미지 회전 각도 확인
        float currentRotation = imageView.getRotation();
        // 회전 각도 90도씩 증가시키기
        float newRotation = currentRotation + 90f;
        // 회전 애니메이션
        imageView.animate().rotation(newRotation).setDuration(200).start();
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
