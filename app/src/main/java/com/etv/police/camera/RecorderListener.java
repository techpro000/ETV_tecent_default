package com.etv.police.camera;

public interface RecorderListener {
    void onPrepareRecord();

    void onStartRecord(String path);

    void onStopRecord(String path, boolean maxTime);

    void onRecordError(String msg);
}
