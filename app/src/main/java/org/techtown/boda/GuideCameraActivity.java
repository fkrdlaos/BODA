package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GuideCameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_camera);

        // Find the button by its ID and set an OnClickListener
        Button nextButton = findViewById(R.id.next1);
        nextButton.setOnClickListener(view -> {
            // Create an Intent to start GuideDictionaryActivity
            Intent intent = new Intent(GuideCameraActivity.this, GuideDictionaryActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Exit the app when the back button is pressed
        super.onBackPressed();
        // 현재 액티비티와 연관된 모든 액티비티를 종료하고 앱을 종료합니다.
        finishAffinity();
        // 프로세스를 완전히 종료하여 백그라운드에서 동작하지 않도록 합니다.
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}