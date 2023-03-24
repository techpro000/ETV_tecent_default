package com.etv.util.gpio;

public interface GpioInfoBackListener {
    void openFailed(String desc);

    void backGpioStringInfoAction(boolean ifHasPerson);
}