package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StudyStartActivity extends AppCompatActivity {
    private Button btn_ss;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_study_start);

        btn_ss = findViewById(R.id.start_butto);

        btn_ss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(StudyStartActivity.this, StudyActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}