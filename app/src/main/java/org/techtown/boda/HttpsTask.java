package org.techtown.boda;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.InputStream;

public class HttpsTask extends AsyncTask<Void, Void, String> {

    private final InputStream inputStream;

    // Constructor
    public HttpsTask(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    protected String doInBackground(Void... params) {
        String result; // 요청 결과를 저장할 변수.

//        HttpsGetCaption requestHttpsURLConnection = new HttpsGetCaption(this.imageBitmap);
//        result = requestHttpsURLConnection.sendPostRequest();
        result = GetCaption.sendCaptionRequest(inputStream);

        try {
            if (result != null) {
                System.out.println("[doInBackground] output : " + result);
            } else {
                System.out.println("[doInBackground] output is null state.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        try {
            if (result != null) {
                System.out.println("[onPostExecute] output is ."+result);
            } else {
                System.out.println("[onPostExecute] output is null state.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}