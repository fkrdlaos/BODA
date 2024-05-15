package org.techtown.boda;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
public class ViewPagerAdapter extends FragmentStateAdapter{
    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }
    @Override
    public Fragment createFragment(int position) {
        // position에 따라 다른 프래그먼트 반환
        switch (position) {
            case 0:
                return new FullFragment(); // 모든 단어
            case 1:
                return new PictureFragment(); // 그림 단어
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 탭의 수
    }
}
