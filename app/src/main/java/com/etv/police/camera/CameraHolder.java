package com.etv.police.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.etv.util.MyLog;

import java.io.IOException;
import java.util.List;

public class CameraHolder implements Camera.PreviewCallback, RecorderListener {

    private Camera camera;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean isOpening;
    private boolean surfaceCreated = false;
    private int rotate;
    //录制功能开关，默认开启
    private boolean recorderEnable = true;
    MediaRecorderHelper mediaRecorderHelper;
    RecorderListener recorderListener;

    public CameraHolder(SurfaceView surfaceView) {
        this(Camera.CameraInfo.CAMERA_FACING_BACK, surfaceView);
    }

    public CameraHolder(int cameraId, SurfaceView surfaceView) {
        this.cameraId = cameraId;
        this.surfaceView = surfaceView;
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceCreated = true;
                if (camera == null) {
                    openCamera();
                }

                startPreview();

                log("=====surfaceCreated");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                log("=====surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                log("=====surfaceDestroyed");
                surfaceCreated = false;
                closeCamera();
            }
        });
    }


    /**
     * 打开相机
     */
    public void openCamera() {
        if (camera != null || isOpening || !surfaceCreated)
            return;
        isOpening = true;
        int numberOfCameras = Camera.getNumberOfCameras();
        log("===openCamera: count=" + numberOfCameras + " / current=" + cameraId);
        if (cameraId > numberOfCameras - 1) {
            isOpening = false;
            return;
        }
        try {
            camera = Camera.open(cameraId);
            if (recorderEnable) {
                if (mediaRecorderHelper != null) {
                    mediaRecorderHelper.release();
                    mediaRecorderHelper = null;
                }
                mediaRecorderHelper = new MediaRecorderHelper(camera, rotate, surfaceHolder.getSurface());
                mediaRecorderHelper.setRecorderListener(this);
            }
            initParameters();
            camera.setDisplayOrientation(rotate);
            camera.setPreviewCallback(this);
            isOpening = false;
        } catch (Exception e) {
            camera = null;
            //相机不存在
            isOpening = false;
        }
    }

    /**
     * 初始化相机配置
     */
    private void initParameters() {
        try {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size bestSize = getBestSize(surfaceView.getWidth(), surfaceView.getHeight(), parameters.getSupportedPreviewSizes());
            if (bestSize != null) {
                MyLog.phone("===显示得尺寸=" + bestSize.width + " / " + bestSize.height);
                parameters.setPreviewSize(bestSize.width, bestSize.height);
                parameters.setPictureSize(bestSize.width, bestSize.height);
                if (mediaRecorderHelper != null) {
                    mediaRecorderHelper.setVideoSize(bestSize.width, bestSize.height);
                }
            } else {
                MyLog.phone("===显示得尺寸=bestSize==null");
            }
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取最接近大小
     *
     * @param width
     * @param height
     * @param sizeList
     * @return
     */
    private Camera.Size getBestSize(int width, int height, List<Camera.Size> sizeList) {
        Camera.Size bestSize = null;
        double targetRatio = ((double) height) / width;
        double minDiff = targetRatio;
        for (Camera.Size size : sizeList) {
            double supportedRatio = (((double) size.width) / size.height);
            log("系统支持的尺寸 : " + size.width + " * " + size.height + ",  比例=" + supportedRatio);
        }
        for (Camera.Size size : sizeList) {
            if (size.width == height && size.height == width) {
                bestSize = size;
                break;
            }
            double supportedRatio = (((double) size.width) / size.height);
            if (Math.abs(supportedRatio - targetRatio) <= minDiff) {
                minDiff = Math.abs(supportedRatio - targetRatio);
                bestSize = size;
            }
        }
        log("目标尺寸 : " + width + " * " + height + ",  比例=" + targetRatio);
        if (bestSize != null) {
            log("最优尺寸 : " + bestSize.width + " * " + bestSize.height);
        }
        return bestSize;
    }


    /**
     * 与SurfaceView传播图像
     */
    public void startPreview() {
        if (camera == null || surfaceHolder == null || !surfaceCreated)
            return;
        // 预览相机,绑定
        try {
            camera.setPreviewDisplay(surfaceHolder);
//             系统相机默认是横屏的，我们要旋转90°
            camera.setDisplayOrientation(0);
            // 开始预览
            camera.startPreview();
            if (recorderListener != null)
                recorderListener.onPrepareRecord();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 旋转角度
     *
     * @param rotate
     */
    public void rotate(int rotate) {
        this.rotate = rotate;
        if (camera == null)
            return;
        camera.setDisplayOrientation(rotate);
    }


    /**
     * 是否正在录制
     *
     * @return
     */
    public boolean isRecording() {
        if (mediaRecorderHelper == null)
            return false;
        return mediaRecorderHelper.isRecording;
    }


    /**
     * 开始录制
     */
    public void startRecord(String savePath, int maxDuration) {
        if (mediaRecorderHelper == null)
            return;
        mediaRecorderHelper.startRecord(savePath, maxDuration);
    }

    /**
     * 开始录制
     *
     * @param savePath 　保存路径
     */
    public void startRecord(String savePath) {
        startRecord(savePath, -1);
    }


    /**
     * 停止录制
     */
    public void stopRecord() {
        if (mediaRecorderHelper == null)
            return;
        mediaRecorderHelper.stopRecord();
    }

    /**
     * 设置录制监听
     *
     * @param listener
     */
    public void setRecorderListener(RecorderListener listener) {
        this.recorderListener = listener;
    }


    /**
     * 关闭相机，释放资源
     */
    public void closeCamera() {
        isOpening = false;
        // 释放hold资源
        if (camera != null) {
            // 停止预览
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.setPreviewCallbackWithBuffer(null);
            // 释放相机资源
            camera.release();
            camera = null;
        }
    }

    public void release() {
        closeCamera();
        if (mediaRecorderHelper != null) {
            mediaRecorderHelper.release();
            mediaRecorderHelper = null;
            recorderListener = null;
        }
    }


    //切换摄像头
    public void switchCamera() {
        closeCamera();
        cameraId = cameraId == Camera.CameraInfo.CAMERA_FACING_BACK ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        openCamera();
        startPreview();
    }


    /**
     * 放大缩小预览大窗口
     *
     * @param zoom
     */
    public void handleZoom(int zoom) {
        if (camera == null)
            return;
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            params.setZoom(zoom);   //maxzoom  40

            camera.setParameters(params);
        } else {
            log("zoom not supported");
        }
    }

    public void takePic(Camera.PictureCallback takeCallBack) {
        if (camera == null)
            return;
        camera.takePicture((Camera.ShutterCallback) () -> {
        }, null, (data, camera) -> {
            camera.startPreview();
            if (takeCallBack != null) {
                takeCallBack.onPictureTaken(data, camera);
            }
        });
    }


    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }


    private void log(String msg) {
        Log.d("CamerHolder", msg);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void onPrepareRecord() {
        if (recorderListener != null)
            recorderListener.onPrepareRecord();
    }

    /**
     * 开始录制回调
     */
    @Override
    public void onStartRecord(String path) {
        if (recorderListener != null)
            recorderListener.onStartRecord(path);
    }

    /**
     * 停止录制回调
     *
     * @param path
     */
    @Override
    public void onStopRecord(String path, boolean maxTime) {
        if (recorderListener != null)
            recorderListener.onStopRecord(path, maxTime);
    }

    /**
     * 录制失败回调
     *
     * @param msg
     */
    @Override
    public void onRecordError(String msg) {
        if (recorderListener != null)
            recorderListener.onRecordError(msg);
    }

    public static boolean hasCamera(Context context) {
//        if (context == null) {
//            return false;
//        }
//        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
//        try {
//            String[] cameraIds = manager.getCameraIdList();
//            return cameraIds.length > 0;
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        return false;
        return isCameraCanUse();
    }

    public static boolean isCameraCanUse() {
        boolean canUse = false;
        Camera mCamera = null;
        try {
            mCamera = Camera.open(0);
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }
        if (mCamera != null) {
            mCamera.release();
            canUse = true;
        }
        return canUse;
    }


}
