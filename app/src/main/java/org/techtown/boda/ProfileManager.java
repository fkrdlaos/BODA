package org.techtown.boda;

import android.widget.ImageView;


public class ProfileManager {
    public static void updateProfileByLevel(int level, ImageView profileImageView) {

        if (level < 10) {
            // 레벨이 10 미만인 경우
            profileImageView.setImageResource(R.drawable.p1);
        } else if (level < 20) {
            // 레벨이 10 이하 20 미만인 경우
            profileImageView.setImageResource(R.drawable.p2);
        } else if (level < 30) {
            // 레벨이 20 이하 30 미만인 경우
            profileImageView.setImageResource(R.drawable.p3);
        } else if (level < 40) {
            // 레벨이 30 이하 40 미만인 경우
            profileImageView.setImageResource(R.drawable.p4);
        } else {
            // 그 외의 경우
            profileImageView.setImageResource(R.drawable.p5);
        }
    }
}
