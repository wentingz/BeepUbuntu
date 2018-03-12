package com.example.wenting.beep;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Created by wenting on 3/10/18.
 */

public class CensoredWords implements Serializable{
    private HashSet<String> wordHashSet;
    private TreeSet<String> wordTreeSet;

    CensoredWords() {
        wordHashSet = new HashSet<String>();
        wordTreeSet = new TreeSet<String>();
    }

    public void addWord(String word) {
        if (!wordHashSet.contains(word)) {
            wordHashSet.add(word);
            wordTreeSet.add(word);
        }
        return;
    }

    public void deleteWord(String word) {
        wordHashSet.remove(word);
        wordTreeSet.remove(word);
    }


}
