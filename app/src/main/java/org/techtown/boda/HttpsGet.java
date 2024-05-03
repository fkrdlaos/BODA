package org.techtown.boda;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpsGet {
    public void sendGetRequest() {
        try {
            URL url = new URL("https://dabo-captioner-4amainj2za-du.a.run.app");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.i("Http GET", response.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Http GET", "네트워크 연결에 실패했습니다.");
        }
    }

}