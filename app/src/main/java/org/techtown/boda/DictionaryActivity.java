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

// DictionaryActivity
public class DictionaryActivity extends BaseActivity {

    private DatabaseReference databaseRef;
    private AllWordsFragment allWordsInstance;
    private ImgWordsFragment imgWordsInstance;
    private TextView wordCountTextView;
    private int allWordsCount = 0;
    private int imgWordsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        ViewPager viewPager = findViewById(R.id.view_pager);
        EditText searchBar = findViewById(R.id.search_bar);
        Button homeButton = findViewById(R.id.button3);
        Button allWordsButton = findViewById(R.id.button_all_words);
        Button imgWordsButton = findViewById(R.id.button_img_words);
        wordCountTextView = findViewById(R.id.wordCount);

        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        vpAdapter.addFragment(new AllWordsFragment(), "전체");
        vpAdapter.addFragment(new ImgWordsFragment(), "그림 도감");
        viewPager.setAdapter(vpAdapter);

        allWordsInstance = (AllWordsFragment) vpAdapter.getItem(0);
        imgWordsInstance = (ImgWordsFragment) vpAdapter.getItem(1);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

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
            public void onPageScrollStateChanged(int state) {}
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DictionaryActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        allWordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
                updateWordCount(0);
                allWordsButton.setBackgroundResource(R.drawable.activity_dictionary_button1);
                imgWordsButton.setBackgroundResource(R.drawable.activity_dictionary_button2);
            }
        });

        imgWordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                updateWordCount(1);
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

                    // LabelList에 해당하는 단어들과 해당하지 않는 단어들을 필터링하여 전달
                    List<WordData> imgWordList = getImgWords(wordDataList);
                    List<WordData> nonImgWordList = getNonImgWords(wordDataList);

                    allWordsCount = nonImgWordList.size();
                    imgWordsCount = imgWordList.size();

                    allWordsInstance.updateData(nonImgWordList); // LabelList에 해당하지 않는 단어 전달
                    imgWordsInstance.updateData(imgWordList); // LabelList에 해당하는 단어 전달

                    int currentTabPosition = viewPager.getCurrentItem();
                    updateWordCount(currentTabPosition);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(DictionaryActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            });
        }

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

    private List<WordData> getNonImgWords(List<WordData> wordList) { // 새로 추가된 메서드
        List<WordData> nonImgWords = new ArrayList<>();
        for (WordData word : wordList) {
            if (!LabelList.hasLabel(word.getWord())) {
                nonImgWords.add(word);
            }
        }
        return nonImgWords;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DictionaryActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
