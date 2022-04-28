package com.east.wifisdk.constant;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *
 *  @description: 和wifi相关的一些常量
 *  @author: jamin
 *  @date: 2020/4/9
 * |---------------------------------------------------------------------------------------------------------------|
 */
public class WifiConstant {
    /**
     * 连接wifi时的状态码
     */
    public static final int SUCCESS_CONNECTION = 2500;
    public static final int ERROR_AUTHENTICATION = 2501;
    public static final int ERROR_NOT_FOUND = 2502;
    public static final int ERROR_SAME_NETWORK = 2503;
    public static final int ERROR_STILL_CONNECTED_TO = 2504;
    public static final int ERROR_WIFI_OPEN = 2505;
    public static final int ERROR_WIFI_CLOSE = 2506;
    public static final int UNKOWN_ERROR = 2506;

    /**
     * 扫描Wifi时的状态码
     */
    public static final int WIFI_NETWORKS_SUCCESS_FOUND = 2600;
    public static final int NO_WIFI_NETWORKS = 2601;

    /**
     * wifi的安全类型
     */
    public static final String SECURITY_WEP = "WEP";
    public static final String SECURITY_WPA = "WPA";
    public static final String SECURITY_PSK = "PSK";
    public static final String SECURITY_EAP = "EAP";
    public static final String SECURITY_NONE = "NONE";
}
