package org.techtown.boda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class NoticeDialog {

    protected Context context;
    protected String title;
    protected String leftMessage;
    protected String centerMessage;

    protected String rightMessage;
    protected Integer leftImageResId;
    protected Integer centerImageResId;
    protected Integer rightImageResId;

    private ImageView centerImageView;

    protected NoticeDialog(){
    }
    protected NoticeDialog(Builder builder) {
        this.context = builder.context;

        this.title = builder.title;
        this.leftMessage = builder.leftMessage;
        this.centerMessage = builder.centerMessage;

        this.rightMessage = builder.rightMessage;
        this.leftImageResId = builder.leftImageResId;
        this.centerImageResId = builder.centerImageResId;
        this.rightImageResId = builder.rightImageResId;
    }

    public static class Builder {
        private final Context context;
        private String title;
        private String leftMessage;
        private String centerMessage;

        private String rightMessage;
        private Integer leftImageResId;
        private Integer centerImageResId;
        private Integer rightImageResId;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setLeftMessage(String leftMessage) {
            this.leftMessage = leftMessage;
            return this;
        }
        public Builder setCenterMessage(String centerMessage) {
            this.centerMessage = centerMessage;
            return this;
        }

        public Builder setRightMessage(String rightMessage) {
            this.rightMessage = rightMessage;
            return this;
        }

        public Builder setLeftImage(int leftImageResId) {
            this.leftImageResId = leftImageResId;
            return this;
        }

        public Builder setCenterImage(int centerImageResId) {
            this.centerImageResId = centerImageResId;
            return this;
        }

        public Builder setRightImage(int rightImageResId) {
            this.rightImageResId = rightImageResId;
            return this;
        }


        public NoticeDialog build() {
            return new NoticeDialog(this);
        }
    }

    public void showDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_notice, null);
        dialogBuilder.setView(dialogView);

        // Set the title and messages
        TextView titleView = dialogView.findViewById(R.id.titleView);
        TextView leftMessageView = dialogView.findViewById(R.id.leftMessageView);
        TextView centerMessageView = dialogView.findViewById(R.id.centerMessageView);
        TextView rightMessageView = dialogView.findViewById(R.id.rightMessageView);
        ImageView leftImageView = dialogView.findViewById(R.id.leftImageView);
        centerImageView = dialogView.findViewById(R.id.centerImageView);
        ImageView rightImageView = dialogView.findViewById(R.id.rightImageView);
        Button dialogButton = dialogView.findViewById(R.id.dialogButton);

        titleView.setText(title);

        if (leftMessage != null) {
            leftMessageView.setText(leftMessage);
            leftMessageView.setVisibility(View.VISIBLE);
        }
        if (centerMessage != null) {
            centerMessageView.setText(centerMessage);
            centerMessageView.setVisibility(View.VISIBLE);
        }

        if (rightMessage != null) {
            rightMessageView.setText(rightMessage);
            rightMessageView.setVisibility(View.VISIBLE);
        }

        if (leftImageResId != null) {
            leftImageView.setImageResource(leftImageResId);
            leftImageView.setVisibility(View.VISIBLE);
        }

        if (centerImageResId != null) {
            centerImageView.setImageResource(centerImageResId);
            centerImageView.setVisibility(View.VISIBLE);
        }

        if (rightImageResId != null) {
            rightImageView.setImageResource(rightImageResId);
            rightImageView.setVisibility(View.VISIBLE);
        }

        AlertDialog dialog = dialogBuilder.create();

        // Set the button click listener
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public ImageView getCenterImageView() {
        return centerImageView;
    }
}
