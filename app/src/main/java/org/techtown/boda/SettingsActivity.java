package org.techtown.boda;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextNickname;
    private Button buttonSave, buttonLogout;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Firebase 인증 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        // 파이어베이스 실시간 데이터베이스의 사용자 경로 참조
        userRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(mAuth.getCurrentUser().getUid());


        editTextNickname = findViewById(R.id.editTextNickname);
        buttonSave = findViewById(R.id.buttonSave);
        buttonLogout = findViewById(R.id.buttonLogout);

        sharedPreferences = getSharedPreferences("MY_PREFS", MODE_PRIVATE);

        String nickname = sharedPreferences.getString("nickname", "");
        editTextNickname.setText(nickname);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 새로운 닉네임 가져오기
                String newNickname = editTextNickname.getText().toString().trim();
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
}
