package com.east.wifisdk.interfaces;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 * @description:  wifi开启关闭状态的监听器
 * @author: East
 * @date: 2019-12-03 11:41
 * |---------------------------------------------------------------------------------------------------------------|
 */
public interface WifiStateListener {
    default void onStateChange(int wifiState){}
    default void onWifiEnabled(){}
    default void onWifiEnabling(){}
    default void onWifiDisabling(){}
    default void onWifiDisabled(){}
    default void onWifiConnected(){}
    default void onWifiDisconnected(){}
    default void onWifiConnecting(){}
    default void onWifiDisconnecting(){}
}
