package com.etv.util;

import android.util.Log;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;

import java.io.File;

public class MyLog {

    public static void i(String tag, String s) {
        if (AppConfig.IF_PRINT_LOG) {
            Log.i(tag, s);
        }
    }

    public static void d(String tag, String s) {
        if (AppConfig.IF_PRINT_LOG) {
            Log.d(tag, s);
        }
    }


    /***
     * 用来打印接受的指令
     * @param message
     */
    public static void message(String message) {
        message(message, false);
    }

    public static void message(String message, boolean print) {
        d("message", message);
        if (print) {
            printExceptionToSd("message", message);
        }
    }

    public static void http(String s) {
        d("http", s);
    }

    public static void image(String s) {
        i("image", s);
    }

    public static void update(String json) {
        update(json, false);
    }

    public static void update(String json, boolean isPrint) {
        d("update", json);
        if (isPrint) {
            printExceptionToSd("update", json);
        }
    }

    public static void login(String s) {
        d("login", s);
    }

    public static void media(String s) {
        d("video", s);
    }

    public static void powerOnOff(String s) {
        d("powerOnOff", s);
        printExceptionToSd("powerOnOff", s);
    }

    public static void powerOnOff(String s, boolean isPrint) {
        d("powerOnOff", s);
        if (isPrint) {
            printExceptionToSd("powerOnOff", s);
        }
    }

    public static void task(String s) {
        task(s, false);
    }

    public static void del(String s) {
        del(s, false);
    }

    public static void del(String s, boolean printTag) {
        d("delete", s);
        if (printTag) {
            printExceptionToSd("delete", s);
        }
    }

    public static void task(String s, boolean isPrint) {
        d("task", s);
        if (isPrint) {
            printExceptionToSd("task", s);
        }
    }

    public static void playTask(String s) {
        playTask(s, false);
    }

    public static void police(String s) {
        d("police", s);
    }


    public static void playTask(String s, boolean isPrint) {
        d("playTask", s);
        if (isPrint) {
            printExceptionToSd("playTask", s);
        }
    }

    public static void playTaskBack(String s) {
        d("playTaskBack", s);
    }

    /**
     * 混播log
     *
     * @param s
     */
    public static void playMix(String s) {
        d("playMix", s);
    }

    public static void apk(String s) {
        d("apk", s);
    }

    public static void socket(String s) {
        socket(s, false);
    }

    public static void socket(String s, boolean isPrint) {
        d("SiteWebsocket", s);
        if (isPrint) {
            printExceptionToSd("SiteWebsocket", s);
        }
    }

    public static void wifi(String s) {
        d("wifi", s);
    }

    public static void diff(String s) {
        diff(s, false);
    }

    public static void diff(String s, boolean isPrint) {
        d("diff", s);
        if (isPrint) {
            printExceptionToSd("diff", s);
        }
    }

    public static void video(String s) {
        video(s, false);
    }

    public static void video(String s, boolean isPrint) {
        d("videoPlay", s);
        if (isPrint) {
            printExceptionToSd("videoPlay", s);
        }
    }

    public static void hdmi(String desc) {
        hdmi(desc, false);
    }

    public static void hdmi(String desc, boolean isPrint) {
        d("hdmi_in", desc);
        if (isPrint) {
            printExceptionToSd("hdmi_in", desc);
        }
    }

    public static void cdl(String s) {
        cdl(s, false);
    }

    public static void face(String desc, boolean isPrint) {
        d("face", desc);
        if (isPrint) {
            printExceptionToSd("face", desc);
        }
    }

    public static void face(String desc) {
        face(desc, false);
    }

    public static void cdl(String s, boolean isPrint) {
        d("cdl", s);
        if (isPrint) {
            printExceptionToSd("cdl", s);
        }
    }

    public static void draw(String s) {
        d("draw", s);
    }

    public static void timer(String s) {
        timer(s, false);
    }

    public static void timer(String s, boolean isPrint) {
        d("timer", s);
        if (isPrint) {
            printExceptionToSd("timer", s);
        }
    }

    public static void down(String s) {
        down(s, false);
    }

    public static void down(String s, boolean isPrint) {
        d("down", s);
        if (isPrint) {
            printExceptionToSd("down", s);
        }
    }

    public static void banner(String toString) {
        banner(toString, false);
    }

    public static void banner(String toString, boolean isPrint) {
        d("banner", toString);
        if (isPrint) {
            printExceptionToSd("banner", toString);
        }
    }

    public static void location(String s) {
        d("location", s);
    }

    public static void taskDown(String s) {
        d("taskDown", s);
    }

    public static void test(String s) {
        d("test", s);
    }

    public static void wps(String s) {
        d("wps", s);
    }

    public static void file(String s) {
        d("file", s);
    }

    public static void db(String s) {
        db(s, false);
    }

    public static void db(String s, boolean isPrint) {
        d("db", s);
        if (isPrint) {
            printExceptionToSd("db", s);
        }
    }

    public static void udp(String s) {
        d("udp", s);
    }

    public static void temp(String msg) {
        d("temputure", msg);
    }

    public static void touch(String s) {
        d("touch", s);
    }

    public static void guardian(String s) {
        d("guardian", s);
    }

    public static void ExceptionPrint(String message) {
        d("ExceptionPrint", message);
        printExceptionToSd("ExceptionPrint", message);
    }

    public static void sleep(String s) {
        sleep(s, false);
    }

    public static void sleep(String s, boolean isPrint) {
        d("sleep", s);
        if (isPrint) {
            printExceptionToSd("sleep", s);
        }
    }

    public static void netty(String desc) {
        netty(desc, false);
    }

    public static void nettyMessage(String desc) {
        d("netty", "Message: " + desc);
    }

    public static void netty(String desc, boolean isPrint) {
        d("netty", desc);
        if (isPrint) {
            printExceptionToSd("netty", desc);
        }
    }

    public static void screen(String s) {
        d("screen", s);
        printExceptionToSd("screen", s);
    }

    public static void aesCodeln(String s) {
        d("aesCodeln", s);
    }

    public static void bgg(String desc) {
        d("bgg", desc);
//        printExceptionToSd("==背景图处理: " + desc);
    }

    public static void sdckeck(String toString) {
        d("sdckeck", toString);
    }

    public static void phone(String s) {
        d("phone", s);
    }

    public static void gpio(String s) {
        d("gpio", s);
    }

    public static void zip(String s) {
        d("zip", s);
    }

    public static void tts(String tts) {
        tts(tts, false);
    }

    public static void usb(String s) {
        d("usb", s);
    }

    public static void sccard(String s) {
        d("sccard", s);
    }

    public static void mmkv(String s) {
        d("mmkv", s);
    }

    public static void shared(String s) {
        d("shared", s);
    }

    public static void pdf(String desc) {
        d("pdfView", desc);
    }

    public static void tts(String tts, boolean isPrint) {
        d("tts", tts);
        if (isPrint) {
            printExceptionToSd("tts", tts);
        }
    }

    private static void printExceptionToSd(String printTag, String message) {
        if (!AppInfo.PERMISSION_COMPLAIY) {
            return;
        }
        try {
            String dataInfo = SimpleDateUtil.getCurrentDateLong() + ".txt";
            String woekPlace = AppInfo.BASE_CRASH_LOG() + "/" + dataInfo;
            File file = new File(woekPlace);
            if (file.exists() && file.isDirectory()) {
                file.delete();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            long currentTime = SimpleDateUtil.getCurrentTimelONG();
            FileUtil.writeMessageInfoToTxt(woekPlace, currentTime + "=" + printTag + " = " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
