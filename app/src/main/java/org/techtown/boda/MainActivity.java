package org.techtown.boda;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ObjectAnimator;

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
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_PICK_IMAGE = 102;

    private TextView tvResult;
    private ProgressBar progressBarExp;
    private TextView textViewExp;
    private TextView textViewLevel; // 레벨을 표시하는 TextView 추가
    private ImageView iv_profile;

    private SharedPreferences sharedPreferences;

    private Dialog loadingDialog;
    private HttpsTask httpsTask;


    private ImageButton btnCamera, btnDictionary, btnStudy;
    private Button btnSettings;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private RTDatabase instance;

    private ProgressDialog progressDialog;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private int exp = 0;
    private int lv = 1;
    private int maxExp = 0;

    private TextView quest1ProgressText, quest2ProgressText;
    private ImageView quest1MedalImage, quest2MedalImage;
    private Button quest1Button, quest2Button;

    // Sample data for dictionary
    private List<String> words = new ArrayList<>();
    private List<String> meanings = new ArrayList<>();
    private List<String> examples = new ArrayList<>();
    private List<String> dateTime = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 네트워크 상태 감지 설정
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> showNetworkDialog());
            }
        };

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String userId = "";
        if (user != null) {
            userId = user.getUid();
        }
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(userId);
        instance = RTDatabase.getInstance(userId); // DB 최초 접근 시 경로 설정 용
        Quest1RewardManager.initFirebase();
        Quest2RewardManager.initFirebase();

        tvResult = findViewById(R.id.tv_result);
        textViewExp = findViewById(R.id.textView2);
        progressBarExp = findViewById(R.id.progress_bar_exp);
        textViewLevel = findViewById(R.id.textView_level); // 레벨을 표시하는 TextView 추가
        iv_profile = findViewById(R.id.iv_profile);

        sharedPreferences = getSharedPreferences("MY_PREFS", MODE_PRIVATE);

        String nickname = sharedPreferences.getString("nickname", "");
        tvResult.setText(nickname);

        btnCamera = findViewById(R.id.btnCamera);
        btnDictionary = findViewById(R.id.btn_Dictionary);
        btnStudy = findViewById(R.id.btn_Study);
        btnSettings = findViewById(R.id.btn_settings);

        quest1ProgressText = findViewById(R.id.quest1_progress);
        quest2ProgressText = findViewById(R.id.quest2_progress);
        quest1MedalImage = findViewById(R.id.quest1_medal);
        quest2MedalImage = findViewById(R.id.quest2_medal);
        quest1Button = findViewById(R.id.quest1_button);
        quest2Button = findViewById(R.id.quest2_button);

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

        // 보상 버튼 클릭 이벤트 핸들러 추가
        quest1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 퀘스트 1의 진행도를 가져와서 보상 주기
                String progressText = quest1ProgressText.getText().toString();
                int quest1Progress = Integer.parseInt(progressText.substring(1, progressText.indexOf('/')).trim());
                Quest1RewardManager.giveQuest1Reward(getApplicationContext(), quest1Progress, quest1Button);

                // 레벨 및 경험치 업데이트
                updateExpAndLevelViews();

            }
        });

        quest2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 퀘스트 2의 진행도를 가져와서 보상 주기
                String progressText = quest2ProgressText.getText().toString();
                int quest2Progress = Integer.parseInt(progressText.substring(1, progressText.indexOf('/')).trim());
                Quest2RewardManager.giveQuest2Reward(getApplicationContext(), quest2Progress, quest2Button);

                // 레벨 및 경험치 업데이트
                updateExpAndLevelViews();

            }
        });






        // Firebase에서 경험치, 레벨 및 최대 경험치 정보 읽어오기
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer expValue = dataSnapshot.child("exp").getValue(Integer.class);
                    Integer lvValue = dataSnapshot.child("lv").getValue(Integer.class);
                    if (expValue != null && lvValue != null) {
                        exp = expValue;
                        lv = lvValue;
                        // maxExp 값이 존재하지 않는 경우 기본값 설정
                        Integer maxExpValue = dataSnapshot.child("maxExp").getValue(Integer.class);
                        if (maxExpValue != null) {
                            maxExp = maxExpValue;
                        } else {
                            // 기본값으로 설정할 최대 경험치 값
                            maxExp = DEFAULT_MAX_EXP;
                            // 데이터베이스에 기본값 저장
                            mDatabaseRef.child("maxExp").setValue(maxExp);
                        }
                        // 경험치, 레벨 및 최대 경험치 정보 업데이트
                        updateExpAndLevelViews();
                        // 경험치가 최대값에 도달했을 경우 레벨 업 및 경험치 초기화
//                        if (exp >= maxExp) {
//                            Log.i("LEVELUPUP", "STARTTTTTTTTTTTT");
//
//                            levelUp();
//                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Firebase에서 퀘스트 진행 상황을 가져오고 UI를 업데이트합니다.
        mDatabaseRef.child("mission").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int wordsProgress = dataSnapshot.child("words").getValue(Integer.class);
                    int challengesProgress = dataSnapshot.child("challenges").getValue(Integer.class);

                    // 퀘스트 진행 상황을 UI에 업데이트합니다.
                    updateQuestProgress(wordsProgress, challengesProgress);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseRef.child("exp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer expValue = dataSnapshot.getValue(Integer.class);
                    if (expValue != null) {
                        exp = expValue;
                        Log.d("Firebase", "Current exp: " + exp);
                        // exp를 사용하여 필요한 작업을 수행합니다.
                        if (exp >= maxExp && maxExp!=0) {
                            Log.i("LEVELUPUP", "STARTTTTTTTTTTTT");
                            levelUp();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        Log.i("ONSTARTTTTT", exp+"&"+maxExp);

    }


    // 퀘스트 진행 상황을 UI에 업데이트합니다.
    private void updateQuestProgress(int wordsProgress, int challengesProgress) {
        int wordsNextTarget = getNextTarget(wordsProgress);
        int challengesNextTarget = getNextTarget(challengesProgress);

        quest1ProgressText.setText("(" + wordsProgress + "/" + wordsNextTarget + ")");
        quest2ProgressText.setText("(" + challengesProgress + "/" + challengesNextTarget + ")");

        Quest1RewardManager.updateQuestProgress(wordsProgress, wordsNextTarget, quest1ProgressText, quest1MedalImage);
        Quest2RewardManager.updateQuestProgress(challengesProgress, challengesNextTarget, quest2ProgressText, quest2MedalImage);

        Quest1RewardManager.updateQuest1RewardButtonVisibility(this, wordsProgress, quest1Button);
        Quest2RewardManager.updateQuest2RewardButtonVisibility(this, challengesProgress, quest2Button);

        // 경험치 및 레벨 업데이트
        updateExpAndLevelViews();
    }

    private int getNextTarget(int currentProgress) {
        if (currentProgress < 10) return 10;
        else if (currentProgress < 30) return 30;
        else if (currentProgress < 50) return 50;
        else if (currentProgress < 80) return 80;
        else return 100;
    }





    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 이미지 품질 및 해상도 설정
            takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // 이미지 품질 설정
            takePictureIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024 * 1024); // 이미지 해상도 설정
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "카메라 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private static final int DEFAULT_MAX_EXP = 100; // 기본 최대 경험치 값

    private void updateExpAndLevelViews() {
        textViewExp.setText("EXP " + exp);
        textViewLevel.setText("Lv. " + lv); // 레벨을 표시
        ProfileManager.updateProfileByLevel(lv, iv_profile);

        // ProgressBar 애니메이션
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBarExp, "progress", progressBarExp.getProgress(), exp);
        progressAnimator.setDuration(500); // 애니메이션 지속 시간 (밀리초 단위)
        progressAnimator.start();

        progressBarExp.setMax(maxExp);
    }

    private void levelUp() {
        lv++; // 레벨 증가
        exp -= maxExp; // 현재 경험치에서 최대 경험치를 뺌
        maxExp += 50 ; // 최대 경험치 50 증가

        // 레벨 및 경험치 업데이트
        updateExpAndLevelViews();

        // Firebase에 업데이트된 레벨, 경험치 및 최대 경험치 정보 저장
        mDatabaseRef.child("exp").setValue(exp);
        mDatabaseRef.child("lv").setValue(lv);
        mDatabaseRef.child("maxExp").setValue(maxExp);

        // 레벨업 팝업창 생성
        NoticeDialog levelUpDialog = new NoticeDialog.Builder(MainActivity.this) // 수정된 부분
                .setTitle("레벨업")
                .setLeftMessage("Lv. " + (lv - 1))
                .setRightMessage("Lv. " + lv)
                .setCenterImage(R.drawable.lv_up_img)
                .build();
        levelUpDialog.showDialog(); // 수정된 부분

        // 애니메이션 설정
        ImageView centerImageView = levelUpDialog.getCenterImageView(); // 수정된 부분
        if (centerImageView != null) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(
                    0.5f, 1.5f, 0.5f, 1.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(1000);
            scaleAnimation.setRepeatCount(Animation.INFINITE);
            scaleAnimation.setRepeatMode(Animation.REVERSE);
            centerImageView.startAnimation(scaleAnimation);
        }

        // 진화 체크
        evolution(lv); // 수정된 부분

        // Toast.makeText(this, "레벨 업!", Toast.LENGTH_SHORT).show();
    }

    private void evolution(int lv) {
        if (lv % 10 == 0 && lv <= 40) {
            int gen = lv / 10;
            int prev_profile;
            int current_profile;
            switch (gen) {
                case 1:
                    prev_profile = R.drawable.gen1_profile;
                    current_profile = R.drawable.gen2_profile;
                    break;
                case 2:
                    prev_profile = R.drawable.gen2_profile;
                    current_profile = R.drawable.gen3_profile;
                    break;
                case 3:
                    prev_profile = R.drawable.gen3_profile;
                    current_profile = R.drawable.gen4_profile;
                    break;
                case 4:
                    prev_profile = R.drawable.gen4_profile;
                    current_profile = R.drawable.gen5_profile;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + gen);
            }
            NoticeDialog evolutionDialog = new NoticeDialog.Builder(MainActivity.this) // 수정된 부분
                    .setTitle("Evolution")
                    .setCenterMessage("!!!진화했어!!!")
                    .setCenterImage(prev_profile)
                    .build();
            evolutionDialog.showDialog(); // 수정된 부분

            // 애니메이션 설정
            ImageView centerImageView = evolutionDialog.getCenterImageView(); // 수정된 부분
            if (centerImageView != null) {
                RotateAnimation rotateAnimation = new RotateAnimation(
                        0f, 360f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(1000);
                rotateAnimation.setRepeatCount(Animation.INFINITE);

                AlphaAnimation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
                fadeOutAnimation.setDuration(1000);
                fadeOutAnimation.setStartOffset(1000);

                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(rotateAnimation);
                animationSet.addAnimation(fadeOutAnimation);

                centerImageView.startAnimation(animationSet);

                fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        centerImageView.setImageResource(current_profile);
                        AlphaAnimation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
                        fadeInAnimation.setDuration(1000);
                        centerImageView.startAnimation(fadeInAnimation);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
        }
    }


    // 다음 메서드는 이미지 파일을 저장하고 해당 파일 경로를 반환하는 데 사용됩니다.

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

    // 이미지 캡처 또는 갤러리에서 이미지를 가져온 후 호출되는 콜백 메서드입니다.
    // onActivityResult 메서드 수정
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_PICK_IMAGE) {
                // 로딩 다이얼로그 표시
                loadingDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
                loadingDialog.setContentView(R.layout.camera_loading);
                loadingDialog.setCancelable(false);

                // 배경색 설정
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000"))); // 반투명한 검정색 배경

                // GIF 이미지 설정
                ImageView loadingImageView = loadingDialog.findViewById(R.id.iv_frame_loading);
                Glide.with(this).load(R.drawable.loading).into(loadingImageView);

                // 로딩 중 텍스트 색상 설정
                TextView loadingTextView = loadingDialog.findViewById(R.id.tv_progress_message);
                loadingTextView.setTextColor(Color.WHITE); // 흰색 텍스트

                loadingDialog.show();

                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    int count = 0;

                    @Override
                    public void run() {
                        switch (count % 4) {
                            case 0:
                                loadingTextView.setText("Loading");
                                break;
                            case 1:
                                loadingTextView.setText("Loading.");
                                break;
                            case 2:
                                loadingTextView.setText("Loading..");
                                break;
                            case 3:
                                loadingTextView.setText("Loading...");
                                break;
                        }
                        count++;
                        if (count * 500 > 20000) { // 20초를 초과하면 작업 중단
                            handler.removeCallbacks(this); // Runnable을 제거하여 반복을 중지
                            loadingDialog.dismiss(); // 다이얼로그를 종료
                            if (httpsTask != null && !httpsTask.isCompleted()) {
                                httpsTask.cancelTask(); // HttpsTask도 중지시킴
                                Toast.makeText(MainActivity.this, "로딩 시간이 초과되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        handler.postDelayed(this, 500); // 0.5초마다 텍스트 변경
                    }
                };

                // 처음에 한 번 실행
                handler.post(runnable);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String imagePath = null;
                        if (requestCode == REQUEST_IMAGE_CAPTURE) {
                            if (data != null && data.getExtras() != null) {
                                Bundle extras = data.getExtras();
                                Bitmap imageBitmap = (Bitmap) extras.get("data");
                                if (imageBitmap != null) {
                                    imagePath = saveImageToFile(imageBitmap);
                                }
                            }
                        } else if (requestCode == REQUEST_PICK_IMAGE) {
                            if (data != null && data.getData() != null) {
                                Uri selectedImageUri = data.getData();
                                try {
                                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), selectedImageUri);
                                    if (imageBitmap != null) {
                                        imagePath = saveImageToFile(imageBitmap);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        final String finalImagePath = imagePath;
                        // 핸들러를 사용하여 메인 스레드에서 UI 갱신
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (finalImagePath != null) {
                                    // 이미지 추출 작업 시작
                                    httpsTask = new HttpsTask(MainActivity.this, finalImagePath);
                                    httpsTask.execute();
                                } else {
                                    Toast.makeText(MainActivity.this, "이미지를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                thread.start();

                // 쓰레드가 20초를 초과하면 종료
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (httpsTask != null && !httpsTask.isCompleted()) {
                            httpsTask.cancelTask(); // HttpsTask도 중지시킴
                            loadingDialog.dismiss(); // 다이얼로그 종료
                            Toast.makeText(MainActivity.this, "로딩 시간이 초과되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 20000);
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
        // 네트워크 콜백 해제
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException e) {
                // 예외 처리: 이미 콜백이 해제된 경우
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // 앱을 종료합니다.
        super.onBackPressed();
        // 현재 액티비티와 연관된 모든 액티비티를 종료하고 앱을 종료합니다.
        finishAffinity();
        // 프로세스를 완전히 종료하여 백그라운드에서 동작하지 않도록 합니다.
        Process.killProcess(Process.myPid());
    }
}