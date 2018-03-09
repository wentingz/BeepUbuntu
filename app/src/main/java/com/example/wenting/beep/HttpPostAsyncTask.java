package com.example.wenting.beep;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import com.google.common.collect.ImmutableSet;
import com.wenting.web.bleep.servlet.WordTimestampObject;
import com.wenting.web.bleep.servlet.Timestamp;
import org.w3c.dom.Text;


/**
 * Created by wenting on 11/19/17.
 */

public class HttpPostAsyncTask extends AsyncTask<String, Void, WordTimestampObject> {
    private byte[] sampleByteGlobal;
    private TextView mText;
    private ArrayList<Timestamp> wordTimestamp = new ArrayList<>();
    private ArrayList<String> wordList = new ArrayList<>();
    public AsyncResponse output = null;

    static ImmutableSet<String> sBadWords = new ImmutableSet.Builder<String>()
            .add("great")
            .build();

    HttpPostAsyncTask(byte[]  sampleByteGlobal, TextView mText) {
        this.sampleByteGlobal = sampleByteGlobal;
        this.mText = mText;
    }


    @Override
    protected WordTimestampObject doInBackground(String... strings) {
        WordTimestampObject result = null;
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setRequestProperty("Content-Type", "text/plain");
            urlConnection.setDoOutput(true);

            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(sampleByteGlobal);
            outputStream.flush();
            outputStream.close();

            int responseCode = urlConnection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                ObjectInputStream is
                        = new ObjectInputStream(urlConnection.getInputStream());
                result = (WordTimestampObject) is.readObject();

                Log.e("word", result.getWordList().get(0));
                is.close();
            }

        } catch (Exception e) {
            Log.e("exception", "failed decoding", e);
        }

        return result;
    }

    @Override
    protected void onPostExecute(WordTimestampObject o) {
        super.onPostExecute(o);
        output.processFinish(o);
//        wordList = o.getWordList();
//        wordTimestamp = o.getWordTimestamp();
//
//        if (mText != null && !TextUtils.isEmpty(text)) {
//            getSenseredWord();
//            mText.setText(text);
//            setWaveformView(currentSample);
//            updatePlaySample(currentSample);
//        }
    }

//    private void getSenseredWord() {
//        if (wordList == null) {
//            Log.e("no words", "!");
//        }
////        ArrayList<Timestamp> timestamps = mSpeechService.getWordTimestamp();
//        for (int i = 0; i < wordList.size(); i++) {
//            Log.e("word", wordList.get(i));
//            if (sBadWords.contains(wordList.get(i))) {
//                Timestamp target = wordTimestamp.get(i);
////                Log.e("startTime", "" + target.getStartTime());
////                Log.e("endTime", "" + target.getEndTime());
////
////                Log.e("length", "" + audioLength);
//                try {
//                    getBeepedAudioDouble(target.getStartTime(),target.getEndTime());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private void getBeepedAudioDouble(double start, double end) throws IOException {
//        int startIndex = (int) Math.round(sampleByte.length * start / audioLength);
//        int endIndex = (int) Math.round(sampleByte.length * end / audioLength);
//        Log.e("startInd", "" + Math.round(sampleByte.length * start / audioLength));
//        Log.e("endInd", "" + Math.round(sampleByte.length * end / audioLength));
//        for (int i = startIndex; i < endIndex; i++) {
//            sampleByte[i] = (byte) Math.round(volumn * Math.sin(i * 6.3 / 50));
//        }
//
//        ShortBuffer sb = ByteBuffer.wrap(sampleByte).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
//        short[] samples = new short[sb.limit()];
//        sb.get(samples);
//        currentSample = samples;
//    }
//

}