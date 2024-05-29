package org.techtown.boda;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SharedPreferences를 사용하여 앱의 첫 실행 여부를 확인
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (!isFirstRun) {
            // 첫 실행이 아니라면 LoginActivity로 바로 이동
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 첫 실행이라면 StartActivity 레이아웃 설정
        setContentView(R.layout.activity_start);

        btn_start = findViewById(R.id.button5);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 첫 실행이 끝났으므로 SharedPreferences에 isFirstRun을 false로 저장
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isFirstRun", false);
                editor.apply();

                // LoginActivity로 이동
                Intent intent = new Intent(StartActivity.this, GuideCameraActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
