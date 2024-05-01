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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class DictionaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        ArrayList<WordData> wordDataList = (ArrayList<WordData>) getIntent().getSerializableExtra("wordDataList");
        // Sort words alphabetically
        Collections.sort(wordDataList, (word1, word2) -> word1.getWord().compareToIgnoreCase(word2.getWord()));
        // recycler view로 변경
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        WordDataAdapter adapter = new WordDataAdapter(wordDataList);
        recyclerView.setAdapter(adapter);

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