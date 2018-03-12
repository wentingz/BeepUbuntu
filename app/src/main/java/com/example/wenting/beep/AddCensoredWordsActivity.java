package com.example.wenting.beep;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

/**
 * Created by wenting on 3/10/18.
 */

public class AddCensoredWordsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addwords);

        ListView listView = (ListView) findViewById(R.id.list);
    }
}
