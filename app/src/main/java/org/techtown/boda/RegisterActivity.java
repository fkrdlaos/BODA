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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; // 파이어베이스 인증
    private DatabaseReference mDatabaseRef; // 실시간 데이터베이스
    private EditText mEtEmail, mEtPwd, mEtNickname, mEtConfirmPwd; // 회원가입 입력필드
    private Button mBtnRegister, mBtnCheck; // 회원가입 버튼
    private boolean isEmailValid = false;

    private SharedPreferences sharedPreferences;

    private Button mBtnBack; // 뒤로가기 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("BODA");

        mEtEmail = findViewById(R.id.et_email);
        mEtPwd = findViewById(R.id.et_pwd);
        mEtNickname = findViewById(R.id.et_nickname);
        mEtConfirmPwd = findViewById(R.id.et_confirm_pwd);
        mBtnRegister = findViewById(R.id.btn_register);
        mBtnCheck = findViewById(R.id.btn_check);

        mBtnBack = findViewById(R.id.button); // 뒤로가기 버튼 찾기

        sharedPreferences = getSharedPreferences("MY_PREFS", MODE_PRIVATE); // SharedPreferences 초기화

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로 가기 버튼을 눌렀을 때 현재 액티비티를 종료하여 이전 화면으로 이동
                finish();
            }
        });

        mBtnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEtEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 중복 체크
                mDatabaseRef.child("UserAccount").orderByChild("emailId").equalTo(email)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // 이미 사용 중인 이메일이 있음
                                    Toast.makeText(RegisterActivity.this, "중복된 이메일입니다. 다른 이메일을 사용해주세요.", Toast.LENGTH_SHORT).show();
                                    isEmailValid = false;
                                } else {
                                    // 사용 가능한 이메일
                                    Toast.makeText(RegisterActivity.this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                                    isEmailValid = true;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(RegisterActivity.this, "데이터베이스 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEtEmail.getText().toString().trim();
                final String password = mEtPwd.getText().toString().trim();
                final String nickname = mEtNickname.getText().toString().trim();
                String confirmPassword = mEtConfirmPwd.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || nickname.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "이메일, 비밀번호, 닉네임, 비밀번호 확인을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 일치 여부 확인
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("nickname", nickname); // SharedPreferences에 닉네임 저장
                editor.apply();

                // 중복 체크 결과가 나온 후 회원가입을 시도
                if (isEmailValid) {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                                UserAccount account = new UserAccount();
                                account.setIdToken(firebaseUser.getUid());
                                account.setEmailId(email);
                                account.setPassword(password);
                                account.setNickname(nickname);

                                // setValue : datavase에 insert(삽입) 행위
                                mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);

                                Toast.makeText(RegisterActivity.this, "회원가입에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish(); // 현재 액티비티 파괴

                            } else {
                                Toast.makeText(RegisterActivity.this, "회원가입에 실패하셨습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "이메일 중복을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}