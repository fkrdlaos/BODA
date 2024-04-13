package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DictionaryActivity extends AppCompatActivity {

    private List<WordData> wordDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        // Get words, meanings, and examples from intent
        ArrayList<String> words = getIntent().getStringArrayListExtra("words");
        final ArrayList<String> meanings = getIntent().getStringArrayListExtra("meanings");
        final ArrayList<String> examples = getIntent().getStringArrayListExtra("examples");

        // Create a list to hold WordData objects
        wordDataList = new ArrayList<>();

        // Populate the list with WordData objects
        for (int i = 0; i < words.size(); i++) {
            wordDataList.add(new WordData(words.get(i), meanings.get(i), examples.get(i)));
        }

        // Sort the wordDataList alphabetically based on word
        Collections.sort(wordDataList, new Comparator<WordData>() {
            @Override
            public int compare(WordData o1, WordData o2) {
                return o1.getWord().compareToIgnoreCase(o2.getWord());
            }
        });

        // Create ArrayAdapter with sorted words
        final ArrayAdapter<WordData> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wordDataList);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get selected word data
                WordData selectedWordData = wordDataList.get(position);

                // Start DetailActivity with selected word data
                Intent detailIntent = new Intent(DictionaryActivity.this, DetailActivity.class);
                detailIntent.putExtra("wordData", selectedWordData);
                startActivity(detailIntent);
            }
        });

        // Button to go back to MainActivity
        Button homeButton = findViewById(R.id.button3);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MainActivity
                Intent intent = new Intent(DictionaryActivity.this, MainActivity.class);
                startActivity(intent);
                // Finish current activity

            }
        });

        // Button to sort the list alphabetically
        Button sortButton = findViewById(R.id.sortbutton);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sort wordDataList alphabetically based on word
                Collections.sort(wordDataList, new Comparator<WordData>() {
                    @Override
                    public int compare(WordData o1, WordData o2) {
                        return o1.getWord().compareToIgnoreCase(o2.getWord());
                    }
                });
                // Notify adapter of data change
                adapter.notifyDataSetChanged();
            }
        });
    }
}
