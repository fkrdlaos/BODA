package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GuideQuestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_quest);

        // Find the buttons by their IDs and set OnClickListeners
        Button nextButton = findViewById(R.id.next4);
        nextButton.setOnClickListener(view -> {
            // Create an Intent to start GuideStudyActivity
            Intent intent = new Intent(GuideQuestActivity.this, GuideEvolutionActivity.class);
            startActivity(intent);
            finish();
        });

        Button beforeButton = findViewById(R.id.before3);
        beforeButton.setOnClickListener(view -> {
            // Create an Intent to go back to GuideCameraActivity
            Intent intent = new Intent(GuideQuestActivity.this, GuideStudyActivity.class);
            startActivity(intent);
            finish(); // Finish current activity to remove it from the back stack
        });
    }

    @Override
    public void onBackPressed() {
        // Navigate back to GuideCameraActivity when the back button is pressed
        super.onBackPressed();
        Intent intent = new Intent(GuideQuestActivity.this, GuideStudyActivity.class);
        startActivity(intent);
        finish(); // Finish current activity to remove it from the back stack
    }
}