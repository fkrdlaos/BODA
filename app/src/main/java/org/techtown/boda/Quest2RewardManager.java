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
                // 에러 처리
                Log.e("initFirebase", "Firebase Database Error: " + databaseError.getMessage());

            }
        });
    }



    // Give reward based on the current progress for quest1Button
    public static void giveQuest2Reward(final Context context, final int currentProgress, final Button quest2Button) {
        mDatabaseRef.child("rewards").child("quest2").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean rewardReceived = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int threshold = Integer.parseInt(snapshot.getKey());
                    boolean rewardGiven = snapshot.getValue(Boolean.class);
                    if (!rewardReceived && !rewardGiven && currentProgress >= threshold) {
                        rewardReceived = true;
                        giveExpAndHideButton(context, quest2Button, "quest2", threshold, mAuth.getCurrentUser().getUid());
                        break;
                    }
                }

                if (!rewardReceived) {
                    quest2Button.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 에러 처리
                Log.e("giveQuest2Reward", "Firebase Database Error: " + databaseError.getMessage());
                Toast.makeText(context, "데이터베이스에서 정보를 가져오는 도중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
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
            Log.e("Quest2RewardManager", "사용자 데이터베이스 참조가 null입니다.");
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


    // Quest2RewardManager 클래스 내부에 추가
    public static void updateQuest2RewardButtonVisibility(Context context, final int currentProgress, final Button quest2Button) {
        mDatabaseRef.child("rewards").child("quest2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean anyRewardReceived = false;
                boolean isButtonVisible = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int threshold = Integer.parseInt(snapshot.getKey());
                    boolean rewardGiven = snapshot.getValue(Boolean.class);

                    if (!rewardGiven && currentProgress >= threshold) {
                        isButtonVisible = true;
                        break;
                    }

                    if (rewardGiven) {
                        anyRewardReceived = true;
                    }
                }

                if (currentProgress > 100) {
                    quest2Button.setText("완료됨");
                    quest2Button.setEnabled(false);
                    isButtonVisible = true;
                }

                quest2Button.setVisibility(isButtonVisible ? View.VISIBLE : View.GONE);

                if (isButtonVisible) {
                    quest2Button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            giveQuest2Reward(context, currentProgress, quest2Button);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 에러 처리
                Log.e("updateQuest2Reward", "Firebase Database Error: " + databaseError.getMessage());
                Toast.makeText(context, "데이터베이스에서 정보를 가져오는 도중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
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