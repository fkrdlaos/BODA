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

    public static void updateWordMission(Context context, String userId, int count) {
        if(wordRef==null){
            wordRef = RTDatabase.getUserDBRef(userId).child("/mission/words");
        }
        wordRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int currentWords = dataSnapshot.getValue(Integer.class);
                    currentWords += count;
                    wordRef.setValue(currentWords);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void updateChallegeMission(Context context, String userId) {
        if(chgRef==null){
            chgRef = RTDatabase.getUserDBRef(userId).child("/mission/challenges");
        }

        chgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int currentChallenges = dataSnapshot.getValue(Integer.class);
                    currentChallenges++;
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