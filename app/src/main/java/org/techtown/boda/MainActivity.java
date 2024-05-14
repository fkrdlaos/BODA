package org.techtown.boda;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_PICK_IMAGE = 102;

    private TextView tvResult;
    private ProgressBar progressBarExp;
    private TextView textViewExp;
    private TextView textViewLevel; // 레벨을 표시하는 TextView 추가

    private SharedPreferences sharedPreferences;

    private ImageButton btnCamera, btnDictionary, btnStudy;
    private Button btnSettings;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;

    private ProgressDialog progressDialog;

    private int exp = 0;
    private int lv = 1;
    private int maxExp = 100;

    // Sample data for dictionary
    private List<String> words = new ArrayList<>();
    private List<String> meanings = new ArrayList<>();
    private List<String> examples = new ArrayList<>();
    private List<String> dateTime = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String userId = "";
        if (user != null) {
            userId = user.getUid();
        }
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(userId);
        RTDatabase.getInstance(userId); // DB 최초 접근 시 경로 설정 용

        tvResult = findViewById(R.id.tv_result);
        textViewExp = findViewById(R.id.textView2);
        progressBarExp = findViewById(R.id.progress_bar_exp);
        textViewLevel = findViewById(R.id.textView_level); // 레벨을 표시하는 TextView 추가

        sharedPreferences = getSharedPreferences("MY_PREFS", MODE_PRIVATE);

        String nickname = sharedPreferences.getString("nickname", "");
        tvResult.setText(nickname);

        btnCamera = findViewById(R.id.btnCamera);
        btnDictionary = findViewById(R.id.btn_Dictionary);
        btnStudy = findViewById(R.id.btn_Study);
        btnSettings = findViewById(R.id.btn_settings);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("사진 선택")
                        .setItems(new CharSequence[]{"카메라로 사진 찍기", "갤러리에서 사진 가져오기"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // 카메라로 사진 찍기 선택
                                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                                        } else {
                                            dispatchTakePictureIntent();
                                        }
                                        break;
                                    case 1: // 갤러리에서 사진 가져오기 선택
                                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        pickPhotoIntent.setType("image/*");
                                        startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE);
                                        break;
                                }
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        btnDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<WordData> wordDataList = new ArrayList<>();
                for (int i = 0; i < words.size(); i++) {
                    WordData wordData = new WordData(words.get(i), meanings.get(i), examples.get(i), dateTime.get(i));
                    wordDataList.add(wordData);
                }

                Intent dictionaryIntent = new Intent(MainActivity.this, DictionaryActivity.class);
                dictionaryIntent.putExtra("wordDataList", (ArrayList<WordData>) wordDataList);
                startActivity(dictionaryIntent);
            }
        });

        btnStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent studyIntent = new Intent(MainActivity.this, StudyStartActivity.class);
                startActivity(studyIntent);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Firebase에서 경험치, 레벨 및 최대 경험치 정보 읽어오기
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    exp = dataSnapshot.child("exp").getValue(Integer.class);
                    lv = dataSnapshot.child("lv").getValue(Integer.class);

                    // maxExp 값이 존재하지 않는 경우 기본값 설정
                    if (dataSnapshot.hasChild("maxExp")) {
                        maxExp = dataSnapshot.child("maxExp").getValue(Integer.class);
                    } else {
                        // 기본값으로 설정할 최대 경험치 값
                        maxExp = DEFAULT_MAX_EXP;
                        // 데이터베이스에 기본값 저장
                        mDatabaseRef.child("maxExp").setValue(maxExp);
                    }

                    // 경험치, 레벨 및 최대 경험치 정보 업데이트
                    updateExpAndLevelViews();

                    // 경험치가 최대값에 도달했을 경우 레벨 업 및 경험치 초기화
                    if (exp >= maxExp) {
                        levelUp();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
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

    private static final int DEFAULT_MAX_EXP = 100; // 기본 최대 경험치 값

    private void updateExpAndLevelViews() {
        textViewExp.setText("EXP " + exp);
        textViewLevel.setText("Lv. " + lv); // 레벨을 표시
        progressBarExp.setProgress(exp);
        progressBarExp.setMax(maxExp);
    }

    private void levelUp() {
        lv++; // 레벨 증가
        exp -= maxExp; // 현재 경험치에서 최대 경험치를 뺌
        maxExp *= 2; // 최대 경험치 2배 증가

        // 레벨 및 경험치 업데이트
        updateExpAndLevelViews();

        // Firebase에 업데이트된 레벨, 경험치 및 최대 경험치 정보 저장
        mDatabaseRef.child("exp").setValue(exp);
        mDatabaseRef.child("lv").setValue(lv);
        mDatabaseRef.child("maxExp").setValue(maxExp);
    }


    private String saveImageToFile(Bitmap imageBitmap) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // 외부 저장소의 경로를 가져옴
        File directory = cw.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(directory, "image.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file.getAbsolutePath();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // 카메라로 사진 찍은 경우
                if (data != null && data.getExtras() != null) {
                    // Display progress dialog
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("사진에서 문장과 단어를 추출하고 있습니다...");
                    progressDialog.setCancelable(false);
                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }
                    });
                    progressDialog.show();

                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        // Save image to file and get file path
                        String imagePath = saveImageToFile(imageBitmap);

                        // Send image file path to HttpsTask class for caption extraction
                        new HttpsTask(this, imagePath).execute();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "카메라로부터 이미지를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "카메라로부터 이미지를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_PICK_IMAGE) {
                // 갤러리에서 사진을 선택한 경우
                if (data != null && data.getData() != null) {
                    // Display progress dialog
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("사진에서 문장과 단어를 추출하고 있습니다...");
                    progressDialog.setCancelable(false);
                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }
                    });
                    progressDialog.show();

                    Uri selectedImageUri = data.getData();
                    try {
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        if (imageBitmap != null) {
                            // Save image to file and get file path
                            String imagePath = saveImageToFile(imageBitmap);

                            // Send image file path to HttpsTask class for caption extraction
                            new HttpsTask(this, imagePath).execute();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(this, "갤러리에서 이미지를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "갤러리에서 이미지를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "갤러리에서 이미지를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent memory leaks by dismissing the dialog when the activity is destroyed
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}