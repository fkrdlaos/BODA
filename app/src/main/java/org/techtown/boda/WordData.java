package org.techtown.boda;

import java.io.Serializable;

public class WordData implements Serializable {
    private String word;
    private String meaning;
    private String example;
    private String dateTime;
    private boolean hasImage; // 이미지가 있는지 여부를 저장하는 변수
    private int imageResId; // 이미지 리소스 ID

    public WordData(String word, String meaning, String example, String dateTime) {
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.dateTime = dateTime;
    }

    public String getWord() {
        return word;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getExample() {
        return example;
    }

    public String getDateTime() {
        return dateTime;
    }

    // 이미지가 있는지 확인하는 메서드
    public boolean hasImage() {
        return hasImage;
    }

    // 이미지 리소스 ID를 반환하는 메서드
    public int getImageResId() {
        return imageResId;
    }

    // 이미지가 있는 경우에만 설정하는 메서드
    public void setImage(int imageResId) {
        this.imageResId = imageResId;
        this.hasImage = true;
    }

    @Override
    public String toString() {
        return word; // Only return the word for ArrayAdapter display
    }
}
