package com.wenting.dataObjects;

/**
 * Created by wenting on 3/9/18.
 */

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wenting on 3/8/18.
 */

public class WordTimestampObject implements Serializable {
    private ArrayList<Timestamp> wordTimestamp = new ArrayList<>();
    private ArrayList<String> wordList = new ArrayList<>();

    public WordTimestampObject(ArrayList<String> wordList, ArrayList<Timestamp> wordTimestamp){
        this.wordList = wordList;
        this.wordTimestamp = wordTimestamp;
    }

    public ArrayList<Timestamp> getWordTimestamp(){
        return wordTimestamp;
    }

    public ArrayList<String> getWordList(){
        return wordList;
    }

}