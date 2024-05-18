package org.techtown.boda;

import android.widget.ImageView;


public class ProfileManager {
    public static void updateProfileByLevel(int level, ImageView profileImageView) {

        if (level <= 10) {
            // 레벨이 20 이하인 경우
            profileImageView.setImageResource(R.drawable.p);
        } else if (level <= 30) {
            // 레벨이 20 초과 50 이하인 경우
            profileImageView.setImageResource(R.drawable.p2);
        } else {
            // 그 외의 경우
            profileImageView.setImageResource(R.drawable.p3);
        }
    }
}
