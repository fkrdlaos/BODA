package org.techtown.boda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class RewardPopupUtil {

    public static void showExpPopup(final Context context, View parentView, final int exp) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_exp, null);
        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView expText = popupView.findViewById(R.id.expText);
        expText.setText("경험치 +" + exp *10 + " EXP");

        // 애니메이션 효과 설정
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        // 팝업을 중앙에 표시
        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);

        // 팝업 닫기 애니메이션 추가
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(1000); // 애니메이션 지속 시간 (1초)
        popupView.startAnimation(fadeOut);

        // 팝업이 완전히 사라진 후에 화면을 전환하기 위해 Handler 사용
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();

                // 화면 전환 애니메이션 적용
                context.startActivity(new Intent(context, MainActivity.class));
                // 메인 액티비티로 이동할 때 페이드 인 애니메이션 적용
                ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 500); // 1초 뒤에 팝업이 사라지고 화면 전환
    }
}
