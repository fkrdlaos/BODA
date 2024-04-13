package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StudyResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_result);

        TextView tv_result = findViewById(R.id.tv_result);
        Button btnHome = findViewById(R.id.btn_home);

        // 학습 결과 정보를 받아옴
        Intent intent = getIntent();
        int totalWords = intent.getIntExtra("totalWords", 0);
        int correctCount = intent.getIntExtra("correctCount", 0);

        // 정확도 계산
        double accuracy = (double) correctCount / totalWords * 100;

        // 정확도에 따라 평가 문구 설정
        String evaluation;
        if (accuracy >= 90) {
            evaluation = "훌륭합니다! 매우 뛰어난 성적이네요!";
        } else if (accuracy >= 70) {
            evaluation = "잘했습니다! 계속 노력해보세요!";
        } else {
            evaluation = "조금 더 노력하면 될 거예요. 화이팅!";
        }

        // 결과 텍스트 설정
        String resultText = "총 " + totalWords + "개의 단어 중 " + correctCount + "개를 맞췄습니다.\n";
        resultText += "정확도: " + String.format("%.2f", accuracy) + "%\n";
        resultText += "평가: " + evaluation;

        // 결과 텍스트를 화면에 표시
        tv_result.setText(resultText);
        // 홈으로 버튼 클릭 이벤트 처리
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MainActivity로 이동
                Intent homeIntent = new Intent(StudyResultActivity.this, MainActivity.class);
                startActivity(homeIntent);
                finish(); // 현재 Activity 종료
            }
        });
    }
}
