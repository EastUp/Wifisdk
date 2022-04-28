package com.east.wifisdk.interfaces;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 * @description:  移除wifi的监听
 * @author: East
 * @date: 2019-12-03 17:24
 * |---------------------------------------------------------------------------------------------------------------|
 */
public interface RemoveWifiListener {
    enum ErrorType {
        WIFI_NOT_CONFIGURE, WIFI_REMOVE_FAIL
    }

    default void onWifiNetworkRemoved(){}
    default void onWifiNetworkRemoveError(ErrorType reason){}
}
