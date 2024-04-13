package org.techtown.boda;

import android.graphics.Bitmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetCaption {
    public static InputStream transformBMtoIS(Bitmap bitmap){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        return new ByteArrayInputStream(bitmapdata);
    }
    public static String sendCaptionRequest(InputStream inputStream){
        try {

            // InputStream에서 byte array로 변환
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            byte[] imageData = byteArrayOutputStream.toByteArray();

            // 멀티파트 바디 생성
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.bmp",
                            RequestBody.create(MediaType.parse("image/bmp"), imageData))
                    .build();

            // 요청 객체 생성
            Request request = new Request.Builder()
                    .url("https://dabo-captioner-4amainj2za-du.a.run.app/predict")
                    .post(requestBody)
                    .build();

            // 요청 실행 및 응답 처리
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.MINUTES) // connect timeout
                    .writeTimeout(60, TimeUnit.MINUTES) // write timeout
                    .readTimeout(60, TimeUnit.MINUTES) // read timeout
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        System.out.println("Server Response: " + response.body().string());

                    } else {
                        System.out.println("Request failed: " + response.message());
                    }
                }
            });

            return "finish well";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return "finish not well";

    }
}