package org.techtown.boda;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.techtown.boda.GetCaption;
import org.techtown.boda.cameraActivity;

import java.io.InputStream;

public class HttpsTask extends AsyncTask<Void, Void, String> {

    private final InputStream inputStream;
    private final Bitmap imageBitmap;
    private final Activity activity;

    // Constructor
    public HttpsTask(Activity activity, InputStream inputStream, Bitmap imageBitmap) {
        this.activity = activity;
        this.inputStream = inputStream;
        this.imageBitmap = imageBitmap;
    }

    @Override
    protected String doInBackground(Void... params) {
        return GetCaption.sendCaptionRequest(inputStream);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            // Pass the result and imageBitmap to the cameraActivity using Intent
            Intent intent = new Intent(activity, cameraActivity.class);
            intent.putExtra("result", result);
            intent.putExtra("imageBitmap", imageBitmap);
            activity.startActivity(intent);
        }
    }
}