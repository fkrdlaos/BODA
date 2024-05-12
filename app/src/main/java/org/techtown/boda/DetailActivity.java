package org.techtown.boda;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    private TextToSpeech textToSpeech;
    private ImageView photoImageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 1001; // 카메라 권한 요청 코드
    private Uri photoUri;

    private int rotationAngle = 0; // 이미지 회전 각도 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get WordData object from Intent
        WordData wordData = (WordData) getIntent().getSerializableExtra("wordData");




        if (wordData != null) {
            String word = wordData.getWord();
            String meaning = wordData.getMeaning();
            String example = wordData.getExample();
            String dateTime = wordData.getDateTime(); // 필드명 수정

            TextView wordTextView = findViewById(R.id.wordTextView);
            TextView meaningTextView = findViewById(R.id.meaningTextView);
            TextView exampleTextView = findViewById(R.id.exampleTextView);
            TextView dateTextView = findViewById(R.id.dateTextView); // 날짜를 표시할 TextView

            wordTextView.setText("단어: " + word);
            meaningTextView.setText("의미: " + meaning);
            exampleTextView.setText("예문: " + example);
            dateTextView.setText("날짜: " + dateTime); // 날짜를 TextView에 설정

            // TTS 버튼 클릭 이벤트 처리
            Button ttsButton = findViewById(R.id.ttsButton);
            ttsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TTS 발음 듣기
                    String text = word + ". " + meaning;
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            });

            // 이미지뷰 초기화
            photoImageView = findViewById(R.id.photoImageView);

            // "사진 추가하기" 버튼 클릭 이벤트 처리
            Button addPhotoButton = findViewById(R.id.addPhotoButton);
            addPhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 카메라로 사진 찍기 또는 갤러리에서 사진 선택하기
                    showImageSourceDialog();
                }
            });
        } else {
            Toast.makeText(this, "단어 데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // "사진 회전" 버튼 클릭 이벤트 처리
        Button rotateImageButton = findViewById(R.id.rotateImageButton);
        rotateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이미지 회전
                rotateImage();
            }
        });

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

    // 사진을 찍을지 갤러리에서 선택할지 다이얼로그 표시
    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진 추가하기");
        builder.setItems(new CharSequence[]{"카메라로 사진 찍기", "갤러리에서 사진 선택하기"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // 카메라 권한이 있는지 확인
                        if (ContextCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            takePhotoFromCamera();
                        } else {
                            // 권한 요청
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        }
                        break;
                    case 1:
                        choosePhotoFromGallery();
                        break;
                }
            }
        });
        builder.show();
    }

    // 카메라로 사진 찍기
    private void takePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "org.techtown.boda.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // 갤러리에서 사진 선택하기
    private void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // 사진 파일 생성
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return imageFile;
    }

    // 이미지 회전 메서드
    private void rotateImage() {
        if (photoImageView != null && photoUri != null) {
            rotationAngle += 90;
            if (rotationAngle >= 360) {
                rotationAngle = 0;
            }

            try {
                // 이미지 회전
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                photoImageView.setImageBitmap(rotatedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // onActivityResult 메서드 안에 추가
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // 카메라에서 사진을 가져왔을 때
                photoImageView.setVisibility(View.VISIBLE);
                // 사진을 리사이징하여 이미지뷰에 설정
                Bitmap bitmap = resizeBitmap(photoUri);
                photoImageView.setImageBitmap(bitmap);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // 갤러리에서 사진을 가져왔을 때
                if (data != null && data.getData() != null) {
                    photoUri = data.getData();
                    photoImageView.setVisibility(View.VISIBLE);
                    // 사진을 리사이징하여 이미지뷰에 설정
                    Bitmap bitmap = resizeBitmap(photoUri);
                    photoImageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    // URI에서 비트맵 이미지를 가져오고 리사이징하는 메서드
    private Bitmap resizeBitmap(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = 500; // 원하는 폭
            int newHeight = (int) (height * ((float)newWidth / width));
            // 크기 조정
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            // 크기 조정한 이미지를 회전
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle);
            return Bitmap.createBitmap(scaledBitmap, 0, 0, newWidth, newHeight, matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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