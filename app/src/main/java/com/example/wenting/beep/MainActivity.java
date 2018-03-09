package com.example.wenting.beep;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.Snackbar;
import android.widget.Button;
import android.widget.FrameLayout;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.common.collect.ImmutableSet;
import com.newventuresoftware.waveform.WaveformView;
import com.wenting.web.bleep.servlet.WordTimestampObject;
import com.wenting.web.bleep.servlet.Timestamp;

import org.apache.commons.io.IOUtils;
import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;



import static java.util.Arrays.copyOfRange;

public class MainActivity extends AppCompatActivity implements AsyncResponse {
    private PlaybackThread mPlaybackThread;
    private RecordingThread mRecordingThread;

    File mAudioFile;

    WaveformView mPlaybackView;

    private static final int REQUEST_RECORD_AUDIO = 13;

    RangeSeekBar<Integer> rangeSeekBar;

    short[] sampleGlobal;
    short[] currentSample;
    byte[]  sampleByte;
    byte[]  sampleByteGlobal;
    int volumn;

    private TextView mText;
    FloatingActionButton playButt;

    long audioLength;
    static ImmutableSet<String> sBadWords = new ImmutableSet.Builder<String>()
            .add("great")
            .build();

    WordTimestampObject returnedOutput;

    @Override
    public void processFinish(WordTimestampObject output){
        returnedOutput = output;

        if (mText != null && !TextUtils.isEmpty(returnedOutput.getWordList().toString())) {
            getSenseredWord();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mText.setText(returnedOutput.getWordList().toString());
                    setWaveformView(currentSample);
                    updatePlaySample(currentSample);
                }
            });
        }
    }



//    private SpeechService  mSpeechService;
//
//    private final SpeechService.Listener mSpeechServiceListener =
//            new SpeechService.Listener() {
//                @Override
//                public void onSpeechRecognized(final String text, final boolean isFinal) {
//                    Log.e("text", text);
//                    if (mText != null && !TextUtils.isEmpty(text)) {
//                        getSenseredWord();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mText.setText(text);
//                                setWaveformView(currentSample);
//                                updatePlaySample(currentSample);
//                            }
//                        });
//                    }
//                }
//            };
//
//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder binder) {
//            mSpeechService = SpeechService.from(binder);
//            mSpeechService.addListener(mSpeechServiceListener);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mSpeechService = null;
//        }
//
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rangeSeekBar = new RangeSeekBar<>(this);
        rangeSeekBar.setRangeValues(0, 100);


        FrameLayout layout = (FrameLayout) findViewById(R.id.seekbar_placeholder);
        layout.addView(rangeSeekBar);


        mPlaybackView = (WaveformView) findViewById(R.id.playbackWaveformView);


        final Button beepBtn = (Button) findViewById(R.id.addBleep);

        final Button unbleepBtn = (Button) findViewById(R.id.rmvBleep);

        final Button share = (Button) findViewById(R.id.share);

        final FloatingActionButton recordButt = (FloatingActionButton) findViewById(R.id.fab);

        final FloatingActionButton recordButtLeft = (FloatingActionButton) findViewById(R.id.record);

        playButt = (FloatingActionButton) findViewById(R.id.playFab);

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.buttonLayout);

        final Button test = (Button) findViewById(R.id.test);



        test.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                final HttpPostAsyncTask task = new HttpPostAsyncTask(sampleByteGlobal, mText);
                task.output = MainActivity.this;
                String url = "http://192.168.86.69:8080/Bleep";
                Log.e("url", url);
                task.execute(url);

            }
        });

        mRecordingThread = new RecordingThread(this);

        mText = (TextView) findViewById(R.id.text);

        MobileAds.initialize(this, "ca-app-pub-1230113270016669~5290316014");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("EFE1F03989E81FBC17BB6C96B8F9F66C")
                .build();
        mAdView.loadAd(adRequest);




        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File audioPath = new File(getApplicationContext().getFilesDir(), "audio");
                if (!audioPath.exists()) {
                    audioPath.mkdirs();
                }

                File newFile = new File(audioPath, "final.m4a");

                InputStream is = new ByteArrayInputStream(sampleByte);


                PCMEncoder pcmEncoder = new PCMEncoder(16000, 44100, 1);
                pcmEncoder.setOutputPath(newFile.getAbsolutePath());
                pcmEncoder.prepare();
                try {
                    pcmEncoder.encode(is, 44100);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                pcmEncoder.stop();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("audio/*");

                Uri contentUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID, newFile);


                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "share"));
            }
        });

        recordButtLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mRecordingThread.recording()) {
                    startAudioRecordingSafe();
                    recordButtLeft.setImageResource(android.R.drawable.presence_busy);
                } else {
                    mRecordingThread.stopRecording();
                    try {
                        getAudioSample();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    speechRecognize();
                    setWaveformView(currentSample);
                    updatePlaySample(currentSample);
                    recordButtLeft.setImageResource(android.R.drawable.presence_audio_online);
                }
            }
        });

        recordButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRecordingThread.recording()) {
                    startAudioRecordingSafe();
                    recordButt.setImageResource(android.R.drawable.presence_busy);
                } else {
                    mRecordingThread.stopRecording();
                    try {
                        getAudioSample();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    speechRecognize();
                    setWaveformView(currentSample);
                    updatePlaySample(currentSample);
                    recordButt.setVisibility(View.GONE);
                    recordButtLeft.setVisibility(View.VISIBLE);
                    playButt.setVisibility(View.VISIBLE);
                    linearLayout.setVisibility(View.VISIBLE);

                }
            }
        });

        playButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecordingThread.recording()){
                    recordButtLeft.performClick();
                }

                if (!mPlaybackThread.playing()) {
                    updatePlaySample(currentSample);
                    mPlaybackThread.startPlayback();
                    playButt.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    mPlaybackThread.stopPlayback();
                    playButt.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        });

        beepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecordingThread.recording()){
                    recordButtLeft.performClick();
                    Toast.makeText(MainActivity.this, "Please select the portion to bleep.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPlaybackThread.playing()) {
                    playButt.performClick();
                    Toast.makeText(MainActivity.this, "Please select the portion to bleep.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int start = rangeSeekBar.getSelectedMinValue();
                    int end = rangeSeekBar.getSelectedMaxValue();
                    getBeepedAudio(start, end);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setWaveformView(currentSample);
                rangeSeekBar.setSelectedMaxValue(100);
                rangeSeekBar.setSelectedMinValue(0);
            }
        });

        unbleepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecordingThread.recording()){
                    recordButtLeft.performClick();
                    Toast.makeText(MainActivity.this, "Please select the portion to unbleep.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPlaybackThread.playing()) {
                    playButt.performClick();
                    Toast.makeText(MainActivity.this, "Please select the portion to unbleep.", Toast.LENGTH_SHORT).show();
                    return;
                }


                try {
                    int start = rangeSeekBar.getSelectedMinValue();
                    int end = rangeSeekBar.getSelectedMaxValue();
                    getUnbeepedAudio(start, end);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setWaveformView(currentSample);
                rangeSeekBar.setSelectedMaxValue(100);
                rangeSeekBar.setSelectedMinValue(0);
            }
        });
    }

    private void getSenseredWord() {
        ArrayList<String> words = returnedOutput.getWordList();
        if (words == null) {
            Log.e("no words", "!");
        }
        ArrayList<Timestamp> timestamps = returnedOutput.getWordTimestamp();
        for (int i = 0; i < words.size(); i++) {
            Log.e("word", words.get(i));
            if (sBadWords.contains(words.get(i))) {
                Timestamp target = timestamps.get(i);

                Log.e("startTime", "" + target.getStartTime());
                Log.e("endTime", "" + target.getEndTime());

                Log.e("length", "" + audioLength);
                try {
                    getBeepedAudioDouble(target.getStartTime(),target.getEndTime());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private double calculateAudioLength(int samplesCount, int sampleRate) {
        return (double) samplesCount / sampleRate;
    }

    private void speechRecognize() {
//        InputStream inputFile = new ByteArrayInputStream(sampleByteGlobal);
//        mSpeechService.recognizeInputStream(inputFile);

        final Button test = (Button) findViewById(R.id.test);
        test.performClick();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Prepare Cloud Speech API
//        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        // Stop Cloud Speech API
//        mSpeechService.removeListener(mSpeechServiceListener);
//        unbindService(mServiceConnection);
//        mSpeechService = null;

        super.onStop();
    }



    private void updatePlaySample(short[] samples) {
        if (samples != null) {
            int start = rangeSeekBar.getSelectedMinValue();
            int end = rangeSeekBar.getSelectedMaxValue();
            int startIndex = Math.round(samples.length * start / 100);
            int endIndex = Math.round(samples.length * end / 100);

            short[] playbackShort = copyOfRange(samples, startIndex, endIndex);
            mPlaybackThread = new PlaybackThread(playbackShort, new PlaybackListener() {
                @Override
                public void onProgress(int progress) {
                    int startOffset = Math.round(mPlaybackView.getAudioLength() *
                            rangeSeekBar.getSelectedMinValue() / 100);
                    mPlaybackView.setMarkerPosition(startOffset + progress);
                }

                @Override
                public void onCompletion() {
                    mPlaybackView.setMarkerPosition(mPlaybackView.getAudioLength());
                    playButt.performClick();
                }
            });
            audioLength = Math.round(calculateAudioLength(samples.length, 44100) * 1000000000);
        }
    }

    private void setWaveformView(short[] samples) {
        if (samples != null) {
            mPlaybackView.setChannels(1);
            mPlaybackView.setSampleRate(PlaybackThread.SAMPLE_RATE);
            mPlaybackView.setSamples(samples);

            mPlaybackView.invalidate();
        }
    }

    private void getBeepedAudioDouble(double start, double end) throws IOException {
        int startIndex = (int) Math.round(sampleByte.length * start / audioLength);
        int endIndex = (int) Math.round(sampleByte.length * end / audioLength);
        Log.e("startInd", "" + Math.round(sampleByte.length * start / audioLength));
        Log.e("endInd", "" + Math.round(sampleByte.length * end / audioLength));
        for (int i = startIndex; i < endIndex; i++) {
            sampleByte[i] = (byte) Math.round(volumn * Math.sin(i * 6.3 / 50));
        }

        ShortBuffer sb = ByteBuffer.wrap(sampleByte).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        currentSample = samples;
    }



    private void getBeepedAudio(int start, int end) throws IOException {
        int startIndex = Math.round(sampleByte.length * start / 100);
        int endIndex = Math.round(sampleByte.length * end / 100);
        for (int i = startIndex; i < endIndex; i++) {
            sampleByte[i] = (byte) Math.round(volumn * Math.sin(i * 6.3 / 50));
        }

        ShortBuffer sb = ByteBuffer.wrap(sampleByte).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        currentSample = samples;
    }

    private void getUnbeepedAudio(int start, int end) throws IOException {
        int startIndex = Math.round(sampleGlobal.length * start / 100);
        int endIndex = Math.round(sampleGlobal.length * end / 100);
        for (int i = startIndex; i < endIndex; i++) {
            currentSample[i] = sampleGlobal[i];
        }

        int startByte = Math.round(sampleByte.length * start / 100);
        int endByte = Math.round(sampleByte.length * end / 100);
        for (int i = startByte; i < endByte; i++) {
            sampleByte[i] = sampleByteGlobal[i];
        }

    }


    private void getAudioSample() throws IOException{
        mAudioFile = new File(getExternalFilesDir(Environment.DIRECTORY_PODCASTS), "Demo.pcm");
        InputStream is = new FileInputStream(mAudioFile);
        byte[] data;
        try {
            data = IOUtils.toByteArray(is);
        } finally {
            is.close();
        }
        sampleByte = data;
        sampleByteGlobal = data.clone();

        byte[] data2 = data.clone();
        Arrays.sort(data2);
        int index = (int) Math.round(data2.length * 0.9);
        volumn = (int) Math.round(data2[index] * 0.7);

        ShortBuffer sb = ByteBuffer.wrap(sampleByte).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        sampleGlobal = samples.clone();
        currentSample = samples;
    }


    private void startAudioRecordingSafe() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            mRecordingThread.startRecording();
        } else {
            requestMicrophonePermission();
        }
    }

    private void requestMicrophonePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
            // Show dialog explaining why we need record audio
            Snackbar.make(mPlaybackView, "Microphone access is required in order to record audio",
                    Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
        FloatingActionButton recordButt = (FloatingActionButton) findViewById(R.id.fab);
        recordButt.setImageResource(android.R.drawable.presence_audio_online);
    }

}

