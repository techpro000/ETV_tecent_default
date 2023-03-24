package com.etv.util.media;

import android.media.MediaPlayer;

import java.io.File;

public class AudioPlayerUtil implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static AudioPlayerUtil instance;

    public static AudioPlayerUtil getInstance() {
        if (instance == null) {
            synchronized (AudioPlayerUtil.class) {
                if (instance == null) {
                    instance = new AudioPlayerUtil();
                }
            }
        }
        return instance;
    }

    MediaPlayer mediaPlayer;

    public AudioPlayerUtil() {
        initMedia();
    }

    private void initMedia() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
    }

    MediaPlayerListener listener;

    public void startToPlayMedia(String filePath, MediaPlayerListener listener) {
        this.listener = listener;
        File file = new File(filePath);
        if (!file.exists()) {
            listener.playerError("File is not Exist");
            return;
        }
        initMedia();
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();//进行重置
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        listener.playerCompany();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        listener.playerError("Code : " + what + " /extra=" + extra);
        return false;
    }
}
