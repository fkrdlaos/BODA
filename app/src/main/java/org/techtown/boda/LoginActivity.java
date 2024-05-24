package org.techtown.boda;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mFirebaseAuth; // 파이어베이스 인증
    private DatabaseReference mDatabaseRef; // 실시간 데이터베이스
    private EditText mEtEmail, mEtPwd; // 로그인 입력 필드
    private SignInButton btnGoogle;
    private FirebaseAuth auth;
    private GoogleApiClient googleApiClient;
    private static final int REO_SIGN_GOOGLE = 100;
    private SharedPreferences sharedPreferences;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // GoogleSignInOptions 및 GoogleSignInClient 초기화
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        sharedPreferences = getSharedPreferences("MY_PREFS", MODE_PRIVATE);
        boolean autoLogin = sharedPreferences.getBoolean("auto_login", false);

        if (autoLogin) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // GoogleApiClient 초기화
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        auth = FirebaseAuth.getInstance();
        btnGoogle = findViewById(R.id.btn_google);
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(LoginActivity.this, "네트워크가 연결이 되어 있지 않습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                // GoogleSignInClient를 사용하여 로그인 인텐트 가져오기
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, REO_SIGN_GOOGLE);
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("BODA");
        mEtEmail = findViewById(R.id.et_email);
        mEtPwd = findViewById(R.id.et_pwd);

        Button btnLogin = findViewById(R.id.btn_login);
        ;
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(LoginActivity.this, "네트워크가 연결이 되어 있지 않습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();
                mFirebaseAuth.signInWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공 시 SharedPreferences에 닉네임 저장
                            mDatabaseRef.child("UserAccount").orderByChild("emailId").equalTo(strEmail)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                UserAccount userAccount = snapshot.getValue(UserAccount.class);
                                                String nickname = userAccount.getNickname();
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean("auto_login", true);
                                                editor.putString("nickname", nickname);
                                                editor.apply();
                                                Toast.makeText(LoginActivity.this, "닉네임: " + nickname, Toast.LENGTH_SHORT).show();

                                                // 만약 exp 및 lv 필드가 존재하지 않으면 기본값으로 설정
                                                if (!snapshot.child("exp").exists()) {
                                                    snapshot.getRef().child("exp").setValue(0);
                                                }
                                                if (!snapshot.child("lv").exists()) {
                                                    snapshot.getRef().child("lv").setValue(1);
                                                }

                                                // 미션이 없으면 미션 정보를 추가
                                                if (!snapshot.child("mission").exists()) {
                                                    snapshot.getRef().child("mission").child("challenges").setValue(0);
                                                    snapshot.getRef().child("mission").child("words").setValue(0);
                                                }

                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(LoginActivity.this, "데이터베이스 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(LoginActivity.this, "로그인 실패..!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        Button btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 이전 액티비티에서 전달된 인텐트 확인
        Intent intent = getIntent();
        boolean signOut = intent.getBooleanExtra("signOut", false);
        if (signOut) {
            // 사용자가 로그아웃한 경우 구글 클라이언트 재설정
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                // 로그아웃 실패 시
                                Toast.makeText(LoginActivity.this, "로그아웃에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

        // onActivityResult 메소드 수정
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REO_SIGN_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // GoogleSignInAccount 가져오기
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    // 로그인 성공 시 처리
                    resultLogin(account);
                }
            } catch (ApiException e) {
                // 로그인 실패 처리
                Toast.makeText(this, "구글 로그인 실패: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resultLogin(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount").child(userId);
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            UserAccount userAccount = snapshot.getValue(UserAccount.class);
                                            String nickname = userAccount.getNickname();
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean("auto_login", true);
                                            editor.putString("nickname", nickname);
                                            editor.apply();

                                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // 사용자 정보가 없는 경우
                                            String email = account.getEmail();
                                            String nickname = account.getDisplayName();
                                            saveUserToFirebase(email, nickname);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(LoginActivity.this, "데이터베이스 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToFirebase(String email, String nickname) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("BODA").child("UserAccount");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 새로운 사용자 정보를 Firebase에 저장
        UserAccount userAccount = new UserAccount(userId, email, nickname, 0, 1, new Mission(0, 0));
        userRef.child(userId).setValue(userAccount).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "구글 로그인 버튼을 한번 더 눌러주세요 ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "사용자 정보 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services Error", Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect(); // 구글 API 클라이언트 연결
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect(); // 구글 API 클라이언트 연결 해제
    }

    @Override
    public void onBackPressed() {
        // 앱을 종료합니다.
        super.onBackPressed();
        // 현재 액티비티와 연관된 모든 액티비티를 종료하고 앱을 종료합니다.
        finishAffinity();
        // 프로세스를 완전히 종료하여 백그라운드에서 동작하지 않도록 합니다.
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
