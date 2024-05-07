package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DictionaryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WordDataAdapter adapter;
    private List<WordData> wordList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        //단어 데이터 목록 받아옴
        //ArrayList<WordData> wordDataList = (ArrayList<WordData>) getIntent().getSerializableExtra("wordDataList");
        // 알파벳 순으로 정렬
        //Collections.sort(wordDataList, (word1, word2) -> word1.getWord().compareToIgnoreCase(word2.getWord()));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        wordList = new ArrayList<>();
        adapter = new WordDataAdapter(this,wordList);
        recyclerView.setAdapter(adapter);

        FirebaseUser curUser = FirebaseAuth.getInstance().getCurrentUser();
        if (curUser!=null){
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("BODA/UserAccount/" + curUser.getUid() + "/collection");
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //기존 데이터 초기화
                    wordList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        WordData word = snapshot.getValue(WordData.class);
                        wordList.add(word); // 단어 리스트에 추가
                    }
                    adapter.notifyDataSetChanged(); // RecyclerView 갱신
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }



        //홈버튼
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