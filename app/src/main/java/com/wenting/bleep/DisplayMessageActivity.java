package com.wenting.bleep;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by wenting on 3/9/18.
 */

public class DisplayMessageActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);

        TextView textbox =(TextView) findViewById(R.id.aboutus);


    }

}
