package org.techtown.boda;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Quest2RewardManager {

    // Constants for quest progress thresholds
    private static final int[] QUEST_THRESHOLDS = {10, 30, 50, 80, 100};

    // Firebase 관련 변수
    private static FirebaseAuth mAuth;
    private static DatabaseReference mDatabaseRef;

    // Initialize Firebase Auth and Database
    public static void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(mAuth.getCurrentUser().getUid());

        // Check if the "rewards" path exists, if not, create it
        mDatabaseRef.child("rewards").child("quest2").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // If the "rewards" path does not exist, create it
                    for (int threshold : QUEST_THRESHOLDS) {
                        mDatabaseRef.child("rewards").child("quest2").child(String.valueOf(threshold)).setValue(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Error handling
            }
        });
    }



    // Give reward based on the current progress for quest1Button
    public static void giveQuest2Reward(final Context context, final int currentProgress, final Button quest1Button) {
        mDatabaseRef.child("rewards").child("quest2").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean rewardReceived = false; // 보상을 받은 경우를 나타내는 플래그

                // 보상 상태를 가져와서 처리
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int threshold = Integer.parseInt(snapshot.getKey());
                    boolean rewardGiven = snapshot.getValue(Boolean.class);
                    if (!rewardReceived && !rewardGiven && currentProgress >= threshold) {
                        rewardReceived = true;
                        giveExpAndHideButton(context, quest1Button, "quest2", threshold, mAuth.getCurrentUser().getUid());
                        break;
                    }
                }

                // 보상을 받지 못한 경우 버튼을 숨김
                if (!rewardReceived) {
                    quest1Button.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 에러 처리
            }
        });
    }








    // Give EXP and hide button when clicked
    private static void giveExpAndHideButton(Context context, Button rewardButton, String quest, int threshold, String userId) {
        DatabaseReference userDbRef = RTDatabase.getUserDBRef(userId);
        if (userDbRef != null) {
            userDbRef.child("rewards").child(quest).child(String.valueOf(threshold)).setValue(true);

            if (quest.equals("quest2")) {
                int expAmount = threshold / 10;
                ExpManager.updateExp(context, userId, expAmount);

                rewardButton.setVisibility(View.GONE);
            }
        } else {
            // 데이터베이스 참조가 null인 경우
            Toast.makeText(context, "사용자 데이터베이스를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            Log.e("Quest1RewardManager", "사용자 데이터베이스 참조가 null입니다.");
        }
    }




    // Update quest progress UI based on the current progress
    public static void updateQuestProgress(int currentProgress, TextView progressText, ImageView medalImage) {
        progressText.setText("(" + currentProgress + "/100)");

        // Update medal image based on progress
        int medalIndex = getMedalIndex(currentProgress);
        int medalDrawable = getMedalDrawable(medalIndex);
        medalImage.setImageResource(medalDrawable);
    }


    // Quest1RewardManager 클래스 내부에 추가
    public static void updateQuest2RewardButtonVisibility(Context context, final int currentProgress, final Button quest1Button) {
        mDatabaseRef.child("rewards").child("quest2").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean anyRewardReceived = false; // 보상을 받은 경우를 나타내는 플래그

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int threshold = Integer.parseInt(snapshot.getKey());
                    boolean rewardGiven = snapshot.getValue(Boolean.class);

                    // 버튼을 보여줄지 여부 결정
                    if (!rewardGiven && currentProgress >= threshold) {
                        quest1Button.setVisibility(View.VISIBLE);

                        // 버튼 클릭 리스너 추가
                        quest1Button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 버튼을 클릭했을 때의 동작 처리
                                giveExpAndHideButton(context, quest1Button, "quest2", threshold, mAuth.getCurrentUser().getUid());
                            }
                        });

                        return; // 버튼을 보여준 후 메소드 종료
                    }

                    // 보상을 받은 경우 플래그 설정
                    if (rewardGiven) {
                        anyRewardReceived = true;
                    }
                }

                // 퀘스트가 완료된 경우 "완료됨" 버튼 표시
                if (currentProgress > 100) {
                    quest1Button.setText("완료됨"); // 버튼 텍스트를 "완료됨"으로 변경
                    quest1Button.setEnabled(false); // 버튼을 비활성화
                    quest1Button.setVisibility(View.VISIBLE); // 완료됨 버튼 표시
                } else {
                    // 어떤 보상도 받을 수 없는 경우 버튼 숨김
                    if (!anyRewardReceived) {
                        quest1Button.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 에러 처리
            }
        });
    }




    // Get the medal index based on the progress
    private static int getMedalIndex(int progress) {
        for (int i = 0; i < QUEST_THRESHOLDS.length; i++) {
            if (progress < QUEST_THRESHOLDS[i]) {
                return i;
            }
        }
        return QUEST_THRESHOLDS.length;
    }

    // Get the drawable resource for the medal based on the index
    private static int getMedalDrawable(int index) {
        switch (index) {
            case 0:
                return R.drawable.medal1; // 동메달
            case 1:
                return R.drawable.medal2; // 은메달
            case 2:
                return R.drawable.medal3; // 금메달
            case 3:
                return R.drawable.medal4; // 동 트로피
            case 4:
                return R.drawable.medal5; // 은 트로피
            case 5:
                return R.drawable.medal6; // 금 트로피
            default:
                return R.drawable.medal1; // 기본값은 동메달
        }
    }
}