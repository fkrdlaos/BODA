package org.techtown.boda;

import android.content.Intent;
import android.graphics.Bitmap;
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

import org.techtown.boda.MainActivity;
import org.techtown.boda.WordData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class cameraActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private LinearLayout wordLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ImageView imageView = findViewById(R.id.imageViewNext);
        wordLayout = findViewById(R.id.wordLayout);

        Bitmap imageBitmap = getIntent().getParcelableExtra("imageBitmap");
        if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap);
        }

        ArrayList<String> words = getIntent().getStringArrayListExtra("words");
        ArrayList<String> meanings = getIntent().getStringArrayListExtra("meanings");
        ArrayList<String> examples = getIntent().getStringArrayListExtra("examples");

        if (words != null && meanings != null && examples != null && words.size() == meanings.size() && words.size() == examples.size()) {
            // Combine word, meaning, and example data into WordData objects
            ArrayList<WordData> wordDataList = new ArrayList<>();
            for (int i = 0; i < words.size(); i++) {
                wordDataList.add(new WordData(words.get(i), meanings.get(i), examples.get(i)));
            }

            // Sort WordData objects alphabetically based on word
            Collections.sort(wordDataList, new Comparator<WordData>() {
                @Override
                public int compare(WordData o1, WordData o2) {
                    return o1.getWord().compareToIgnoreCase(o2.getWord());
                }
            });

            // Display sorted word, meaning, and example data
            for (final WordData wordData : wordDataList) {
                TextView wordTextView = new TextView(this);
                wordTextView.setText("단어: " + wordData.getWord());
                wordTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textToSpeech.speak(wordData.getWord(), TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                });
                wordLayout.addView(wordTextView);

                TextView meaningTextView = new TextView(this);
                meaningTextView.setText("의미: " + wordData.getMeaning());
                meaningTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String ssml = "<speak><prosody rate=\"medium\">" + wordData.getMeaning() + "</prosody></speak>";
                        textToSpeech.speak(ssml, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                });
                wordLayout.addView(meaningTextView);

                TextView exampleTextView = new TextView(this);
                exampleTextView.setText("예문: " + wordData.getExample());
                exampleTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textToSpeech.speak(wordData.getExample(), TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                });
                wordLayout.addView(exampleTextView);

                View spaceView = new View(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20);
                wordLayout.addView(spaceView, layoutParams);
            }
        }

        textToSpeech = new TextToSpeech(cameraActivity.this, new TextToSpeech.OnInitListener() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
