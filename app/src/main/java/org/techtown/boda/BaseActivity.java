package org.techtown.boda;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BaseActivity extends AppCompatActivity {
    protected FirebaseAuth mAuth;
    protected GoogleSignInClient mGoogleSignInClient;
    protected SharedPreferences sharedPreferences;
    private NetworkChangeReceiver networkChangeReceiver;
    private boolean isReceiverRegistered = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase 인증 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("MY_PREFS", MODE_PRIVATE);

        // GoogleSignInOptions 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 네트워크 상태 변경 리시버 등록
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isReceiverRegistered) {
            unregisterReceiver(networkChangeReceiver);
            isReceiverRegistered = false;
        }
    }

    private void registerNetworkReceiver() {
        if (!isReceiverRegistered) {
            networkChangeReceiver = new AppNetworkChangeReceiver() {
                @Override
                protected void onNetworkUnavailable(Context context) {
                    handler.post(() -> {
                        if (!isNetworkAvailable(context)) {
                            showNetworkDialog();
                        }
                    });
                }
            };
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkChangeReceiver, filter);
            isReceiverRegistered = true;
        }
    }

    public void showNetworkDialog() {
        if (!isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("네트워크 연결이 끊겼습니다. 로그인 화면으로 이동합니다.")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            logoutAndRedirect();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    protected void logoutAndRedirect() {
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
                        Toast.makeText(BaseActivity.this, "구글 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                        intent.putExtra("signOut", true); // 로그인 화면에서 다시 로그인할 수 있도록 플래그 설정
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(BaseActivity.this, "구글 로그아웃 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // 일반 로그인 로그아웃
                mAuth.signOut();

                Toast.makeText(BaseActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                // LoginActivity로 이동
                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                intent.putExtra("signOut", true); // 로그인 화면에서 다시 로그인할 수 있도록 플래그 설정
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(BaseActivity.this, "이미 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
