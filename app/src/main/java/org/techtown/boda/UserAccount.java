package org.techtown.boda;

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

    // 기본 생성자
    public UserAccount() {
        this.exp = 0;
        this.lv = 1;
        this.mission = new Mission();
    }

    // 모든 필드를 초기화하는 생성자
    public UserAccount(String idToken, String emailId, String nickname, int exp, int lv, Mission mission) {
        this.idToken = idToken;
        this.emailId = emailId;
        this.nickname = nickname;
        this.exp = exp;
        this.lv = lv;
        this.mission = mission;
    }

    // Getter와 Setter 메소드

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
        this.challenges = 0;
        this.words = 0;
    }

    // 모든 필드를 초기화하는 생성자
    public Mission(int challenges, int words) {
        this.challenges = challenges;
        this.words = words;
    }

    // Getter와 Setter 메소드
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
