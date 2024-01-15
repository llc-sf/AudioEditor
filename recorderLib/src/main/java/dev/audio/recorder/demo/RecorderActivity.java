package dev.audio.recorder.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

import dev.audio.recorder.IdealRecorder;
import dev.audio.recorder.R;
import dev.audio.recorder.StatusListener;
import dev.audio.recorder.widget.wlv.WaveLineView;
import dev.audio.recorder.utils.Log;
import dev.audio.recorder.widget.WaveView;
import dev.audio.recorder.widget.san.CustomWaveView;

public class RecorderActivity extends AppCompatActivity {

    private Button recordBtn;
    private WaveView waveView;
    private CustomWaveView customWaveView;
    private WaveLineView waveLineView;
    private TextView tips;

    private IdealRecorder idealRecorder;

    private IdealRecorder.RecordConfig recordConfig;


    private StatusListener statusListener = new StatusListener() {
        @Override
        public void onStartRecording() {
            waveLineView.startAnim();
            tips.setText("开始录音");
        }

        @Override
        public void onRecordData(short[] data, int length) {

            for (int i = 0; i < length; i += 60) {
                waveView.addData(data[i]);
                customWaveView.addData(data[i]);
            }

            Log.d("MainActivity", "current buffer size is " + length);
        }

        @Override
        public void onVoiceVolume(int volume) {
            double myVolume = (volume - 40) * 4;
            waveLineView.setVolume((int) myVolume);
            Log.d("MainActivity", "current volume is " + volume);
        }

        @Override
        public void onRecordError(int code, String errorMsg) {
            tips.setText("录音错误" + errorMsg);
        }

        @Override
        public void onFileSaveFailed(String error) {
            Toast.makeText(RecorderActivity.this, "文件保存失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFileSaveSuccess(String fileUri) {
            Toast.makeText(RecorderActivity.this, "文件保存成功,路径是" + fileUri, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopRecording() {
            tips.setText("录音结束");
            waveLineView.stopAnim();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IdealRecorder.getInstance().init(this);
        setContentView(R.layout.activity_recorder);
        recordBtn = (Button) findViewById(R.id.register_record_btn);
        waveView = (WaveView) findViewById(R.id.wave_view);
        customWaveView = findViewById(R.id.custom_wave_view);
        waveLineView = (WaveLineView) findViewById(R.id.waveLineView);
        tips = (TextView) findViewById(R.id.tips);
        idealRecorder = IdealRecorder.getInstance();
        recordBtn.setOnLongClickListener(v -> {
            readyRecord();
            return true;
        });
        recordBtn.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                    stopRecord();
                    return false;

            }
            return false;
        });
        recordConfig = new IdealRecorder.RecordConfig(MediaRecorder.AudioSource.MIC, 48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    }


    // 定义请求权限的请求码
    private static final int REQUEST_AUDIO_PERMISSION_CODE = 100;

    // 其他成员变量...


    private void readyRecord() {
        if (checkPermissions()) {
            record();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean audioAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (audioAccepted && storageAccepted) {
                    record();
                } else {
                    Toast.makeText(this,
                            "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * 开始录音
     */
    private void record() {
        //如果需要保存录音文件  设置好保存路径就会自动保存  也可以通过onRecordData 回调自己保存  不设置 不会保存录音
        idealRecorder.setRecordFilePath(getSaveFilePath());
//        idealRecorder.setWavFormat(false);
        //设置录音配置 最长录音时长 以及音量回调的时间间隔
        idealRecorder.setRecordConfig(recordConfig).setMaxRecordTime(20000).setVolumeInterval(200);
        //设置录音时各种状态的监听
        idealRecorder.setStatusListener(statusListener);
        idealRecorder.start(); //开始录音

    }

    /**
     * 获取文件保存路径
     *
     * @return
     */
    private String getSaveFilePath() {
        File file = new File(Environment.getExternalStorageDirectory(), "Audio");
        if (!file.exists()) {
            file.mkdirs();
        }
        File wavFile = new File(file, "ideal.wav");
        return wavFile.getAbsolutePath();
    }


    /**
     * 停止录音
     */
    private void stopRecord() {
        //停止录音
        idealRecorder.stop();
    }
}