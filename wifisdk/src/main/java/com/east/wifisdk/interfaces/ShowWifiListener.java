package com.east.wifisdk.interfaces;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import org.json.JSONArray;

import java.util.List;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 * @description:  显示搜索到的wifi监听器
 * @author: East
 * @date: 2019-12-03 11:47
 * |---------------------------------------------------------------------------------------------------------------|
 */
public interface ShowWifiListener {
    void onNetworksFound(
            WifiManager wifiManager,
            List<ScanResult> wifiScanResult
    );

    default void onNetworksFound(JSONArray wifiList){}
    default void errorSearchingNetworks(int errorCode){}
}
