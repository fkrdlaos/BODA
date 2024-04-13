package org.techtown.boda;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_PICK = 102;

    private TextView tv_result;
    private ImageView iv_profile;
    private ImageView imageView;
    private Button btnCamera, btnDictionary, btnStudy, btn_logout;
    private FirebaseAuth mFirebaseAuth;

    // Sample data for dictionary
    private List<String> words = new ArrayList<>();
    private List<String> meanings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Sample data for dictionary
        words.add("Apple");
        words.add("Banana");
        words.add("Cat");
        meanings.add("사과");
        meanings.add("바나나");
        meanings.add("고양이");

        Intent intent = getIntent();
        String nickName = intent.getStringExtra("nickName");
        String photoUrl = intent.getStringExtra("photoUrl");

        tv_result = findViewById(R.id.tv_result);
        tv_result.setText(nickName);

        iv_profile = findViewById(R.id.iv_profile);
        Glide.with(this).load(photoUrl).into(iv_profile);

        btnCamera = findViewById(R.id.btnCamera);
        btnDictionary = findViewById(R.id.btn_Dictionary);

        btnStudy = findViewById(R.id.btn_Study);

        btn_logout = findViewById(R.id.btn_logout);

        imageView = findViewById(R.id.imageView);

        mFirebaseAuth = FirebaseAuth.getInstance();

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });

        btnDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start DictionaryActivity
                Intent dictionaryIntent = new Intent(MainActivity.this, DictionaryActivity.class);
                dictionaryIntent.putStringArrayListExtra("words", (ArrayList<String>) words);
                dictionaryIntent.putStringArrayListExtra("meanings", (ArrayList<String>) meanings);
                startActivity(dictionaryIntent);
            }
        });

        btnStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start StudyActivity
                Intent studyIntent = new Intent(MainActivity.this, StudyActivity.class);
                studyIntent.putStringArrayListExtra("words", (ArrayList<String>) words);
                startActivity(studyIntent);
            }
        });


        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "카메라 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (data != null && data.getExtras() != null) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        imageView.setImageBitmap(imageBitmap);
                        // 다음 화면으로 이미지 전달
                        Intent nextIntent = new Intent(MainActivity.this, cameraActivity.class);
                        nextIntent.putExtra("imageBitmap", imageBitmap);
                        startActivity(nextIntent);
                    } else {
                        Toast.makeText(this, "사진을 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "사진을 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null && data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        imageView.setImageBitmap(bitmap);
                        // 다음 화면으로 이미지 전달
                        Intent nextIntent = new Intent(MainActivity.this, cameraActivity.class);
                        nextIntent.putExtra("imageBitmap", bitmap);
                        startActivity(nextIntent);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "사진을 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "사진을 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
