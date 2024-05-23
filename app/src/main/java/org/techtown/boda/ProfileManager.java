package org.techtown.boda;

import android.widget.ImageView;


public class ProfileManager {
    public static void updateProfileByLevel(int level, ImageView profileImageView) {

        if (level < 10) {
            // 레벨이 10 미만인 경우
            profileImageView.setImageResource(R.drawable.gen1_profile);
        } else if (level < 20) {
            // 레벨이 10 이하 20 미만인 경우
            profileImageView.setImageResource(R.drawable.gen2_profile);
        } else if (level < 30) {
            // 레벨이 20 이하 30 미만인 경우
            profileImageView.setImageResource(R.drawable.gen3_profile);
        } else if (level < 40) {
            // 레벨이 30 이하 40 미만인 경우
            profileImageView.setImageResource(R.drawable.gen4_profile);
        } else {
            // 그 외의 경우
            profileImageView.setImageResource(R.drawable.gen5_profile);
        }
    }
}
