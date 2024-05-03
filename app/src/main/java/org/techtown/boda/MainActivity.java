package org.techtown.boda;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.firebase.auth.FirebaseAuth;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_PICK_IMAGE = 102;

    private TextView tvResult;

    private SharedPreferences sharedPreferences;

    private ImageButton btnCamera, btnDictionary, btnStudy;
    private Button btnsettings;
    private FirebaseAuth mFirebaseAuth;

    // Sample data for dictionary
    private List<String> words = new ArrayList<>();
    private List<String> meanings = new ArrayList<>();
    private List<String> examples = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Sample data for dictionary
        /*words.add("Cat");
        words.add("elephant");
        words.add("Apple");
        words.add("Banana");
        words.add("Door");

        meanings.add("고양이");
        meanings.add("코끼리");
        meanings.add("사과");
        meanings.add("바나나");
        meanings.add("문");

        examples.add("Humans began keeping cats as pets to hunt rats and mice.");
        examples.add("The elephants are poached for their tusks.");
        examples.add("Eating more than one apple is prohibited");
        examples.add("I have a banana for lunch.");
        examples.add("Love is an open door");*/

        tvResult = findViewById(R.id.tv_result);

        sharedPreferences = getSharedPreferences("MY_PREFS", MODE_PRIVATE);

        String nickname = sharedPreferences.getString("nickname", "");
        tvResult.setText(nickname);

        btnCamera = findViewById(R.id.btnCamera);
        btnDictionary = findViewById(R.id.btn_Dictionary);
        btnStudy = findViewById(R.id.btn_Study);
        btnsettings = findViewById(R.id.btn_settings);

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
                    WordData wordData = new WordData(words.get(i), meanings.get(i), examples.get(i));
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
                Intent studyIntent = new Intent(MainActivity.this, StudyActivity.class);
                studyIntent.putStringArrayListExtra("words", (ArrayList<String>) words);
                startActivity(studyIntent);
            }
        });

        btnsettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
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
                    ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("사진에서 문장과 단어를 추출하고 있습니다...");
                    progressDialog.setCancelable(false);
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
                    ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("사진에서 문장과 단어를 추출하고 있습니다...");
                    progressDialog.setCancelable(false);
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

}