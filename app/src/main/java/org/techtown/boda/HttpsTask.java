package org.techtown.boda;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

public class HttpsTask extends AsyncTask<Void, Void, String> {
    private final String imagePath;
    private final Activity activity;

    public HttpsTask(Activity activity, String imagePath) {
        this.activity = activity;
        this.imagePath = imagePath;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            // 이미지 파일의 경로를 사용하여 캡션 요청을 보냄
            return GetCaption.sendCaptionRequest(imagePath);
        } catch (Exception e) {
            e.printStackTrace();
            return "네트워크 오류: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // 결과를 처리하는 부분
        if (result != null) {
            // 네트워크 연결이 끊긴 경우 BaseActivity에 정의된 팝업창을 표시
            if (result.contains("네트워크 오류")) {
                if (activity instanceof BaseActivity) {
                    ((BaseActivity) activity).showNetworkDialog();
                }
            } else {
                // 네트워크 연결이 유지된 경우 결과를 처리
                // CameraActivity로 결과를 전달하는 부분
                activity.startActivity(new Intent(activity, CameraActivity.class)
                        .putExtra("result", result)
                        .putExtra("imagePath", imagePath));
            }
        }
    }
}