package org.techtown.boda;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

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
        // 클릭 이벤트를 처리하기 위한 listener 생성
        WordDataAdapter.OnItemClickListener listener = new WordDataAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 아이템 클릭 시 처리할 로직 작성
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
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String word = snapshot.getKey();
                        String meaning = snapshot.getValue(String.class);
                        WordData wordData = new WordData(word, meaning, "");
                        wordDataList.add(wordData);
                    }
                    adapter = new WordDataAdapter(wordDataList,listener);
                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
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
}