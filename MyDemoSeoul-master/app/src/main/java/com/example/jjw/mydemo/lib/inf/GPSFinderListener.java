package com.example.jjw.mydemo.lib.inf;

/**
 * Created by JJW on 2016-10-27.
 */
public interface GPSFinderListener {
    void onGPSFinderSuccess();              //GPS가 켜져있는 경우 시작
    void onGPSFinderFailed();               //GPS가 꺼져있는 경우
}
