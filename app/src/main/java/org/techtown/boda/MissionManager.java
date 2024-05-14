package org.techtown.boda;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class MissionManager {
    private static DatabaseReference dbRef = null;

    private MissionManager(){}

    public static void updateWordMission(Context context, int count) {
        // 새로운 단어마다 미션 1씩 증가
        if(dbRef==null){
            dbRef = RTDatabase.getUserDBRef();
        }
        dbRef.child("/mission/words").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 기존 exp 값이 있는 경우
                    int currentWords = dataSnapshot.getValue(Integer.class);
                    // 새로 추가된 단어의 갯수만큼 exp를 증가시킴
                    currentWords += count;
                    // exp 값을 업데이트
                    dbRef.child("/mission/words").setValue(currentWords);
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
        if(dbRef==null){
            dbRef = RTDatabase.getUserDBRef();
        }
        dbRef.child("/mission/challenges").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 기존 exp 값이 있는 경우
                    int currentChallenges = dataSnapshot.getValue(Integer.class);
                    // 새로 추가된 단어의 갯수만큼 exp를 증가시킴
                    currentChallenges++;
                    // exp 값을 업데이트
                    dbRef.child("/mission/challenges").setValue(currentChallenges);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
