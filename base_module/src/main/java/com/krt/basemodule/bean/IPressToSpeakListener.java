package com.krt.basemodule.bean;

public interface IPressToSpeakListener {
    void onPressed();
    void onReleased(boolean isCanceled, PressRecordResult recordResult);
    void onWantToCanceled(boolean wantToCancel);
    void onRecording(int seconds);
}
