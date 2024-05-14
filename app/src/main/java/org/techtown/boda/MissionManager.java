package org.techtown.boda;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class MissionManager {
    private static DatabaseReference wordRef = null;
    private static DatabaseReference chgRef = null;
    private MissionManager(){}

    public static void updateWordMission(Context context, int count) {
        // 새로운 단어마다 미션 1씩 증가
        if(wordRef==null){
            wordRef = RTDatabase.getUserDBRef().child("/mission/words");
        }
        wordRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 기존 exp 값이 있는 경우
                    int currentWords = dataSnapshot.getValue(Integer.class);
                    // 새로 추가된 단어의 갯수만큼 exp를 증가시킴
                    currentWords += count;
                    // exp 값을 업데이트
                    wordRef.setValue(currentWords);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void updateChallegeMission(Context context) {
        // 챌린지 완료할 때 마다
        if(chgRef==null){
            chgRef = RTDatabase.getUserDBRef().child("/mission/challenges");
            Log.i("chgRef null", "chgRef NULL");
        }
        Log.i("chgRef not null", "chgRef NOT NULL");

        chgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 기존 mission의 challenges 값이 있는 경우
                    int currentChallenges = dataSnapshot.getValue(Integer.class);
                    // 새로 추가된 단어의 갯수만큼 exp를 증가시킴
                    currentChallenges++;
                    // exp 값을 업데이트
                    Log.i("chgRef null", "${currentChallenges}");

                    chgRef.setValue(currentChallenges);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}