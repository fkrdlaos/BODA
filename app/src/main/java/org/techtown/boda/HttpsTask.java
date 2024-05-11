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
    protected String doInBackground(Void... params) {
        try {
            // Extract caption from image path
            return GetCaption.sendCaptionRequest(imagePath);
        } catch (Exception e) {
            e.printStackTrace();
            // 예외가 발생했을 때 반환할 기본값 또는 오류 메시지를 반환합니다.
            return "예외 발생: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            // Pass the result to the cameraActivity using Intent
            activity.startActivity(new Intent(activity, CameraActivity.class)
                    .putExtra("result", result)
                    .putExtra("imagePath", imagePath));

        }
    }
}