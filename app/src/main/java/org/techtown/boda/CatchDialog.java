package org.techtown.boda;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class CatchDialog extends NoticeDialog{
    private DialogInterface.OnClickListener positiveButtonListener;

    protected CatchDialog(CatchBuilder builder) {
        super(builder);
        this.positiveButtonListener = builder.positiveButtonListener;
    }

    @Override
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
        ImageView centerImageView = dialogView.findViewById(R.id.centerImageView);
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
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }
        });

        dialog.show();
    }

    public static class CatchBuilder extends Builder {
        private DialogInterface.OnClickListener positiveButtonListener;

        public CatchBuilder(Context context) {
            super(context);
        }

        public CatchBuilder setPositiveButtonListener(DialogInterface.OnClickListener listener) {
            this.positiveButtonListener = listener;
            return this;
        }

        @Override
        public CatchDialog build() {
            return new CatchDialog(this);
        }
    }
}