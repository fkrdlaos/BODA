package org.techtown.boda;

import java.io.Serializable;

public class WordData implements Serializable {
    private String word;
    private String meaning;
    private String example;

    public WordData(String word, String meaning, String example) {
        this.word = word;
        this.meaning = meaning;
        this.example = example;
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

    @Override
    public String toString() {
        return word; // Only return the word for ArrayAdapter display
    }
}

