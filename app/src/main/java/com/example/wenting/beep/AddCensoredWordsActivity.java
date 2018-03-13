package com.example.wenting.beep;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.wenting.dataObjects.CensoredWords;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by wenting on 3/10/18.
 */

public class AddCensoredWordsActivity extends AppCompatActivity {
    private static CensoredWordsAdaptor adapter;
    CensoredWords censoredWordsList;
    ListProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addwords);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        censoredWordsList = (CensoredWords) bundle.getSerializable("list");

        provider = new ListProvider();
        final ListView listView = (ListView) findViewById(R.id.list);
        adapter= new CensoredWordsAdaptor(provider, getApplicationContext());
        listView.setAdapter(adapter);



        final Button addBtn = (Button) findViewById(R.id.addWordButton);
        final EditText userInputWord = (EditText) findViewById(R.id.word_input);

        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String editTextValue = userInputWord.getText().toString().toLowerCase();
                String[] inputedWords = editTextValue.split(" ");
                for (int i=0; i < inputedWords.length; i++) {
                    provider.add(inputedWords[i]);
                }
                userInputWord.getText().clear();

            }
        });
    }

    public class ListProvider {
        public boolean isEmpty() {
            return censoredWordsList.getCensoredWordsHashSet().isEmpty();
        }

        public int size() {
            return censoredWordsList.getCensoredWordsHashSet().size();
        }

        public String get(int i) {
            ArrayList<String> censoredWordsArrayList = new ArrayList<String>(
                    censoredWordsList.getCensoredWordsTreeSet());
            return censoredWordsArrayList.get(i);
        }

        public void remove(String word) {
            censoredWordsList.deleteWord(word);
            adapter.notifyDataSetChanged();
        }

        public void add(String word) {
            censoredWordsList.addWord(word);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", censoredWordsList);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
