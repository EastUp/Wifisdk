/*
 * Created by Jose Flavio on 10/18/17 12:55 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package com.east.wifisdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.east.wifisdk.WifiConnector;
import com.east.wifisdk.interfaces.WifiStateListener;


/**
 * |---------------------------------------------------------------------------------------------------------------|
 *  @description:  wifi状态监听广播
 *  @author: East
 *  @date: 2019-12-04 10:37
 * |---------------------------------------------------------------------------------------------------------------|
 */
public class WifiStateReceiver extends BroadcastReceiver {

    private WifiConnector mWifiConnector;

    private int mWifiState;
    private NetworkInfo.State mNetWorkState;

    public WifiStateReceiver(WifiConnector wifiConnector) {
        this.mWifiConnector = wifiConnector;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiStateListener wifiStateListener = mWifiConnector.getWifiStateListener();

        String action = intent.getAction();

        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

            if(mWifiState == wifiState)
                return;
            mWifiState = wifiState;

            onStateChange(wifiStateListener,wifiState);

            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLED:
                    wifiLog("Wifi 已开启");
                    onWifiEnabled(wifiStateListener);
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    wifiLog("正在开启 wifi");
                    onWifiEnabling(wifiStateListener);
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    wifiLog("正在关闭 wifi");
                    onWifiDisabling(wifiStateListener);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    wifiLog("Wifi 已关闭");
                    onWifiDisabled(wifiStateListener);
                    break;

            }
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            wifiLog("网络状态改变");
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info == null)
                return;

            NetworkInfo.State state = info.getState();
            if(mNetWorkState == state)
                return;
            mNetWorkState = state;

            if (state.equals(NetworkInfo.State.DISCONNECTED)) {// 如果断开连接
                wifiLog("wifi网络连接断开 ");
                onWifiDisconnected(wifiStateListener);
            }

            if (state.equals(NetworkInfo.State.CONNECTED)) {
                wifiLog("连接到wifi网络");
                onWifiConnected(wifiStateListener);
            }

            if (state.equals(NetworkInfo.State.CONNECTING)) {
                wifiLog("正在连接wifi");
                onWifiConnecting(wifiStateListener);
            }

            if (state.equals(NetworkInfo.State.DISCONNECTING)) {
                wifiLog("正在断开wifi");
                onWifiDisconnecting(wifiStateListener);
            }

        }


    }

    private void wifiLog(String text) {
        if (mWifiConnector.isLogOrNot()) Log.d(WifiConnector.TAG, "WifiStateReceiver: " + text);
    }

    private void onStateChange(WifiStateListener wifiStateListener,int wifiState){
        if (wifiStateListener != null)
            wifiStateListener.onStateChange(wifiState);
    }

    private void onWifiEnabled(WifiStateListener wifiStateListener){
        if (wifiStateListener != null)
            wifiStateListener.onWifiEnabled();
    }

    private void onWifiEnabling(WifiStateListener wifiStateListener){
        if (wifiStateListener != null)
            wifiStateListener.onWifiEnabling();
    }

    private void onWifiDisabling(WifiStateListener wifiStateListener){
        if (wifiStateListener != null)
            wifiStateListener.onWifiDisabling();
    }

    private void onWifiDisabled(WifiStateListener wifiStateListener){
        if (wifiStateListener != null)
            wifiStateListener.onWifiDisabled();
    }

    private void onWifiConnected(WifiStateListener wifiStateListener){
        if (wifiStateListener != null)
            wifiStateListener.onWifiConnected();
    }

    private void onWifiDisconnected(WifiStateListener wifiStateListener){
        if (wifiStateListener != null)
            wifiStateListener.onWifiDisconnected();
    }

    private void onWifiConnecting(WifiStateListener wifiStateListener){
        if (wifiStateListener != null)
            wifiStateListener.onWifiConnecting();
    }

    private void onWifiDisconnecting(WifiStateListener wifiStateListener){
        if (wifiStateListener != null)
            wifiStateListener.onWifiDisconnecting();
    }

}