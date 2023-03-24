package com.etv.police.camera;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.etv.util.MyLog;

public class MediaRecorderHelper implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {
    Camera camera;
    int rotation;
    Surface surface;
    public boolean isRecording;
    MediaRecorder mediaRecorder;
    String videoPath = "";
    RecorderListener recorderListener;
    int videoWidth = 640;
    int videoHeight = 480;
    boolean micEnable = false;


    public MediaRecorderHelper(Camera camera, int rotation, Surface surface) {
        this.camera = camera;
        this.rotation = rotation;
        this.surface = surface;
    }

    public void setRecorderListener(RecorderListener listener) {
        this.recorderListener = listener;
    }

    public void setVideoSize(int width, int height) {
        this.videoWidth = width;
        this.videoHeight = height;
    }

    public void startRecord(String savePath) {
        startRecord(savePath, -1);
    }


    /**
     * 开始录制
     */
    public void startRecord(String savePath, int maxDuration) {
        if (isRecording || camera == null || TextUtils.isEmpty(savePath))
            return;
        videoPath = savePath;
        try {
            if (mediaRecorder == null) {
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setOnErrorListener(this);
                mediaRecorder.setOnInfoListener(this);
            }
            camera.unlock();
            mediaRecorder.reset();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setOrientationHint(rotation);
            if (micEnable)
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);     //设置从麦克风采集声音

            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);   //设置从摄像头采集图像
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //设置视频的输出格式为MP4
            if (micEnable)
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); //设置音频的编码格式
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT); // 设置视频的编码格式
            MyLog.phone("===录制得尺寸===" + videoWidth + " / " + videoHeight);

            mediaRecorder.setVideoSize(videoWidth, videoHeight); // 设置视频大小
            mediaRecorder.setVideoFrameRate(30);  // 设置帧率
            //it.setMaxDuration(10000)
            mediaRecorder.setPreviewDisplay(surface); //设置
            //设置录制的视频编码比特率
            // mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
            mediaRecorder.setVideoEncodingBitRate(512 * 1024);
            if (maxDuration != -1) {
                mediaRecorder.setMaxDuration(maxDuration); //设置最大录像时间
            }
            mediaRecorder.setOutputFile(videoPath); //设置输出文件
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            if (recorderListener != null) {
                recorderListener.onStartRecord(videoPath);
            }
            log("视频保存路径：" + videoPath);
        } catch (Exception e) {
            e.printStackTrace();
            log("录制出错");
            if (recorderListener != null) {
                recorderListener.onRecordError("录制错误:" + e.toString());
            }
        }


    }


    /**
     * 结束录制
     */
    public void stopRecord() {
        if (!isRecording)
            return;
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            log("录制完成:" + videoPath);
        }
        isRecording = false;
        if (recorderListener != null) {
            recorderListener.onStopRecord(videoPath, false);
        }
    }

    public void release() {
        if (isRecording) {
            stopRecord();
        }
        if (mediaRecorder != null) {
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.release();
            mediaRecorder = null;
        }
        isRecording = false;
        recorderListener = null;
    }


    private void log(String msg) {
        Log.d("RecorderHelper", msg);
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        release();
        if (recorderListener != null) {
            recorderListener.onRecordError(what + "");
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED && recorderListener != null) {//如果已经到了最大的录制时间
            recorderListener.onStopRecord(videoPath, true);
        }
    }
}
