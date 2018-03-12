package com.wenting.dataObjects;

/**
 * Created by wenting on 3/9/18.
 */

import java.io.Serializable;


public class Timestamp implements Serializable{
    private Long startTime;
    private Long endTime;

    public Timestamp(long startSecond, int StartNanosec, long endSecond, int endNanosec) {
        startTime = startSecond * 1000000000 + StartNanosec;
        endTime = endSecond  * 1000000000 + endNanosec;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }
}
