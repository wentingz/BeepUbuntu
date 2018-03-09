package com.example.wenting.beep;

import java.io.Serializable;

/**
 * Created by wenting on 11/9/17.
 */

public class Timestamp implements Serializable {
    private long startTime;
    private long endTime;

    public Timestamp(long startSecond, int StartNanosec, long endSecond, int endNanosec) {
        startTime = startSecond * 1000000000 + StartNanosec;
        endTime = endSecond  * 1000000000 + endNanosec;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
