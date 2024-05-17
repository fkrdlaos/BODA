package org.techtown.boda;

import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * 사용자 계정 정보 모델 클래스
 */
public class UserAccount {

    private String idToken; // Firebase Uid (고유 토큰 정보)
    private String emailId; // 이메일 아이디
    private String password; // 비밀번호
    private String nickname; // 닉네임
    private Mission mission; // 미션 정보
    private int exp; // 경험치
    private int lv; // 레벨

    public UserAccount() {
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }
}

class Mission {
    private int challenges; // 도전 횟수
    private int words; // 단어 수

    public Mission() {
    }

    public int getChallenges() {
        return challenges;
    }

    public void setChallenges(int challenges) {
        this.challenges = challenges;
    }

    public int getWords() {
        return words;
    }

    public void setWords(int words) {
        this.words = words;
    }
}
