package com.etv.util.gpio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GpioHelper {

    private File mFile;
    private FileOutputStream fileWriteStream;
    private FileInputStream fileInputStream;

    public GpioHelper(String path) {
        try {
            mFile = new File(path);
            if (!mFile.exists()) {
                mFile = null;
                return;
            }
            fileWriteStream = new FileOutputStream(mFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeInfo(String context) {
        if (fileWriteStream == null)
            return;
        try {
            fileWriteStream.write(context.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //读取会不停的新建对象谨慎使用
    public String readGpioInfo() {
        if (mFile == null)
            return "";
        closeGpio();
        String result = "";
        try {
            fileInputStream = new FileInputStream(mFile);
            byte[] data = new byte[1];
            fileInputStream.read(data);
            result = new String(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeGpio();
        }
        return result;
    }

    private void closeGpio() {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
                fileInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void releaseGpio() {
        if (fileWriteStream != null) {
            try {
                fileWriteStream.close();
                fileWriteStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        closeGpio();
    }
}
