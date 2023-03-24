package com.etv.util.ijk;


/***
 * 播放状态回调
 */
public interface IJKPlayerListener {

    void onAutoCompletion();

    void onError(int what, int extra);
}
