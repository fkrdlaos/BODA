package org.techtown.boda;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;

import java.util.Stack;

public class GuidePopupManager {

    private Activity activity;
    private Stack<Dialog> dialogStack;

    public GuidePopupManager(Activity activity) {
        this.activity = activity;
        this.dialogStack = new Stack<>();
    }

    public void showPopupGuideCamera() {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.popup_guide_camera);
        dialog.setCancelable(false);

        Button nextButton = dialog.findViewById(R.id.next6);
        Button backButton = dialog.findViewById(R.id.back1);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupGuideDictionary();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAllDialogs();
            }
        });

        dialog.show();
        dialogStack.push(dialog);
    }

    private void showPopupGuideDictionary() {
        closeCurrentDialog();
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.popup_guide_dictionary);
        dialog.setCancelable(false);

        Button nextButton = dialog.findViewById(R.id.next7);
        Button backButton = dialog.findViewById(R.id.back2);
        Button beforeButton = dialog.findViewById(R.id.before5);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupGuideStudy();
            }
        });

        beforeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupGuideCamera();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAllDialogs();
            }
        });

        dialog.show();
        dialogStack.push(dialog);
    }

    private void showPopupGuideStudy() {
        closeCurrentDialog();
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.popup_guide_study);
        dialog.setCancelable(false);

        Button nextButton = dialog.findViewById(R.id.next8);
        Button backButton = dialog.findViewById(R.id.back3);
        Button beforeButton = dialog.findViewById(R.id.before6);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupGuideQuest();
            }
        });

        beforeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupGuideDictionary();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAllDialogs();
            }
        });

        dialog.show();
        dialogStack.push(dialog);
    }

    private void showPopupGuideQuest() {
        closeCurrentDialog();
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.popup_guide_quest);
        dialog.setCancelable(false);

        Button nextButton = dialog.findViewById(R.id.next9);
        Button backButton = dialog.findViewById(R.id.back4);
        Button beforeButton = dialog.findViewById(R.id.before7);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupGuideEvolution();
            }
        });

        beforeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupGuideStudy();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAllDialogs();
            }
        });

        dialog.show();
        dialogStack.push(dialog);
    }

    private void showPopupGuideEvolution() {
        closeCurrentDialog();
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.popup_guide_evolution);
        dialog.setCancelable(false);

        Button nextButton = dialog.findViewById(R.id.next10);
        Button beforeButton = dialog.findViewById(R.id.before8);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAllDialogs();
            }
        });

        beforeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupGuideQuest();
            }
        });

        dialog.show();
        dialogStack.push(dialog);
    }

    private void closeCurrentDialog() {
        if (!dialogStack.isEmpty()) {
            Dialog currentDialog = dialogStack.pop();
            currentDialog.dismiss();
        }
    }

    private void closeAllDialogs() {
        while (!dialogStack.isEmpty()) {
            Dialog dialog = dialogStack.pop();
            dialog.dismiss();
        }
    }
}
