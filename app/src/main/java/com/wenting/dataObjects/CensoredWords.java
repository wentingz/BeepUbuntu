package com.wenting.dataObjects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Created by wenting on 3/10/18.
 */

public class CensoredWords implements Serializable{
    private HashSet<String> wordHashSet;
    private TreeSet<String> wordTreeSet;

    private String[] defaultCensoredWords = new String[]{"shit","piss","fuck","cunt","cocksucker",
            "motherfucker","tits"};

    public CensoredWords() {
        wordHashSet = new HashSet<>();
        wordTreeSet = new TreeSet<>();
        for (int i = 0; i < defaultCensoredWords.length; i++) {
            addWord(defaultCensoredWords[i]);
        }

    }

    public HashSet<String> getCensoredWordsHashSet() {
        return wordHashSet;
    }

    public TreeSet<String> getCensoredWordsTreeSet() {
        return wordTreeSet;
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
