package com.etv.util;


import java.io.InputStream;
import java.io.OutputStream;

public class Shell {

    private static final Runtime RUNTIME;
    private static Process mSuProcess;

    static {
        RUNTIME = Runtime.getRuntime();
    }

    public static Process exeCommand(String command) {
        try {
            return RUNTIME.exec(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Process exeCommand(String... command) {
        try {
            return RUNTIME.exec(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void exeSuCommand(String cmd) {
        Process process = exeCommand("su");
        if (process == null) {
            return;
        }
        try {
            cmd += "\n";
            OutputStream os = process.getOutputStream();
            os.write(cmd.getBytes());
            os.flush();
            os.write("exit\n".getBytes());
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exeInstallApp(String apkPath) {
        exeSuCommand("pm install -r " + apkPath);
    }

    public static Result exeCommandForResult(String command) {
        return getResultForCommand(command.split(" "));
    }

    public static Result exeCommandForResult(String[] command) {
        return getResultForCommand(command);
    }

    private static Result getResultForCommand(String... command) {
        try {
            Process process = RUNTIME.exec(command);
            int code = process.waitFor();
            int len;
            byte[] buf = null;
            InputStream stream = null;
            if (code == 0) {
                stream = process.getInputStream();
                buf = new byte[stream.available()];
                len = stream.read(buf);
            } else {
                stream = process.getErrorStream();
                buf = new byte[stream.available()];
                len = stream.read(buf);
            }
            process.destroy();
            return new Result(code, new String(buf, 0, len));
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(-1, e.getMessage());
        }
    }

    public static class Result{
        public int code;
        public String msg;

        private Result(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
}
