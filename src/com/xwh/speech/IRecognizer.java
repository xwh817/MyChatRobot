package com.xwh.speech;

/**
 * Created by Administrator on 2016/3/15.
 */
public interface IRecognizer {
    void startSpeech();
    void stopListening();
    boolean isListening();
}
