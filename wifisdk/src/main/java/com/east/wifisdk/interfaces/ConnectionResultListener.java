package com.east.wifisdk.interfaces;

import android.net.wifi.SupplicantState;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 * @description:  自己wifi请求结果的监听器
 * @author: East
 * @date: 2019-12-03 11:40
 * |---------------------------------------------------------------------------------------------------------------|
 */
public interface ConnectionResultListener {
    default void successfulConnect(String SSID){}
    default void errorConnect(int codeReason){}
    default void onStateChange(SupplicantState supplicantState){}
}
