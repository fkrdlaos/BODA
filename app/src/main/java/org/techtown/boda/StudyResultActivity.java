package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudyResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_result);

        TextView tv_result = findViewById(R.id.tv_result);
        TextView tv_result3 = findViewById(R.id.tv_result3);
        TextView tv_eva = findViewById(R.id.tv_eva);
        Button btnHome = findViewById(R.id.btn_home);
        ProgressBar progressBar = findViewById(R.id.circularProgress);

        Intent intent = getIntent();
        int totalWords = intent.getIntExtra("totalWords", 0);
        int correctCount = intent.getIntExtra("correctCount", 0);

        double accuracy = (double) correctCount / totalWords * 100;
        int progress = (int) accuracy;

        progressBar.setProgress(progress); // 프로그래스바에 정확도를 설정

        String evaluation;
        if (accuracy >= 90) {
            evaluation = "훌륭합니다! 매우 뛰어난 성적이네요!";
        } else if (accuracy >= 70) {
            evaluation = "잘했습니다! 계속 노력해보세요!";
        } else {
            evaluation = "조금 더 노력하면 될 거예요. 화이팅!";
        }

        tv_result.setText(String.valueOf(correctCount));
        tv_result3.setText(String.valueOf(totalWords));
        tv_eva.setText(evaluation);

        // Firebase에서 현재 사용자의 ID 가져오기
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        // 미션 및 경험치 업데이트
        MissionManager.updateChallegeMission(StudyResultActivity.this, userId);
        ExpManager.updateExp(StudyResultActivity.this, userId, correctCount);

        // 홈으로 버튼 클릭 이벤트 처리
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(StudyResultActivity.this, MainActivity.class);
                startActivity(homeIntent);
                finish();
            }
        });
    }
    public void onBackPressed() {

        super.onBackPressed();
        // 뒤로가기 버튼을 눌렀을 때
        Intent intent = new Intent(StudyResultActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
