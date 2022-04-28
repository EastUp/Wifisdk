/*
 * Created by Jose Flavio on 10/18/17 12:49 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */
package com.east.wifisdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.east.wifisdk.WifiConnector;
import com.east.wifisdk.constant.WifiConstant;
import com.east.wifisdk.interfaces.ConnectionResultListener;


/**
 * |---------------------------------------------------------------------------------------------------------------|
 *  @description:  连接wifi的广播
 *  @author: East
 *  @date: 2019-12-03 16:05
 * |---------------------------------------------------------------------------------------------------------------|
 */
public class WifiConnectionReceiver extends BroadcastReceiver {

    private WifiConnector mWifiConnector;

    public WifiConnectionReceiver(WifiConnector wifiConnector) {
        this.mWifiConnector = wifiConnector;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        ConnectionResultListener connectionResultListener = mWifiConnector.getConnectionResultListener();

        String action = intent.getAction();
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {

            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

            wifiLog("Connection state: " + state);

            onStateChange(connectionResultListener,state);

            switch (state) {
                case COMPLETED:
                    wifiLog("Connection to Wifi was successfully completed...\n" +
                            "Connected to BSSID: " + mWifiConnector.getWifiManager().getConnectionInfo().getBSSID() +
                            " And SSID: " + mWifiConnector.getWifiManager().getConnectionInfo().getSSID());
                    if (mWifiConnector.getWifiManager().getConnectionInfo().getBSSID() != null) {
                        mWifiConnector.setCurrentWifiSSID(mWifiConnector.getWifiManager().getConnectionInfo().getSSID());
                        mWifiConnector.setCurrentWifiBSSID(mWifiConnector.getWifiManager().getConnectionInfo().getBSSID());
                        successfulConnect(connectionResultListener, mWifiConnector.getCurrentWifiSSID());
                    }
                    // if BSSID is null, may be is still triying to get information about the access point
                    break;

                case DISCONNECTED:
                    int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    wifiLog("Disconnected... Supplicant error: " + supl_error);

                    // only remove broadcast listener if error was ERROR_AUTHENTICATING
                    if (supl_error == WifiManager.ERROR_AUTHENTICATING) {
                        wifiLog("Authentication error...");
                        if (mWifiConnector.deleteWifiConf()) {
                            errorConnect(connectionResultListener, WifiConstant.ERROR_AUTHENTICATION);
                        } else {
                            errorConnect(connectionResultListener,WifiConstant.UNKOWN_ERROR);
                        }
                    }
                    break;

                case AUTHENTICATING:
                    wifiLog("Authenticating...");
                    break;
            }

        }
    }

    private void wifiLog(String text) {
        if (mWifiConnector.isLogOrNot()) Log.d(WifiConnector.TAG, "ConnectionReceiver: " + text);
    }

    private void successfulConnect(ConnectionResultListener connectionResultListener, String SSID){
        if(connectionResultListener!=null)
            connectionResultListener.successfulConnect(SSID);
    }
    private void errorConnect(ConnectionResultListener connectionResultListener,int codeReason){
        if(connectionResultListener!=null)
            connectionResultListener.errorConnect(codeReason);

    }
    private void onStateChange(ConnectionResultListener connectionResultListener, SupplicantState supplicantState){
        if(connectionResultListener!=null)
            connectionResultListener.onStateChange(supplicantState);
    }

}