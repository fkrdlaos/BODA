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

public class Quest1RewardManager {

    private static final int[] QUEST_THRESHOLDS = {10, 30, 50, 80, 100};

    private static FirebaseAuth mAuth;
    private static DatabaseReference mDatabaseRef;

    public static void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(mAuth.getCurrentUser().getUid());

        mDatabaseRef.child("rewards").child("quest1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    for (int threshold : QUEST_THRESHOLDS) {
                        mDatabaseRef.child("rewards").child("quest1").child(String.valueOf(threshold)).setValue(false);
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

    public static void giveQuest1Reward(final Context context, final int currentProgress, final Button quest1Button) {
        mDatabaseRef.child("rewards").child("quest1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean rewardReceived = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int threshold = Integer.parseInt(snapshot.getKey());
                    boolean rewardGiven = snapshot.getValue(Boolean.class);
                    if (!rewardReceived && !rewardGiven && currentProgress >= threshold) {
                        rewardReceived = true;
                        giveExpAndHideButton(context, quest1Button, "quest1", threshold, mAuth.getCurrentUser().getUid());
                        break;
                    }
                }

                if (!rewardReceived) {
                    quest1Button.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 에러 처리
                Log.e("giveQuest1Reward", "Firebase Database Error: " + databaseError.getMessage());
                Toast.makeText(context, "데이터베이스에서 정보를 가져오는 도중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void giveExpAndHideButton(Context context, Button rewardButton, String quest, int threshold, String userId) {
        DatabaseReference userDbRef = RTDatabase.getUserDBRef(userId);
        if (userDbRef != null) {
            userDbRef.child("rewards").child(quest).child(String.valueOf(threshold)).setValue(true);

            if (quest.equals("quest1")) {
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




    public static void updateQuestProgress(int currentProgress, TextView progressText, ImageView medalImage) {
        progressText.setText("(" + currentProgress + "/100)");

        int medalIndex = getMedalIndex(currentProgress);
        int medalDrawable = getMedalDrawable(medalIndex);
        medalImage.setImageResource(medalDrawable);
    }

    public static void updateQuest1RewardButtonVisibility(Context context, final int currentProgress, final Button quest1Button) {
        mDatabaseRef.child("rewards").child("quest1").addValueEventListener(new ValueEventListener() {
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
                    quest1Button.setText("완료됨");
                    quest1Button.setEnabled(false);
                    isButtonVisible = true;
                }

                quest1Button.setVisibility(isButtonVisible ? View.VISIBLE : View.GONE);

                if (isButtonVisible) {
                    quest1Button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            giveQuest1Reward(context, currentProgress, quest1Button);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 에러 처리
                Log.e("updateQuest1Reward", "Firebase Database Error: " + databaseError.getMessage());
                Toast.makeText(context, "데이터베이스에서 정보를 가져오는 도중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static int getMedalIndex(int progress) {
        for (int i = 0; i < QUEST_THRESHOLDS.length; i++) {
            if (progress < QUEST_THRESHOLDS[i]) {
                return i;
            }
        }
        return QUEST_THRESHOLDS.length;
    }

    private static int getMedalDrawable(int index) {
        switch (index) {
            case 0:
                return R.drawable.medal1;
            case 1:
                return R.drawable.medal2;
            case 2:
                return R.drawable.medal3;
            case 3:
                return R.drawable.medal4;
            case 4:
                return R.drawable.medal5;
            case 5:
                return R.drawable.medal6;
            default:
                return R.drawable.medal1;
        }
    }
}