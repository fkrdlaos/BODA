package org.techtown.boda;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
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

        animateProgressBar(progressBar, 0, progress); // 프로그래스바에 정확도를 애니메이션으로 설정

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

        // 팝업창 표시
        showAnimatedPopup("경험치 획득", "+" + correctCount * 10 + " EXP");

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 뒤로가기 버튼을 눌렀을 때
        Intent intent = new Intent(StudyResultActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // 프로그래스바 애니메이션 설정
    private void animateProgressBar(ProgressBar progressBar, int start, int end) {
        ValueAnimator animation = ValueAnimator.ofInt(start, end);
        animation.setDuration(1000); // 애니메이션 지속 시간 (밀리초)
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progressBar.setProgress((int) animation.getAnimatedValue());
            }
        });
        animation.start();
    }

    // 팝업창 표시 메서드
    private void showAnimatedPopup(String title, String message) {
        // Create AlertDialog with custom animation
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .create();

        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();

        // Fade out animation after 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeOutAndDismiss(dialog);
            }
        }, 1000); // 1 seconds delay before fade-out animation starts
    }

    // Fade out animation for dismissing the dialog
    private void fadeOutAndDismiss(final AlertDialog dialog) {
        final Window window = dialog.getWindow();
        if (window != null) {
            final View view = window.getDecorView();
            ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
            animator.setDuration(300); // Animation duration (milliseconds)
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float alpha = (float) animation.getAnimatedValue();
                    view.setAlpha(alpha);
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    dialog.dismiss(); // Dismiss the dialog after fade-out animation
                }
            });
            animator.start();
        }
    }
}

