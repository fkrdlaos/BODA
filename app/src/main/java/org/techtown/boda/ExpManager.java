package org.techtown.boda;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ExpManager {
    private static DatabaseReference dbRef = null;

    private ExpManager(){}

    public static void updateExp(Context context, int count) {
        // 새로운 단어에 대한 exp 증가
        if(dbRef==null){
            dbRef = RTDatabase.getUserDBRef();
        }
        dbRef.child("exp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 기존 exp 값이 있는 경우
                    int currentExp = dataSnapshot.getValue(Integer.class);
                    // 새로 추가된 단어의 갯수만큼 exp를 증가시킴
                    int addedExp = count * 10;
                    currentExp += addedExp;
                    // exp 값을 업데이트
                    dbRef.child("exp").setValue(currentExp);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
