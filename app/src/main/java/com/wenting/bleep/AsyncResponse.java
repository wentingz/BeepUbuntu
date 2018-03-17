package com.wenting.bleep;


import com.wenting.dataObjects.WordTimestampObject;

/**
 * Created by wenting on 3/9/18.
 */

public interface AsyncResponse {
    void processFinish(WordTimestampObject output);
}
