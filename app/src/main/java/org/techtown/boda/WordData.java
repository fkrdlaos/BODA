package org.techtown.boda;

import java.io.Serializable;

public class WordData implements Serializable {
    private String word;
    private String meaning;
    private String example;
    private String dateTime;

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

    @Override
    public String toString() {
        return word; // Only return the word for ArrayAdapter display
    }
}