package org.techtown.boda;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

public class DictionaryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WordDataAdapter adapter;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

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
                    int wordCount = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String word = snapshot.getKey();
                        String meaning = snapshot.child("meanings").getValue(String.class);
                        String sentence = snapshot.child("sentence").getValue(String.class);
                        String dateTime = snapshot.child("dateTime").getValue(String.class); // "date_time"으로 수정
                        WordData wordData = new WordData(word, meaning, sentence, dateTime);

                        wordDataList.add(wordData);
                        wordCount++;
                    }
                    String grade = calculateGrade(wordCount);
                    adapter = new WordDataAdapter(wordDataList, listener);
                    recyclerView.setAdapter(adapter);

                    TextView wordCountTextView = findViewById(R.id.wordCount);
                    wordCountTextView.setText("발견한 단어 갯수 : " + wordCount + " (등급: " + grade + ")");

                    ProgressBar progressBar = findViewById(R.id.progressBar);
                    // 등급에 따른 Clip drawable을 설정
                    Drawable progressDrawable;
                    switch (grade) {
                        case "아이언":
                            progressDrawable = getResources().getDrawable(R.drawable.progress_bar_exp2);
                            break;
                        case "브론즈":
                            progressDrawable = getResources().getDrawable(R.drawable.progress_bar_exp2);
                            break;
                        case "실버":
                            progressDrawable = getResources().getDrawable(R.drawable.progress_bar_exp2);
                            break;
                        case "골드":
                            progressDrawable = getResources().getDrawable(R.drawable.progress_bar_exp2);
                            break;
                        case "플래티넘":
                            progressDrawable = getResources().getDrawable(R.drawable.progress_bar_exp2);
                            break;
                        default:
                            progressDrawable = getResources().getDrawable(R.drawable.progress_bar_exp2);
                            break;
                    }
                    progressBar.setProgressDrawable(progressDrawable);
                    // 단어 개수에 맞게 프로그래스 바의 진행 상태 설정
                    progressBar.setProgress(wordCount); // 이 부분이 추가됨
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

    private String calculateGrade(int wordCount) {
        if (wordCount >= 0 && wordCount <= 9) {
            return "아이언";
        } else if (wordCount >= 10 && wordCount <= 29) {
            return "브론즈";
        } else if (wordCount >= 30 && wordCount <= 59) {
            return "실버";
        } else if (wordCount >= 60 && wordCount <= 99) {
            return "골드";
        } else if (wordCount >= 100 && wordCount <= 149) {
            return "플래티넘";
        } else {
            return "다이아몬드";
        }
    }
}