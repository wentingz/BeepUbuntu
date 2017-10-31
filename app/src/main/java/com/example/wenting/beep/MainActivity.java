package com.example.wenting.beep;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.support.design.widget.Snackbar;
import android.widget.FrameLayout;
import android.widget.ToggleButton;
import com.newventuresoftware.waveform.WaveformView;
import org.apache.commons.io.IOUtils;
import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class MainActivity extends AppCompatActivity {
    private PlaybackThread mPlaybackThread;
    private RecordingThread mRecordingThread;

    File mAudioFile;

    WaveformView mPlaybackView;

    private static final int REQUEST_RECORD_AUDIO = 13;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlaybackView = (WaveformView) findViewById(R.id.playbackWaveformView);

        final ToggleButton playButt = (ToggleButton) findViewById(R.id.playBtn);

        final ToggleButton recordButt = (ToggleButton) findViewById(R.id.recordBtn);

        final ToggleButton beepBtn = (ToggleButton) findViewById(R.id.addBeep);

        mRecordingThread = new RecordingThread(this);



        recordButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRecordingThread.recording()) {
                    startAudioRecordingSafe();
                } else {
                    mRecordingThread.stopRecording();
                    setWaveformView();
                }
            }
        });


        playButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                short[] newSample = null;
                try {
                    newSample = getBeepedAudio(0, 100000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateWaveformView(newSample);
            }
        });


        RangeSeekBar<Integer> rangeSeekBar = new RangeSeekBar<>(this);
        rangeSeekBar.setRangeValues(0, 100);


        FrameLayout layout = (FrameLayout) findViewById(R.id.seekbar_placeholder);
        layout.setBackgroundColor(Color.BLACK);
        layout.addView(rangeSeekBar);

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
    }

    private void setWaveformView() {

        short[] samples = null;
        try {
            samples = getAudioSample();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (samples != null) {


            mPlaybackThread = new PlaybackThread(samples, new PlaybackListener() {
                @Override
                public void onProgress(int progress) {
                    mPlaybackView.setMarkerPosition(progress);
                }
                @Override
                public void onCompletion() {
                    mPlaybackView.setMarkerPosition(mPlaybackView.getAudioLength());
                }
            });
            mPlaybackView.setChannels(1);
            mPlaybackView.setSampleRate(PlaybackThread.SAMPLE_RATE);
            mPlaybackView.setSamples(samples);

            mPlaybackView.invalidate();
        }
    }

    private short[] getAudioSample() throws IOException{
        mAudioFile = new File(getExternalFilesDir(Environment.DIRECTORY_PODCASTS), "Demo.pcm");
        InputStream is = new FileInputStream(mAudioFile);
        byte[] data;
        try {
            data = IOUtils.toByteArray(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        return samples;
    }

    private short[] getBeepedAudio(int start, int length) throws IOException {
        File newFile = new File(getExternalFilesDir(Environment.DIRECTORY_PODCASTS), "Demo.pcm");
        InputStream is = new FileInputStream(newFile);
        byte[] data;
        try {
            data = IOUtils.toByteArray(is);
            for (int i = start; i < length; i++) {
                if (i <= data.length) {
                    data[i] = (byte) Math.round(50 * Math.sin(i * 6.3 / 50));
                }

            }
        } finally {
            if (is != null) {
                is.close();
            }
        }

        ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        return samples;
    }

    private void updateWaveformView(short[] samples) {

        if (samples != null) {


            mPlaybackThread = new PlaybackThread(samples, new PlaybackListener() {
                @Override
                public void onProgress(int progress) {
                    mPlaybackView.setMarkerPosition(progress);
                }
                @Override
                public void onCompletion() {
                    mPlaybackView.setMarkerPosition(mPlaybackView.getAudioLength());
                }
            });
            mPlaybackView.setChannels(1);
            mPlaybackView.setSampleRate(PlaybackThread.SAMPLE_RATE);
            mPlaybackView.setSamples(samples);

            mPlaybackView.invalidate();
        }
    }

}

