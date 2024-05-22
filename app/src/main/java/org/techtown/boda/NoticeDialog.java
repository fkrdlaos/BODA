package org.techtown.boda;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import android.view.View.OnClickListener;
public class NoticeDialog {
    private final Context context;
    private String title;
    private String message;
    private Drawable image;
    private View.OnClickListener positiveButtonListener;

    private NoticeDialog(Builder builder) {
        this.context = builder.context;
        this.title = builder.title;
        this.message = builder.message;
        this.image = builder.image;
        this.positiveButtonListener = builder.positiveButtonListener;
    }

    public void show() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        // Inflate custom view
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_notice, null);
        dialogBuilder.setView(dialogView);

        // Set custom view content
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        ImageView imageView = dialogView.findViewById(R.id.dialog_image);
        Button positiveButton = dialogView.findViewById(R.id.dialog_button);

        titleView.setText(title);
        messageView.setText(message);

        if (image != null) {
            imageView.setImageDrawable(image);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }


        if (positiveButtonListener != null) {
            positiveButton.setOnClickListener(positiveButtonListener);
        } else {
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Dismiss the dialog
                }
            });
        }

        AlertDialog dialog = dialogBuilder.create();
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (positiveButtonListener != null) {
                    positiveButtonListener.onClick(v);
                }
            }
        });

        dialog.show();
    }
    public static class Builder {
        private final Context context;
        private String title;
        private String message;
        private Drawable image;
        private View.OnClickListener positiveButtonListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setImage(Drawable image) {
            this.image = image;
            return this;
        }

        public Builder setPositiveButtonListener(View.OnClickListener listener) {
            this.positiveButtonListener = listener;
            return this;
        }


        public NoticeDialog build() {
            return new NoticeDialog(this);
        }

    }

}
