package com.example.wenting.beep;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

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

        final ToggleButton beepBtn = (ToggleButton) findViewById(R.id.addBleep);

        mRecordingThread = new RecordingThread(this);


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
            sampleByte[i] = (byte) Math.round(50 * Math.sin(i * 6.3 / 50));
        }

        ShortBuffer sb = ByteBuffer.wrap(sampleByte).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        currentSample = samples;
    }


    private void getAudioSample() throws IOException{
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
        sampleByte = data;
        ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        sampleGlobal = samples;
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

