package com.example.wenting.beep;

import com.wenting.web.bleep.servlet.WordTimestampObject;

/**
 * Created by wenting on 3/9/18.
 */

public interface AsyncResponse {
    void processFinish(WordTimestampObject output);
}
