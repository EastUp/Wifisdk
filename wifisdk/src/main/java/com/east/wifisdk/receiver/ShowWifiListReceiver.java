/*
 * Created by Jose Flavio on 10/18/17 12:57 PM. 
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package com.east.wifisdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;


import com.east.wifisdk.WifiConnector;
import com.east.wifisdk.constant.WifiConstant;
import com.east.wifisdk.interfaces.ShowWifiListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *  @description:  扫描的回调
 *  @author: East
 *  @date: 2019-12-04 10:35
 * |---------------------------------------------------------------------------------------------------------------|
 */
public class ShowWifiListReceiver extends BroadcastReceiver {
    
    private WifiConnector mWifiConnector;

    public ShowWifiListReceiver(WifiConnector wifiConnector) {
        this.mWifiConnector = wifiConnector;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ShowWifiListener showWifiListener = mWifiConnector.getShowWifiListener();

        final JSONArray wifiList = new JSONArray();
        List<ScanResult> wifiScanResult = mWifiConnector.getWifiManager().getScanResults();
        if(wifiScanResult == null)
            return;
        int scanSize = wifiScanResult.size();

        wifiLog("Showwifireceiver action:  " + intent.getAction());

        try {
            scanSize--;
            wifiLog("Scansize: " + scanSize);
            if (scanSize >= 0) {
               onNetworksFound(showWifiListener, mWifiConnector.getWifiManager(), wifiScanResult);
                while (scanSize >= 0) {
                    if (!wifiScanResult.get(scanSize).SSID.isEmpty()) {
                        /**
                         * individual wifi item information
                         */
                        JSONObject wifiItem = new JSONObject();

                        wifiItem.put("SSID", wifiScanResult.get(scanSize).SSID);
                        wifiItem.put("BSSID", wifiScanResult.get(scanSize).BSSID);
                        wifiItem.put("INFO", wifiScanResult.get(scanSize).capabilities);

                        /**
                         * this check if device has a current WiFi connection
                         */
                        if (wifiScanResult.get(scanSize).BSSID.equals(mWifiConnector.getWifiManager().getConnectionInfo().getBSSID())) {
                            wifiItem.put("CONNECTED", true);
                            mWifiConnector.setCurrentWifiSSID(wifiScanResult.get(scanSize).SSID);
                            mWifiConnector.setCurrentWifiBSSID(wifiScanResult.get(scanSize).BSSID);
                        } else {
                            wifiItem.put("CONNECTED", false);
                        }
                        wifiItem.put("SECURITY_TYPE", WifiConnector.getWifiSecurityType(wifiScanResult.get(scanSize)));
                        wifiItem.put("LEVEL", WifiManager.calculateSignalLevel(wifiScanResult.get(scanSize).level, 100) + "%");

                        wifiList.put(wifiItem);
                    }

                    scanSize--;
                }

                onNetworksFound(showWifiListener,wifiList);

            } else {
                errorSearchingNetworks(showWifiListener, WifiConstant.NO_WIFI_NETWORKS);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            errorSearchingNetworks(showWifiListener,WifiConstant.UNKOWN_ERROR);
        }

    }

    private void wifiLog(String text) {
        if (this.mWifiConnector.isLogOrNot()) Log.d(WifiConnector.TAG, "ShowWifiListListener: " + text);
    }


    private void onNetworksFound(ShowWifiListener showWifiListener, WifiManager wifiManager, List<ScanResult> wifiScanResult){
        if(showWifiListener!=null)
            showWifiListener.onNetworksFound(wifiManager,wifiScanResult);
    }
    private void onNetworksFound(ShowWifiListener showWifiListener, JSONArray wifiList){
        if(showWifiListener!=null)
            showWifiListener.onNetworksFound(wifiList);
    }
    private void errorSearchingNetworks(ShowWifiListener showWifiListener,int errorCode){
        if(showWifiListener!=null)
            showWifiListener.errorSearchingNetworks(errorCode);
    }

}