package org.techtown.boda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class cameraActivity extends AppCompatActivity {

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
                Intent intent = new Intent(cameraActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void processResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String sentence = jsonObject.getString("sentence");
            JSONArray wordsArray = jsonObject.getJSONArray("words");

            // Display the sentence
            displaySentence(sentence);

            // Display the words
            for (int i = 0; i < wordsArray.length(); i++) {
                String word = wordsArray.getString(i);
                displayWord(word);
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

    private void displayWord(String word) {
        // Display a word
        TextView wordTextView = new TextView(this);
        wordTextView.setText("단어: " + word);
        // Set click listener to speak the word
        wordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textToSpeech != null && !textToSpeech.isSpeaking()) {
                    textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });
        wordLayout.addView(wordTextView);
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
                        Toast.makeText(cameraActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(cameraActivity.this, "TTS 초기화에 실패했습니다.", Toast.LENGTH_SHORT).show();
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
}
