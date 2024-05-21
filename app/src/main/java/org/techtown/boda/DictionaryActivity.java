package org.techtown.boda;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

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

    private int allWordsCount = 0; // 전체 단어 개수
    private int imgWordsCount = 0; // 그림 도감 단어 개수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        ViewPager viewPager = findViewById(R.id.view_pager);
        EditText searchBar = findViewById(R.id.search_bar);
        Button homeButton = findViewById(R.id.button3);
        Button allWordsButton = findViewById(R.id.button_all_words);
        Button imgWordsButton = findViewById(R.id.button_img_words);
        wordCountTextView = findViewById(R.id.wordCount); // TextView 연결

        // Adapter 설정
        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        vpAdapter.addFragment(new AllWordsFragment(), "전체");
        vpAdapter.addFragment(new ImgWordsFragment(), "그림 도감");
        viewPager.setAdapter(vpAdapter);

        // 각 도감 fragment 인스턴스 가져오기
        allWordsInstance = (AllWordsFragment) vpAdapter.getItem(0);
        imgWordsInstance = (ImgWordsFragment) vpAdapter.getItem(1);

        // ViewPager의 페이지 전환 리스너 설정
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // 무시
            }

            @Override
            public void onPageSelected(int position) {
                updateWordCount(position);
                if (position == 0) {
                    allWordsButton.setBackgroundResource(R.drawable.activity_dictionary_button1);
                    imgWordsButton.setBackgroundResource(R.drawable.activity_dictionary_button2);
                } else if (position == 1) {
                    allWordsButton.setBackgroundResource(R.drawable.activity_dictionary_button2);
                    imgWordsButton.setBackgroundResource(R.drawable.activity_dictionary_button1);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 무시
            }
        });

        // 홈 버튼 클릭 시 MainActivity로 이동
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DictionaryActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 각 버튼 클릭 시 해당 fragment로 전환
        allWordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
                updateWordCount(0);
                // 버튼 배경 변경
                allWordsButton.setBackgroundResource(R.drawable.activity_dictionary_button1);
                imgWordsButton.setBackgroundResource(R.drawable.activity_dictionary_button2);
            }
        });

        imgWordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                updateWordCount(1);
                // 버튼 배경 변경
                allWordsButton.setBackgroundResource(R.drawable.activity_dictionary_button2);
                imgWordsButton.setBackgroundResource(R.drawable.activity_dictionary_button1);
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
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String word = snapshot.getKey();
                        String meaning = snapshot.child("meanings").getValue(String.class);
                        String sentence = snapshot.child("sentence").getValue(String.class);
                        String dateTime = snapshot.child("date_time").getValue(String.class);
                        WordData wordData = new WordData(word, meaning, sentence, dateTime);
                        wordDataList.add(wordData);
                    }
                    List<WordData> imgWordList = getImgWords(wordDataList);

                    allWordsCount = wordDataList.size(); // 전체 단어 개수 설정
                    imgWordsCount = imgWordList.size(); // 그림 도감 단어 개수 설정

                    // fragment1에 데이터 전달
                    allWordsInstance.updateData(wordDataList);
                    imgWordsInstance.updateData(imgWordList);

                    // 현재 선택된 탭에 따라 단어 개수를 TextView에 설정
                    int currentTabPosition = viewPager.getCurrentItem();
                    updateWordCount(currentTabPosition);
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
                String query = editable.toString();
                allWordsInstance.filter(query);
                imgWordsInstance.filter(query);
            }
        });
    }

    // 각 탭에 따른 단어 개수를 업데이트하는 메서드
    private void updateWordCount(int tabPosition) {
        if (tabPosition == 0) {
            wordCountTextView.setText("발견한 단어 개수 : " + allWordsCount);
        } else if (tabPosition == 1) {
            wordCountTextView.setText("발견한 단어 개수 : " + imgWordsCount);
        }
    }

    private List<WordData> getImgWords(List<WordData> wordList) {
        List<WordData> imgWords = new ArrayList<>();
        for (WordData word : wordList) {
            if (LabelList.hasLabel(word.getWord())) {
                imgWords.add(word);
            }
        }
        return imgWords;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 뒤로가기 버튼을 눌렀을 때 DictionaryActivity로 이동
        Intent intent = new Intent(DictionaryActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
