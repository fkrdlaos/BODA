package org.techtown.boda;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.techtown.boda.LoginActivity;
import org.techtown.boda.MainActivity;

public class SettingsActivity extends AppCompatActivity {

    private Button buttonNickname, buttonLogout;
    private SharedPreferences sharedPreferences;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Firebase 인증 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        // 파이어베이스 실시간 데이터베이스의 사용자 경로 참조
        userRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(mAuth.getCurrentUser().getUid());

        buttonNickname = findViewById(R.id.buttonNickname);
        buttonLogout = findViewById(R.id.buttonLogout);


        sharedPreferences = getSharedPreferences("MY_PREFS", MODE_PRIVATE);

        String nickname = sharedPreferences.getString("nickname", "");

        buttonNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeNicknameDialog(nickname);
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out from Firebase Auth
                mAuth.signOut();

                // Move back to LoginActivity
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이전 페이지로 이동
                finish();
            }
        });
    }

    // 팝업창을 띄우는 함수
    private void showChangeNicknameDialog(String nickname) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_change_nickname, null);
        builder.setView(dialogView);

        EditText editTextNewNickname = dialogView.findViewById(R.id.editTextNewNickname);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
        TextView textViewCharCount =dialogView.findViewById(R.id.textViewCharCount);
        // 기존 닉네임 설정
        editTextNewNickname.setText(nickname);

        // 글자 수 세기
        editTextNewNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No need to implement
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 글자 수 표시
                textViewCharCount.setText(String.valueOf(s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No need to implement
            }
        });

        // "취소" 버튼 클릭 시
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 팝업창만 닫기
                alertDialog.dismiss();
            }
        });

        // "확인" 버튼 클릭 시
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 새로운 닉네임 가져오기
                String newNickname = editTextNewNickname.getText().toString().trim();
                if (!newNickname.isEmpty() && newNickname.length() <= 20) {
                    // 팝업창 닫기
                    alertDialog.dismiss();
                    // 닉네임 변경 작업 수행
                    changeNickname(newNickname);
                } else if (newNickname.isEmpty()) {
                    // 닉네임이 비어있는 경우 사용자에게 메시지 표시
                    Toast.makeText(SettingsActivity.this, "새로운 닉네임을 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // 최대 길이 초과 시 메시지 표시
                    Toast.makeText(SettingsActivity.this, "닉네임은 20자 이하여야 합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // AlertDialog 생성 및 표시
        alertDialog = builder.create();
        alertDialog.show();
    }

    // 닉네임 변경 메서드
    private void changeNickname(String newNickname) {
        // SharedPreferences에 저장
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nickname", newNickname);
        editor.apply();

        // Firebase 실시간 데이터베이스에 업데이트
        userRef.child("nickname").setValue(newNickname, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    // 업데이트 실패 처리
                    Toast.makeText(SettingsActivity.this, "닉네임 변경에 실패 했습니다.: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    // 업데이트 성공 처리
                    Toast.makeText(SettingsActivity.this, "닉네임 변경에 성공 했습니다.", Toast.LENGTH_SHORT).show();
                    // MainActivity로 이동
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}