package com.etv.util.recorder;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/***
 * 用来保存预览帧数
 */
public class ProcessFrameAsyncTask extends AsyncTask<byte[], Void, String> {
    @Override
    protected String doInBackground(byte[]... params) {
        processFrame(params[0]);
        return null;
    }

    private void processFrame(byte[] frameData) {
        //下面这段注释掉的代码是把预览帧数据输出到sd卡中，以.yuv格式保存
        String path = "/sdcard/" + "testFrame" + ".yuv";
        File file = new File(path);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(frameData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
