package org.techtown.boda;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends BaseActivity {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1001;
    private TextToSpeech textToSpeech;
    private ImageView photoImageView;
    private TextView wordTextView;
    private TextView meaningTextView;
    private TextView exampleTextView;
    private TextView dateTextView;
    private static final int REQUEST_CAMERA_PERMISSION = 1001; // 카메라 권한 요청 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        WordData wordData = (WordData) getIntent().getSerializableExtra("wordData");

        if (wordData != null && wordData.getDateTime() != null) {
            String word = wordData.getWord();
            String meaning = wordData.getMeaning();
            String example = wordData.getExample();
            String dateTime = wordData.getDateTime();
            int imageId = wordData.getImageResId();

            wordTextView = findViewById(R.id.wordTextView);
            meaningTextView = findViewById(R.id.meaningTextView);
            exampleTextView = findViewById(R.id.exampleTextView);
            dateTextView = findViewById(R.id.dateTextView);

            wordTextView.setText(word);
            meaningTextView.setText(meaning);
            exampleTextView.setText(example);

            // "date_time" 값을 올바른 형식으로 변환하여 TextView에 표시
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date date = sdf.parse(dateTime);
                dateTextView.setText(sdf.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("Date_Time_Debug", "Error parsing date: " + e.getMessage());
                // ParseException 발생 시, TextView에 특정 메시지를 표시하거나 기본값을 사용할 수 있습니다.
                dateTextView.setText("날짜 정보를 처리할 수 없습니다.");
            }

            // TTS 버튼 클릭 이벤트 처리
            Button ttsButton = findViewById(R.id.ttsButton);
            ttsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TTS 발음 듣기
                    String text = word;
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            });

            // 이미지뷰 초기화
            photoImageView = findViewById(R.id.photoImageView);
            Button addPhotoButton = findViewById(R.id.addPhotoButton);
            photoImageView.setVisibility(View.GONE);


            FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            String userId = "";
            if (user != null) {
                userId = user.getUid();
            }
            DatabaseReference wordDB = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(userId).child("collection").child(word);


            // 권한 확인 및 요청
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                // 권한이 이미 허용된 경우 이미지 표시
                if(LabelList.hasLabel(word)){
                    fetchImagePathAndDisplay(wordDB);
                }
            }



            // "사진 추가하기" 버튼 클릭 이벤트 처리
            if (LabelList.hasLabel(word)) {
                addPhotoButton.setVisibility(View.VISIBLE);
                addPhotoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 바로 카메라로 넘어가기
                        if (ContextCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            Intent intent = new Intent(DetailActivity.this, CatchActivity.class);
                            intent.putExtra("word", word);
                            startActivity(intent);
                        } else {
                            // 권한 요청
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        }
                    }
                });
            } else {
                addPhotoButton.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "단어 데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 뒤로가기 버튼 클릭 이벤트 처리
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이전 페이지로 이동
                Intent intent = new Intent(DetailActivity.this, DictionaryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button selectButton = findViewById(R.id.select);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이전 페이지로 이동
                Intent intent = new Intent(DetailActivity.this, DictionaryActivity.class);
                startActivity(intent);
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
    private void fetchImagePathAndDisplay(DatabaseReference wordDB) {
        wordDB.child("path").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String imagePath = dataSnapshot.getValue(String.class);
                    if (imagePath != null) {
                        // 이미지 경로가 있을 경우 이미지 표시
                        photoImageView.setVisibility(View.VISIBLE);
                        wordTextView.setTextSize(22);
                        meaningTextView.setTextSize(18);
                        Log.i("URI", imagePath);
                        displayImage(imagePath);
                    } else {
                        Toast.makeText(DetailActivity.this, "이미지 경로가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailActivity.this, "단어 그림을 수집해봐요!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DetailActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayImage(String imagePath) {
        File files = new File(imagePath);
        try {
            //Uri imageUri = Uri.parse(imagePath);
            Bitmap myBitmap = BitmapFactory.decodeFile(files.getAbsolutePath());
            photoImageView.setImageBitmap(myBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 뒤로가기 버튼을 눌렀을 때 DictionaryActivity로 이동
        Intent intent = new Intent(DetailActivity.this, DictionaryActivity.class);
        startActivity(intent);
        finish();
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