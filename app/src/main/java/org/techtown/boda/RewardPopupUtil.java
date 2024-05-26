package org.techtown.boda;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class RewardPopupUtil {

    public static void showExpPopup(Context context, View parentView, int exp) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_exp, null);
        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView expText = popupView.findViewById(R.id.expText);
        expText.setText("경험치 +" + exp * 10+ " EXP");

        // 애니메이션 효과 설정
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        // 팝업을 중앙에 표시
        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);

        // 2초 후에 팝업 닫기
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();
            }
        }, 2000);
    }
}
