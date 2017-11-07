package com.example.wenting.beep;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.support.design.widget.Snackbar;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ToggleButton;
import com.newventuresoftware.waveform.WaveformView;
import org.apache.commons.io.IOUtils;
import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

import static java.util.Arrays.copyOfRange;

public class MainActivity extends AppCompatActivity {
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

    private SpeechService  mSpeechService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rangeSeekBar = new RangeSeekBar<>(this);
        rangeSeekBar.setRangeValues(0, 100);


        FrameLayout layout = (FrameLayout) findViewById(R.id.seekbar_placeholder);
        layout.addView(rangeSeekBar);


        mPlaybackView = (WaveformView) findViewById(R.id.playbackWaveformView);

        final Button playButt = (Button) findViewById(R.id.playBtn);

        final ToggleButton recordButt = (ToggleButton) findViewById(R.id.recordBtn);

        final Button beepBtn = (Button) findViewById(R.id.addBleep);

        final Button unbleepBtn = (Button) findViewById(R.id.rmvBleep);

        final Button speech = (Button) findViewById(R.id.speech);

        final Button share = (Button) findViewById(R.id.share);

        mRecordingThread = new RecordingThread(this);


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File audioPath = new File(getApplicationContext().getFilesDir(), "audio");
                if (!audioPath.exists()) {
                    audioPath.mkdirs();
                }

//                File newFile = new File("/data/data/com.example.wenting.beep/files/final.pcm");
//                File newFile = new File(getApplicationContext().getFilesDir(), "final.pcm");
                File newFile = new File(audioPath, "final.pcm");

                if (newFile == null) {
                    Log.e("final", "file is null");
                }

                Log.e("path", newFile.getAbsolutePath());

                FileOutputStream os = null;

                try {
                    os = new FileOutputStream(newFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }



                try {
                    byte bData[] = sampleByteGlobal;
                    os.write(bData);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setType("audio/*");

                Uri contentUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID, newFile);


                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "share"));
            }
        });


        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        recordButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRecordingThread.recording()) {
                    startAudioRecordingSafe();
                } else {
                    mRecordingThread.stopRecording();
                    try {
                        getAudioSample();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    setWaveformView(currentSample);
                }
            }
        });

        playButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePlaySample(currentSample);
                if (!mPlaybackThread.playing()) {
                    mPlaybackThread.startPlayback();
                } else {
                    mPlaybackThread.stopPlayback();
                }
            }
        });

        beepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                }
            });
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
        if (mAudioFile == null) {
            Log.e("demo", "file is null");
        }
        InputStream is = new FileInputStream(mAudioFile);
        byte[] data;
        try {
            data = IOUtils.toByteArray(is);
        } finally {
            if (is != null) {
                is.close();
            }
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
        ToggleButton recordButt = (ToggleButton) findViewById(R.id.recordBtn);
        recordButt.setChecked(false);
    }

}

