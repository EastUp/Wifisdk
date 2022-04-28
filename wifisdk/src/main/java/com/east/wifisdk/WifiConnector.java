package com.east.wifisdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.east.wifisdk.interfaces.ConnectionResultListener;
import com.east.wifisdk.interfaces.RemoveWifiListener;
import com.east.wifisdk.interfaces.ShowWifiListener;
import com.east.wifisdk.interfaces.WifiStateListener;
import com.east.wifisdk.receiver.ReceiverManager;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.east.wifisdk.utils.SsidUtils.ssidFormat;
import static com.east.wifisdk.utils.SsidUtils.trimQuotes;
import static com.east.wifisdk.constant.WifiConstant.*;


/**
 * |---------------------------------------------------------------------------------------------------------------|
 *
 *  @description: 工具类
 *  @author: East
 *  @date: 2019-12-04 11:15
 * |---------------------------------------------------------------------------------------------------------------|
 */
public class WifiConnector {

    public static final String TAG = WifiConnector.class.getName();

    //是否打印日志
    private boolean logOrNot = false;

    //Context的软引用
    private static SoftReference<Context> mContextRefrence;

    //WifiConfiguration对象将包含访问点信息
    private WifiConfiguration mWifiConfiguration;

    //WifiManager对象来管理wifi连接
    private WifiManager mWifiManager;

    //wifi状态监听
    private WifiStateListener mWifiStateListener;

    //wifi是否连接的监听
    private ConnectionResultListener mConnectionResultListener;

    //扫描到的wifi监听
    private ShowWifiListener mShowWifiListener;

    //删除网络的监听
    private RemoveWifiListener mRemoveWifiListener;

    //wifi的以及连接过的配置
    private static List<WifiConfiguration> mConfList;

    //当前连接的Wi-Fi网络的字符串值
    private String mCurrentWifiSSID = null;

    //可以从任何地方获取静态值
    public static String CURRENT_WIFI = null;

    //当前连接的Wi-Fi网络的mac地址
    private String mCurrentWifiBSSID = null;

    private volatile static WifiConnector mInstance;

    public static WifiConnector getInstance(){
        if(mInstance == null){
            synchronized (WifiConnector.class){
                if(mInstance == null)
                    mInstance = new WifiConnector();
            }
        }
        return mInstance;
    }

    private WifiConnector() { }

    /**
     * 初始化
     */
    public void init(Context context){
        mContextRefrence = new SoftReference<>(context);
        this.mWifiManager = (WifiManager) mContextRefrence.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (isWifiEnbled() && isWiFiConnected()) {
            setCurrentWifiInfo();
        }
        //注册广播
        ReceiverManager.getDefault().init(mContextRefrence.get(),this);
    }

    /**
     *  销毁
     */
    public void destroy(){
        ReceiverManager.getDefault().destroy();
        if(mContextRefrence != null){
            mContextRefrence.clear();
            mContextRefrence = null;
        }
        mInstance = null;
    }

    public WifiStateListener getWifiStateListener() {
        return mWifiStateListener;
    }

    public void setWifiStateListener(WifiStateListener wifiStateListener) {
        mWifiStateListener = wifiStateListener;
    }

    public ConnectionResultListener getConnectionResultListener() {
        return mConnectionResultListener;
    }

    public void setConnectionResultListener(ConnectionResultListener connectionResultListener) {
        this.mConnectionResultListener = connectionResultListener;
    }

    public ShowWifiListener getShowWifiListener() {
        return mShowWifiListener;
    }

    public void setShowWifiListener(ShowWifiListener showWifiListener) {
        mShowWifiListener = showWifiListener;
    }

    public RemoveWifiListener getRemoveWifiListener() {
        return mRemoveWifiListener;
    }

    public void setRemoveWifiListener(RemoveWifiListener removeWifiListener) {
        mRemoveWifiListener = removeWifiListener;
    }

    public WifiConfiguration getWifiConfiguration() {
        return mWifiConfiguration;
    }

    public void setWifiConfiguration(WifiConfiguration wifiConfiguration) {
        mWifiConfiguration = wifiConfiguration;
    }

    // endregion

    /**
     * 启用wifi
     * 如果要监听wifi状态
     * 回调以更新应用程序的用户界面。
     *
     * @return 此WifiConnector对象可在任何寄存器回调方法中使用。
     */
    public Boolean enableWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            wifiLog("Wifi enabled, determining current connected wifi network if there is one...");
            return mWifiManager.setWifiEnabled(true);
        } else {
            wifiLog("Wifi is already enable...");
            return true;
        }
    }


    /**
     * 禁用wifi
     * 回调以更新应用程序上的用户界面
     */
    public Boolean disableWifi() {
        if (mWifiManager.isWifiEnabled()) {
            return mWifiManager.setWifiEnabled(false);
        } else {
            wifiLog("Wifi is not enable...");
            return true;
        }
    }

    /**
     * wifi是否打开
     *
     * @return true 表示wifi打开
     */
    public boolean isWifiEnbled() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * 这个Wifi是否能扫描到
     * @param SSID
     * @return
     */
    public boolean isWifiCanScan(String SSID){
        mWifiManager.startScan();
        List<ScanResult> mWifiList=mWifiManager.getScanResults();
        if(mWifiList==null){
            return false;
        }
        for(ScanResult existingWifi:mWifiList){
            if(existingWifi.SSID.equals(SSID)){
                Log.v("test","扫描到热点:"+SSID);
                return true;
            }
        }
        Log.v("test","扫描不到热点:"+SSID);
        return false;
    }

    private void setCurrentWifiInfo() {
        setCurrentWifiSSID(mWifiManager.getConnectionInfo().getSSID());
        setCurrentWifiBSSID(mWifiManager.getConnectionInfo().getBSSID());
    }

    public String getCurrentWifiSSID() {
        return mCurrentWifiSSID;
    }

    public void setCurrentWifiSSID(String currentWifiSSID) {
        this.mCurrentWifiSSID = currentWifiSSID;
        CURRENT_WIFI = mCurrentWifiSSID;
    }

    public String getCurrentWifiBSSID() {
        return mCurrentWifiBSSID;
    }

    public void setCurrentWifiBSSID(String currentWifiBSSID) {
        this.mCurrentWifiBSSID = currentWifiBSSID;
    }

    public boolean setPriority(int priority) {
        try {
            this.mWifiConfiguration.priority = priority;
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public WifiManager getWifiManager() {
        return mWifiManager;
    }

    public void setWifiManager(WifiManager wifiManager) {
        this.mWifiManager = wifiManager;
    }

    /**
     * 尝试扫描可用的wifi网络。获得结果（或未获得结果）后，
     */
    public void scanAndShowWifiList(ShowWifiListener showWifiListener) {
        wifiLog("show wifi list");
        mShowWifiListener = showWifiListener;
        scanWifiNetworks();
    }

    private void scanWifiNetworks() {
        mWifiManager.startScan();
    }

    public void setScanResult(ScanResult scanResult, String password) {
        setWifiConfiguration(scanResult.SSID, scanResult.BSSID, getWifiSecurityType(scanResult), password);
    }

    /**
     * 判断 这个ssid是否已经连接过了
     *
     * @return
     */
    public WifiConfiguration getSavedSSIDWifiConfiguration(String SSID) {
        //注意：在低版本可以移除，在6.0以上如果是自己的应用之前连接过，通过removeWifi()可以移除
        //但是 如果是其他应用的configrRation 那么在本应用是不可以移除的 此时 直接连接即可
        //因此会做一个判断，判断removeWifi 后集合的数量是否减少，若没有则表示没有移除成功，直接连接即可
        WifiConfiguration tempConfig = this.IsExsits(SSID);
        List<WifiConfiguration> beforeConfig = mWifiManager.getConfiguredNetworks();
        if (tempConfig != null) {
            return tempConfig;
        } else {
            return null;
        }
    }

    /**
     * 是否之前连接过这个Wifi
     */
    private WifiConfiguration IsExsits(String SSID) {
        if (!SSID.startsWith("\""))
            SSID = ssidFormat(SSID);
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals(SSID)) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    public boolean isAlreadyConnected(String BSSID) {
        ConnectivityManager connManager = (ConnectivityManager) mContextRefrence.get().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getActiveNetworkInfo();

        wifiLog("isAlreadyConnected: " + mWifiManager.getConnectionInfo().getBSSID() + " " + BSSID);

        if (mWifi != null && mWifi.getType() == ConnectivityManager.TYPE_WIFI && mWifi.isConnected()) {
            return isConnectedToBSSID(BSSID);
        } else {
            wifiLog("getActiveNetwork - NetworkInfo is null");
        }
        return false;
    }

    /**
     * 是否已经连接上 BSSID为"${BSSID}" 的wifi
     *
     * @param BSSID
     * @return
     */
    public boolean isConnectedToBSSID(String BSSID) {
        if (TextUtils.isEmpty(BSSID))
            return false;
        if (mWifiManager.getConnectionInfo().getBSSID() != null &&
                mWifiManager.getConnectionInfo().getBSSID().equals(BSSID)) {
            wifiLog("Already connected to: " + mWifiManager.getConnectionInfo().getSSID() +
                    "  BSSID: " + mWifiManager.getConnectionInfo().getBSSID() + "  " + BSSID);
            return true;
        }
        return false;
    }

    /**
     * 是否已经连接上 BSSID为"${BSSID}" 的wifi
     *
     * @param SSID
     * @return
     */
    public boolean isConnectedToSSID(String SSID) {
        if (TextUtils.isEmpty(SSID))
            return false;
        if (!SSID.startsWith("\""))
            SSID = ssidFormat(SSID);
        String connectedSSID = mWifiManager.getConnectionInfo().getSSID();
        if (connectedSSID != null)
            wifiLog("connected:" + connectedSSID);
        if (connectedSSID != null &&
                connectedSSID.equals(SSID)) {
            wifiLog("Already connected to: " + connectedSSID +
                    "  SSID: " + mWifiManager.getConnectionInfo().getBSSID() + "  " + SSID);
            return true;
        }
        return false;
    }

    /**
     * c
     * 尝试连接到构造函数上设置的特定wifi。
     *
     * @param connectionResultListener 连接的结果回调
     */
    public void connectToWifi(ConnectionResultListener connectionResultListener) {
        this.mConnectionResultListener = connectionResultListener;
        connectToWifi();
    }

    public void connectToWifi(String ssid, String securityType, String pwd, ConnectionResultListener connectionResultListener) {
        setWifiConfiguration(ssid, null, securityType, pwd);
        connectToWifi(connectionResultListener);
    }

    public void connectToWifi(String ssid, String securityType, String pwd) {
        setWifiConfiguration(ssid, null, securityType, pwd);
        connectToWifi();
    }

    /**
     * 连接配置信息是mWifiConfiguration的wifi
     */
    public void connectToWifi() {
        if (!isWifiEnbled()) {
            if (!enableWifi()) {
                if (mConnectionResultListener != null)
                    mConnectionResultListener.errorConnect(ERROR_WIFI_OPEN);
                return;
            }
            long timeMills = System.currentTimeMillis();
            while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                try {
                    if (System.currentTimeMillis() - timeMills > 2500) break;
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (isWiFiConnected() && (isConnectedToBSSID(mWifiConfiguration.BSSID) || isConnectedToSSID(mWifiConfiguration.SSID))) {
            if (mConnectionResultListener != null)
                mConnectionResultListener.errorConnect(ERROR_SAME_NETWORK);
        } else {
            String bssid = mWifiManager.getConnectionInfo().getBSSID();
            if (bssid != null && mCurrentWifiBSSID != null && !mCurrentWifiBSSID.equals(bssid)) {
                setCurrentWifiSSID(mWifiManager.getConnectionInfo().getSSID());
                setCurrentWifiBSSID(bssid);
                wifiLog("Already connected to: " + mWifiManager.getConnectionInfo().getSSID() + " " +
                        "Now trying to connect to " + mWifiConfiguration.SSID);
            }
            connectToWifiAccesPoint();
        }
    }

    /**
     * 设置wifi的配置
     *
     * @param SSID         一定得是"开头的字符串,没有的话就得自己添加上去
     * @param BSSID
     * @param securityType
     * @param password
     */
    public void setWifiConfiguration(String SSID, String BSSID, String securityType, String password) {
        WifiConfiguration savedSSIDWifiConfiguration = getSavedSSIDWifiConfiguration(SSID);
        if (savedSSIDWifiConfiguration != null)
            mWifiConfiguration = savedSSIDWifiConfiguration;
        else {
            this.mWifiConfiguration = new WifiConfiguration();
            if (!SSID.startsWith("\""))
                this.mWifiConfiguration.SSID = ssidFormat(SSID);
            else
                this.mWifiConfiguration.SSID = SSID;
            this.mWifiConfiguration.BSSID = BSSID;


            if (securityType.equals(SECURITY_NONE)) {
                mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            } else {
                mWifiConfiguration.preSharedKey = ssidFormat(password);
                mWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                mWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
                mWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
                mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                mWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                mWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            }

//            if (securityType.equals(SECURITY_NONE)) {
//                mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            }else if(securityType.equals(SECURITY_WEP)){ //WIFICIPHER_WEP
//                mWifiConfiguration.hiddenSSID = true;
//                mWifiConfiguration.wepKeys[0]= "\""+password+"\"";
//                mWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//                mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//                mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//                mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//                mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//                mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//                mWifiConfiguration.wepTxKeyIndex = 0;
//            }else if(securityType.equals(SECURITY_WPA)){ //WIFICIPHER_WPA
//                mWifiConfiguration.preSharedKey = "\""+password+"\"";
//                mWifiConfiguration.hiddenSSID = true;
//                mWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//                mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//                mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//                mWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//                //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//                mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//                mWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//                mWifiConfiguration.status = WifiConfiguration.Status.ENABLED;
//            }
        }
    }

    /**
     * 允许连接到特定的Wifi接入点。
     * 尝试获取网络ID 如果网络ID = -1，则添加网络配置
     *
     * @return true:连接成功,false:连接失败
     */
    private boolean connectToWifiAccesPoint() {
        int networkId = getNetworkId(mWifiConfiguration.SSID);
        wifiLog("network id found: " + networkId);
        if (networkId == -1) {
            networkId = mWifiManager.addNetwork(mWifiConfiguration);
            wifiLog("networkId now: " + networkId);
        }
        return enableNetwork(networkId);
    }

    private boolean enableNetwork(int networkId) {
        if (networkId == -1) {
            wifiLog("So networkId still -1, there was an error... may be authentication?");
            if (mConnectionResultListener != null)
                mConnectionResultListener.errorConnect(ERROR_AUTHENTICATION);
            return false;
        }
        return connectWifiManager(networkId);
    }

    private boolean connectWifiManager(int networkId) {
        if (isWiFiConnected())
            mWifiManager.disconnect();
        return mWifiManager.enableNetwork(networkId, true);
    }

    /**
     * 通过给定的SSID搜索网络ID。
     * 如果网络是全新的，则返回-1。
     *
     * @param SSID wifi 的名称
     * @return wifi 网络 id
     */
    private int getNetworkId(String SSID) {
        mConfList = mWifiManager.getConfiguredNetworks();
        if (mConfList != null && mConfList.size() > 0) {
            for (WifiConfiguration existingConfig : mConfList) {
                if (trimQuotes(existingConfig.SSID).equals(trimQuotes(SSID))) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }

    public void removeCurrentWifiNetwork(RemoveWifiListener removeWifiListener) {
        removeWifiNetwork(getCurrentWifiSSID(), removeWifiListener);
    }

    public void removeWifiNetwork(WifiConfiguration wifiConfiguration, RemoveWifiListener removeWifiListener) {
        removeWifiNetwork(wifiConfiguration.SSID, removeWifiListener);
    }

    public void removeWifiNetwork(ScanResult scanResult, RemoveWifiListener removeWifiListener) {
        removeWifiNetwork(scanResult.SSID, removeWifiListener);
    }

    public void removeWifiNetwork(String SSID, RemoveWifiListener removeWifiListener) {
        this.mRemoveWifiListener = removeWifiListener;
        removeWifiNetwork(SSID);
    }

    /**
     * 移除已经连接过的wifi
     * 应用程序不允许删除由其他应用程序创建的网络。注意必须要是系统的app才能删除 解决方案->把app变成系统app，并赋予system权限
     * https://blog.csdn.net/weixin_36001685/article/details/101543430
     *
     * @param SSID
     */
    private void removeWifiNetwork(String SSID) {
        List<WifiConfiguration> list1 = mWifiManager.getConfiguredNetworks();
        if (list1 != null && list1.size() > 0) {
            boolean findWifi = false;
            for (WifiConfiguration i : list1) {
                try {
                    if (trimQuotes(SSID).equals(trimQuotes(i.SSID))) {
                        findWifi = true;
                        mWifiManager.disableNetwork(i.networkId);
                        if (trimQuotes(mCurrentWifiSSID).equals(trimQuotes(i.SSID))) {
                            mWifiManager.disconnect();
                        }
                        if (mWifiManager.removeNetwork(i.networkId)) {
                            wifiLog("Network deleted: " + i.networkId + " " + i.SSID);
                            mRemoveWifiListener.onWifiNetworkRemoved();
                        } else {
                            mRemoveWifiListener.onWifiNetworkRemoveError(RemoveWifiListener.ErrorType.WIFI_REMOVE_FAIL);
                        }
                        mWifiManager.saveConfiguration();
                        break;
                    }
                } catch (NullPointerException e) {
                    wifiLog("Exception on removing wifi network: " + e.toString());
                }
            }
            if (!findWifi) {
                wifiLog("没有找到指定wifi");
                mRemoveWifiListener.onWifiNetworkRemoveError(RemoveWifiListener.ErrorType.WIFI_NOT_CONFIGURE);
            }
        } else {
            wifiLog("Empty Wifi List");
            mRemoveWifiListener.onWifiNetworkRemoveError(RemoveWifiListener.ErrorType.WIFI_NOT_CONFIGURE);
        }
    }

    /**
     * ForgetNetwork是仅在应用程序已签名并作为系统运行时才有效的方法。
     * 它将在WifiManager类上寻找“忘记”隐藏方法。
     *
     * @param wifiManager current mWifiManager
     * @param i           要删除的WifiConfiguration
     * @hide
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void forgetWifiNetwork(WifiManager wifiManager, WifiConfiguration i) {
        try {
            Method[] methods = wifiManager.getClass().getDeclaredMethods();
            Method forgetMEthod = null;
            for (Method method : methods) {
                if (method.getName().contains("forget")) {
                    forgetMEthod = method;
                    forgetMEthod.invoke(wifiManager, i.networkId, null);
                    wifiLog("Forgotten network " + i.SSID);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            wifiLog("Exception: " + e.toString());
        }
    }

    /**
     * 与{@link #forgetWifiNetwork（WifiManager，WifiConfiguration）}相似，
     * 但是它将与作为用户应用程序安装的任何应用程序一起运行，并且只会删除自己创建的wifi配置。
     *
     * @return true 如果删除配置成功
     */
    public boolean deleteWifiConf() {
        try {
            mConfList = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : mConfList) {
                if (mWifiConfiguration.SSID != null && i.SSID != null && i.SSID.equals(mWifiConfiguration.SSID)) {
                    wifiLog("Deleting wifi configuration: " + i.SSID);
                    mWifiManager.removeNetwork(i.networkId);
                    return mWifiManager.saveConfiguration();
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    /**
     * 断开指定的WiFi
     *
     * @param targetSsid
     */
    public void disconnectWifi(String targetSsid) {
        if (!targetSsid.startsWith("\""))
            targetSsid = ssidFormat(targetSsid);
        List<WifiConfiguration> wifiConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfig : wifiConfigs) {
            String ssid = wifiConfig.SSID;
            if (ssid.equals(targetSsid)) {
                disconnectWifi(wifiConfig.networkId);
            }
        }
    }

    /**
     * 断开指定ID的网络
     *
     * @param netId
     */
    private void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    /**
     * 断开wifi
     */
    private void disconnectWifi() {
        mWifiManager.disconnect();
    }


    public void setLog(boolean log) {
        this.logOrNot = log;
    }

    public boolean isLogOrNot() {
        return logOrNot;
    }

    /**
     * 获取wifi加密方式
     *
     * @param result
     * @return
     */
    public static String getWifiSecurityType(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("WPA")) {
            return SECURITY_WPA;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public void wifiLog(String text) {
        if (logOrNot) Log.d(TAG, "WifiConnector: " + text);
    }


    /**
     * 获取wifi是否连接
     *
     * @return
     */
    public boolean isWiFiConnected() {
        ConnectivityManager connectManager = (ConnectivityManager) mContextRefrence.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

}
