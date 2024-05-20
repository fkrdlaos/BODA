package org.techtown.boda;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ExpManager {
    public static void updateExp(Context context, String userId, int count) {
        DatabaseReference userDbRef = RTDatabase.getUserDBRef(userId);
        if (userDbRef != null) {
            userDbRef.child("exp").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        int currentExp = dataSnapshot.getValue(Integer.class);
                        int addedExp = count * 10;
                        Log.i("EXPEXP", "currentEXP"+currentExp);
                        Log.i("EXPEXP", "addedExp"+currentExp);

                        currentExp += addedExp;
                        userDbRef.child("exp").setValue(currentExp);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 데이터베이스 참조가 null인 경우
            Toast.makeText(context, "사용자 데이터베이스를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            Log.e("ExpManager", "사용자 데이터베이스 참조가 null입니다.");
        }
    }
}



