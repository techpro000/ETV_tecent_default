package com.etv.util.recorder;


import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;

import java.io.IOException;

public class MediaRecorderUtil {

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private MediaRecorder mMediaRecorder;
    private Camera.Parameters mParameters;

    public MediaRecorderUtil() {

    }

    /**
     * 准备mHolder工作
     * 打开相机
     */
    public void initCameraAndSurfaceViewHolder(SurfaceView mSurfaceView) {
        if (mHolder == null) {
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(new HoderCallBack());
        }
        if (mCamera == null) {
            mCamera = Camera.open(0);
        }
    }


    private boolean prepareMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
//        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
        String path = "/sdcard/123.mp4";
        mMediaRecorder.setOutputFile(path);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e("haha", "=========录像异常==" + e.toString());
            releaseMediaRecorder();
        }
        return true;
    }

    /**
     * 释放相机资源
     */
    private void realseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public boolean startRecording() {
        if (prepareMediaRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    public void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
        }
        releaseMediaRecorder();
    }


    /**
     * SurfaceHolder.Callback回调函数
     */
    private class HoderCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            openPreView();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (holder.getSurface() == null) {
                return;
            }
            mCamera.stopPreview();
            openPreView();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    /**
     * initCameraAndSurfaceViewHolder初始化hoder后
     * 2.设置预览功能
     */
    private void openPreView() {
        try {
            if (mCamera != null) {
                mParameters = mCamera.getParameters();
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                mCamera.setParameters(mParameters);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        //自动对焦回调
                    }
                });
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        new ProcessFrameAsyncTask().execute(data);
                        Log.d("TAG", "预览帧大小：" + String.valueOf(data.length));
                    }
                });
                int widthScreen = SharedPerUtil.getScreenWidth();
                int heightScreen = SharedPerUtil.getScreenHeight();
                int degree = 90;
                if (widthScreen > heightScreen) {  //横屏
                    degree = 90;
                } else {
                    degree = 0;
                }
                mCamera.setDisplayOrientation(degree);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    public void onDestroy() {
        releaseMediaRecorder();
        realseCamera();
    }

}
