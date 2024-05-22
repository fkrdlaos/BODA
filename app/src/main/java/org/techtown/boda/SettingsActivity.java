package org.techtown.boda;

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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    private Button buttonNickname, buttonLogout, buttonReset;
    private SharedPreferences sharedPreferences;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private AlertDialog alertDialog;
    private GoogleSignInClient mGoogleSignInClient;

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
        buttonReset = findViewById(R.id.reset);

        sharedPreferences = getSharedPreferences("MY_PREFS", MODE_PRIVATE);
        String nickname = sharedPreferences.getString("nickname", "");

        // GoogleSignInOptions 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeNicknameDialog(nickname);
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountDialog();
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

    private void logout() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // SharedPreferences 초기화
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // 구글 로그인 여부 확인
            boolean isGoogleLogin = sharedPreferences.getBoolean("isGoogleLogin", false);
            if (isGoogleLogin) {
                // 구글 로그아웃
                mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase Auth에서 로그아웃
                        mAuth.signOut();
                        // LoginActivity로 이동
                        Toast.makeText(SettingsActivity.this, "구글 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.putExtra("signOut", true); // 로그인 화면에서 다시 로그인할 수 있도록 플래그 설정
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SettingsActivity.this, "구글 로그아웃 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // 일반 로그인 로그아웃
                mAuth.signOut();

                Toast.makeText(SettingsActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                // LoginActivity로 이동
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.putExtra("signOut", true); // 로그인 화면에서 다시 로그인할 수 있도록 플래그 설정
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(SettingsActivity.this, "이미 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_del_account, null);
        builder.setView(dialogView);

        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.delete().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("auto_login", false);
                            editor.apply();
                            // LoginActivity로 이동
                            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                            intent.putExtra("signOut", true); // 로그인 화면에서 다시 로그인할 수 있도록 플래그 설정
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SettingsActivity.this, "계정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("auto_login", false);
                            editor.apply();
                            // LoginActivity로 이동
                            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                            intent.putExtra("signOut", true); // 로그인 화면에서 다시 로그인할 수 있도록 플래그 설정
                            startActivity(intent);
                            finish();

                        }
                    });
                } else {
                    Toast.makeText(SettingsActivity.this, "데이터베이스 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showChangeNicknameDialog(String nickname) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_change_nickname, null);
        builder.setView(dialogView);

        EditText editTextNewNickname = dialogView.findViewById(R.id.editTextNewNickname);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
        TextView textViewCharCount = dialogView.findViewById(R.id.textViewCharCount);

        editTextNewNickname.setText(nickname);

        editTextNewNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewCharCount.setText(String.valueOf(s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newNickname = editTextNewNickname.getText().toString().trim();
                if (!newNickname.isEmpty() && isValidNickname(newNickname)) {
                    alertDialog.dismiss();
                    changeNickname(newNickname);
                } else if (newNickname.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "새로운 닉네임을 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "닉네임은 한글 8자, 영어 15자, 혼합 8자 이하로 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean isValidNickname(String nickname) {
        int length = nickname.length();
        boolean isEnglish = nickname.matches("^[a-zA-Z]*$");

        if (isEnglish) {
            return length <= 15;
        } else {
            return length <= 8;
        }
    }

    private void changeNickname(String newNickname) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nickname", newNickname);
        editor.apply();

        userRef.child("nickname").setValue(newNickname, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    Toast.makeText(SettingsActivity.this, "닉네임 변경에 실패 했습니다.: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "닉네임 변경에 성공 했습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public void onBackPressed() {
        super.onBackPressed();
        // 뒤로가기 버튼을 눌렀을 때 MainActivity로 이동
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
