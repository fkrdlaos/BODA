package org.techtown.boda;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

public class HttpsTask extends AsyncTask<Void, Void, String> {
    private final String imagePath;
    private final Activity activity;

    private volatile boolean isCancelled = false; // 작업 중단 여부를 저장하는 변수
    private volatile boolean isCompleted = false; // 작업 완료 여부를 저장하는 변수

    public HttpsTask(Activity activity, String imagePath) {
        this.activity = activity;
        this.imagePath = imagePath;
    }

    // 작업을 중지하는 메서드
    public void cancelTask() {
        isCancelled = true;
        cancel(true);
    }

    // 작업 완료 여부를 확인하는 메서드
    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (isCancelled) return null; // 작업 중단되었을 경우 바로 리턴

        try {
            // 이미지 파일의 경로를 사용하여 캡션 요청을 보냄
            String result = GetCaption.sendCaptionRequest(imagePath);
            if (isCancelled) return null; // 작업 중단된 후 결과를 무시
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "네트워크 오류: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        isCompleted = true; // 작업 완료 상태로 설정
        // 결과를 처리하는 부분
        if (result != null && !isCancelled) {
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
