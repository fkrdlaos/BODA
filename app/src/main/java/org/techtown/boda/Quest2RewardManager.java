package org.techtown.boda;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Quest2RewardManager {

    private static final int[] QUEST_THRESHOLDS = {10, 30, 50, 80, 100};

    private static FirebaseAuth mAuth;
    private static DatabaseReference mDatabaseRef;

    public static void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(currentUser.getUid());

            mDatabaseRef.child("rewards").child("quest2").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        for (int threshold : QUEST_THRESHOLDS) {
                            mDatabaseRef.child("rewards").child("quest2").child(String.valueOf(threshold)).setValue(false);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("initFirebase", "Firebase Database Error: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e("initFirebase", "FirebaseUser is null");
        }
    }

    public static void giveQuest2Reward(final Context context, final int currentProgress, final Button quest2Button) {
        mDatabaseRef.child("rewards").child("quest2").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean rewardReceived = false;
                int nextTarget = getNextTarget(currentProgress);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int threshold = Integer.parseInt(snapshot.getKey());
                    boolean rewardGiven = snapshot.getValue(Boolean.class);
                    if (!rewardReceived && !rewardGiven && currentProgress >= threshold) {
                        rewardReceived = true;
                        giveExpAndHideButton(context, quest2Button, "quest2", threshold, mAuth.getCurrentUser().getUid(), nextTarget);
                        break;
                    }
                }

                if (!rewardReceived && currentProgress < 100) {
                    quest2Button.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("giveQuest2Reward", "Firebase Database Error: " + databaseError.getMessage());
                Toast.makeText(context, "데이터베이스에서 정보를 가져오는 도중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void giveExpAndHideButton(Context context, Button rewardButton, String quest, int threshold, String userId, int nextTarget) {
        DatabaseReference userDbRef = RTDatabase.getUserDBRef(userId);
        if (userDbRef != null) {
            userDbRef.child("rewards").child(quest).child(String.valueOf(threshold)).setValue(true);

            int expAmount = threshold / 10;
            ExpManager.updateExp(context, userId, expAmount);

            if (threshold == 100) {
                rewardButton.setText("완료됨");
                rewardButton.setEnabled(false);
            } else {
                rewardButton.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(context, "사용자 데이터베이스를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            Log.e("Quest2RewardManager", "사용자 데이터베이스 참조가 null입니다.");
        }
    }

    public static void updateQuestProgress(int currentProgress, int nextTarget, TextView progressText, ImageView medalImage) {
        progressText.setText("(" + currentProgress + "/" + nextTarget + ")");

        int medalIndex = getMedalIndex(currentProgress);
        int medalDrawable = getMedalDrawable(medalIndex);
        medalImage.setImageResource(medalDrawable);
    }

    public static void updateQuest2RewardButtonVisibility(Context context, final int currentProgress, final Button quest2Button) {
        mDatabaseRef.child("rewards").child("quest2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isButtonVisible = false;
                boolean isCompleted = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int threshold = Integer.parseInt(snapshot.getKey());
                    boolean rewardGiven = snapshot.getValue(Boolean.class);

                    if (threshold == 100 && rewardGiven) {
                        isCompleted = true;
                        break;
                    }

                    if (!rewardGiven && currentProgress >= threshold) {
                        isButtonVisible = true;
                        break;
                    }
                }

                if (isCompleted) {
                    quest2Button.setText("완료됨");
                    quest2Button.setEnabled(false);
                    quest2Button.setVisibility(View.VISIBLE);
                } else {
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("updateQuest2Reward", "Firebase Database Error: " + databaseError.getMessage());
                Toast.makeText(context, "데이터베이스에서 정보를 가져오는 도중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static int getNextTarget(int currentProgress) {
        if (currentProgress < 10) return 10;
        else if (currentProgress < 30) return 30;
        else if (currentProgress < 50) return 50;
        else if (currentProgress < 80) return 80;
        else return 100;
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
