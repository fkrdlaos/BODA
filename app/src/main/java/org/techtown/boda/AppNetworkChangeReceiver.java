package org.techtown.boda;

import android.content.Context;
import android.content.Intent;

public class AppNetworkChangeReceiver extends NetworkChangeReceiver {
    @Override
    protected void onNetworkUnavailable(Context context) {
        // 네트워크가 끊겼을 때 처리할 내용을 여기에 작성
        Intent logoutIntent = new Intent(context, LoginActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(logoutIntent);
    }
}
