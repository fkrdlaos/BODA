package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;

public class DictionaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        ArrayList<String> words = getIntent().getStringArrayListExtra("words");
        ArrayList<String> meanings = getIntent().getStringArrayListExtra("meanings");

        // Sort words alphabetically
        Collections.sort(words);

        ListView listView = findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, words);
        listView.setAdapter(adapter);

        Button homeButton = findViewById(R.id.button3);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start activity_main activity
                Intent intent = new Intent(DictionaryActivity.this, MainActivity.class);
                startActivity(intent);
                // Finish current activity
                finish();
            }
        });
    }
}