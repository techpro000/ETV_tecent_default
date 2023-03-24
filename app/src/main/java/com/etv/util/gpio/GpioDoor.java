//package com.etv.util.gpio;
//
//import android.os.Handler;
//import android.os.Message;
//
//import com.ys.acsdev.util.LogUtil;
//
//// Created by kermitye on 2020/5/14 9:23
//
///**
// * ===================================================================================
// * gpio方式开门
// * ===================================================================================
// */
//public class GpioDoor extends BaseDoor {
//
//    private GpioDoor() {
//    }
//
//    private static class SingletonHolder {
//        public static final GpioDoor INSTANCE = new GpioDoor();
//    }
//
//    public static GpioDoor getInstance() {
//        return SingletonHolder.INSTANCE;
//    }
//
//
//    private static final int gpIO = 171;
//    private static final int MSG_CLOSE = 0x01;
//
//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case MSG_CLOSE:
//                    close();
//                    break;
//            }
//        }
//    };
//    DoorResultListener mListener;
//
//    public void init() {
////        GpioUtils.upgradeRootPermissionForExport();
//        checkIO();
//    }
//
//    @Override
//    public void open() {
//        log("=======DoorManager======开們");
//        boolean success = GpioUtils.writeGpioValue(gpIO, "1");
//        if (mListener != null)
//            mListener.onDoorResult(success);
//        if (mCloseTime > 0) {
//            mHandler.sendEmptyMessageDelayed(MSG_CLOSE, (long) (mCloseTime * 1000));
//        }
//    }
//
//    @Override
//    public void close() {
//        log("=======DoorManager=====000=关门");
//        GpioUtils.writeGpioValue(gpIO, "0");
////        mHandler.postDelayed(() -> openDoor(), 500);
//    }
//
//    public boolean checkState() {
//        String value = getGpioValue();
//        log("===============当前门的状：" + value);
//        return value == "1";
//    }
//
//
//    private boolean checkIO() {
//        if (GpioUtils.exportGpio(gpIO)) {
//            GpioUtils.upgradeRootPermissionForGpio(gpIO);
//            String status = GpioUtils.getGpioDirection(gpIO);
//            if ("" == status || status == null) {
//                return false;
//            } else {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private String getGpioValue() {
//        return GpioUtils.getGpioValue(gpIO);
//    }
//
//
//    public void setDoorResultListener(DoorResultListener listener) {
//        this.mListener = listener;
//    }
//
//
//    @Override
//    public void destory() {
//        mListener = null;
//        mHandler.removeMessages(MSG_CLOSE);
//    }
//
//    private void log(String msg) {
//        LogUtil.e(msg, "GpioDoor");
//    }
//
//}
