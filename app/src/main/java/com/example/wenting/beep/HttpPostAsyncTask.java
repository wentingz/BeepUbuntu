package com.example.wenting.beep;
import android.os.AsyncTask;
import android.util.Log;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.wenting.dataObjects.WordTimestampObject;



/**
 * Created by wenting on 11/19/17.
 */

public class HttpPostAsyncTask extends AsyncTask<String, Void, WordTimestampObject> {
    private byte[] sampleByteGlobal;
    public AsyncResponse output = null;


    HttpPostAsyncTask(byte[]  sampleByteGlobal) {
        this.sampleByteGlobal = sampleByteGlobal;
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
    }
}