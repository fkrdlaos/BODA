package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get WordData object from Intent
        WordData wordData = (WordData) getIntent().getSerializableExtra("wordData");

        // Extract data from WordData object
        String word = wordData.getWord();
        String meaning = wordData.getMeaning();
        String example = wordData.getExample();

        TextView wordTextView = findViewById(R.id.wordTextView);
        TextView meaningTextView = findViewById(R.id.meaningTextView);
        TextView exampleTextView = findViewById(R.id.exampleTextView);

        wordTextView.setText("단어: " + word);
        meaningTextView.setText("의미: " + meaning);
        exampleTextView.setText("예문: " + example);

        Button ttsButton = findViewById(R.id.ttsButton);
        ttsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TTS 발음 듣기
                String text = word + ". " + meaning + ". " + example;
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이전 페이지로 이동
                finish();
            }
        });

        textToSpeech = new TextToSpeech(DetailActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(DetailActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailActivity.this, "TTS 초기화에 실패했습니다.", Toast.LENGTH_SHORT).show();
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
}