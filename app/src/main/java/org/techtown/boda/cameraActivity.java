package org.techtown.boda;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class cameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ImageView imageView = findViewById(R.id.imageViewNext);

        Bitmap imageBitmap = getIntent().getParcelableExtra("imageBitmap");
        if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap);
        }
        Button moveHomeButton = findViewById(R.id.button2);
        moveHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cameraActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Optional, if you want to finish current activity when moving to MainActivity
            }
        });
    }

}
