package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.techtown.boda.WordData;
import org.techtown.boda.WordDataAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DictionaryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WordDataAdapter adapter;
    private DatabaseReference databaseRef;
    private EditText searchBar;
    private List<WordData> originalWordDataList;
    private List<WordData> filteredWordDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        WordDataAdapter.OnItemClickListener listener = new WordDataAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(DictionaryActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        };

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            databaseRef = FirebaseDatabase.getInstance().getReference()
                    .child("BODA").child("UserAccount").child(userId).child("collection");

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<WordData> wordDataList = new ArrayList<>();
                    originalWordDataList = new ArrayList<>();
                    filteredWordDataList = new ArrayList<>();
                    int wordCount = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String word = snapshot.getKey();
                        String meaning = snapshot.child("meanings").getValue(String.class);
                        String sentence = snapshot.child("sentence").getValue(String.class);
                        String dateTime = snapshot.child("date_time").getValue(String.class);
                        WordData wordData = new WordData(word, meaning, sentence, dateTime);

                        wordDataList.add(wordData);
                        originalWordDataList.add(wordData);
                        filteredWordDataList.add(wordData);
                        wordCount++;
                    }
                    adapter = new WordDataAdapter(filteredWordDataList, listener);
                    recyclerView.setAdapter(adapter);

                    TextView wordCountTextView = findViewById(R.id.wordCount);
                    wordCountTextView.setText("발견한 단어 갯수 : " + wordCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(DictionaryActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Button homeButton = findViewById(R.id.button3);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DictionaryActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void filter(String text) {
        filteredWordDataList.clear();
        if (text.isEmpty()) {
            filteredWordDataList.addAll(originalWordDataList);
        } else {
            text = text.toLowerCase(Locale.getDefault());
            for (WordData wordData : originalWordDataList) {
                if (wordData.getWord().toLowerCase(Locale.getDefault()).contains(text)) {
                    filteredWordDataList.add(wordData);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
