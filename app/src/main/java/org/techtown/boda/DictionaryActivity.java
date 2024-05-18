// DictionaryActivity.java
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
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DictionaryActivity extends AppCompatActivity {

    private DatabaseReference databaseRef;

    private AllWordsFragment allWordsInstance;
    private ImgWordsFragment imgWordsInstance;

    private TextView wordCountTextView; // 발견한 단어 갯수를 표시할 TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        ViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        EditText searchBar = findViewById(R.id.search_bar);
        Button homeButton = findViewById(R.id.button3);
        wordCountTextView = findViewById(R.id.wordCount); // TextView 연결

        // Adapter 설정
        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        vpAdapter.addFragment(new AllWordsFragment(), "전체");
        vpAdapter.addFragment(new ImgWordsFragment(), "그림 도감");
        viewPager.setAdapter(vpAdapter);
        tabLayout.setupWithViewPager(viewPager);

        // 각 도감 fragment 인스턴스 가져오기
        allWordsInstance = (AllWordsFragment) vpAdapter.getItem(0);
        imgWordsInstance = (ImgWordsFragment) vpAdapter.getItem(1);

        // 홈 버튼 클릭 시 MainActivity로 이동
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DictionaryActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            databaseRef = FirebaseDatabase.getInstance().getReference()
                    .child("BODA").child("UserAccount").child(userId).child("collection");

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<WordData> wordDataList = new ArrayList<>();
                    int wordCount = 0; // 발견한 단어의 개수를 세기 위한 변수
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String word = snapshot.getKey();
                        String meaning = snapshot.child("meanings").getValue(String.class);
                        String sentence = snapshot.child("sentence").getValue(String.class);
                        String dateTime = snapshot.child("date_time").getValue(String.class);
                        WordData wordData = new WordData(word, meaning, sentence, dateTime);
                        wordDataList.add(wordData);
                        wordCount++; // 단어를 추가할 때마다 개수를 증가시킴
                    }
                    List<WordData> imgWordList = getImgWords(wordDataList);

                    // fragment1에 데이터 전달
                    allWordsInstance.updateData(wordDataList);
                    imgWordsInstance.updateData(imgWordList);

                    // 발견한 단어의 개수를 TextView에 설정
                    wordCountTextView.setText("발견한 단어 갯수 : " + wordCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(DictionaryActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 검색 기능 구현
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                allWordsInstance.filter(editable.toString());
            }
        });
    }


    private List<WordData> getImgWords(List<WordData> wordList){
        List<WordData> imgWords = new ArrayList<WordData>();
        for(WordData word : wordList){
            if(LabelList.hasLabel(word.getWord())){
                imgWords.add(word);
            }
        }

        return imgWords;
    }
}

