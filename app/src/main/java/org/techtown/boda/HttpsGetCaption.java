package org.techtown.boda;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpsPost{

    private final Bitmap imageBitmap;

    public HttpsPost() {
        this.imageBitmap = BitmapFactory.decodeFile("C:\\Users\\BriGHt_sun\\Desktop\\24-1\\capstone\\proj\\app\\BODA\\app\\src\\main\\java\\org\\techtown\\boda\\play_football.bmp");
    }

    public void sendPostRequest() {
        try {
            System.out.println("sendPostRequest");

            String encodedImage = ImageUtil.encodeToBase64(this.imageBitmap, Bitmap.CompressFormat.JPEG, 100);
            String postData = "image=" + encodedImage;

            URL url = new URL("https://dabo-captioner-4amainj2za-du.a.run.app/prediction");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(postData);
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
