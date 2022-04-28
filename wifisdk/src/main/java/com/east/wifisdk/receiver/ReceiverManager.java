package com.east.wifisdk.receiver;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.east.wifisdk.WifiConnector;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *
 * @description: Wifi广播管理
 * @author: jamin
 * @date: 2020/4/9
 * |---------------------------------------------------------------------------------------------------------------|
 */
public class ReceiverManager {

    //接受wifi的状态
    public WifiStateReceiver mWifiStateReceiver;

    //点击链接wifi时候,wifi是否连接成功的监听
    public WifiConnectionReceiver mWifiConnectionReceiver;

    //扫描wifi的广播
    public ShowWifiListReceiver mShowWifiListReceiver;

    private Context applicationContext;

    private WifiConnector mWifiConnector;

    private static volatile ReceiverManager mInstance;

    public static ReceiverManager getDefault() {
        if (mInstance == null) {
            synchronized (ReceiverManager.class) {
                if (mInstance == null)
                    mInstance = new ReceiverManager();
            }
        }
        return mInstance;
    }

    private ReceiverManager() {
    }

    public void init(Context context, WifiConnector connector) {
        applicationContext = context.getApplicationContext();
        mWifiConnector = connector;
        createAndRegisteWifiStateBroadcast();
        createAndRegisteWifiConnectionBroadcast();
        createAndRegisteShowWifiListBroadcast();
    }

    private void createAndRegisteWifiStateBroadcast() {
        IntentFilter wifiStateFilter = new IntentFilter();
        wifiStateFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiStateFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWifiStateReceiver = new WifiStateReceiver(mWifiConnector);
        try {
            applicationContext.getApplicationContext().registerReceiver(mWifiStateReceiver, wifiStateFilter);
        } catch (Exception e) {
            mWifiConnector.wifiLog("Exception on registering broadcast for listening Wifi State: " + e.toString());
        }
    }

    private void createAndRegisteWifiConnectionBroadcast() {
        IntentFilter chooseWifiFilter = new IntentFilter();
        chooseWifiFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mWifiConnectionReceiver = new WifiConnectionReceiver(mWifiConnector);
        try {
            applicationContext.getApplicationContext().registerReceiver(mWifiConnectionReceiver, chooseWifiFilter);
        } catch (Exception e) {
            mWifiConnector.wifiLog("Register broadcast error (Choose): " + e.toString());
        }
    }

    private void createAndRegisteShowWifiListBroadcast() {
        IntentFilter showWifiListFilter = new IntentFilter();
        showWifiListFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mShowWifiListReceiver = new ShowWifiListReceiver(mWifiConnector);
        try {
            applicationContext.getApplicationContext().getApplicationContext().registerReceiver(mShowWifiListReceiver, showWifiListFilter);
        } catch (Exception e) {
            mWifiConnector.wifiLog("Register broadcast error (ShowWifi): " + e.toString());
        }
    }

    /**
     * 注销所有广播
     */
    public void destroy() {
        if (applicationContext == null)
            throw new RuntimeException("you never called init method");

        mWifiConnector.wifiLog("Unregistering wifi listener(s)");
        try {
            if (mWifiStateReceiver != null)
                applicationContext.getApplicationContext().unregisterReceiver(mWifiStateReceiver);
            if (mWifiConnectionReceiver != null)
                applicationContext.getApplicationContext().unregisterReceiver(this.mWifiConnectionReceiver);
            if (mShowWifiListReceiver != null)
                applicationContext.getApplicationContext().unregisterReceiver(mShowWifiListReceiver);
        } catch (Exception e) {
            mWifiConnector.wifiLog("Error unregistering Wifi Remove Listener because may be it was never registered");
        }
        applicationContext = null;
        mWifiConnector = null;
    }
}
