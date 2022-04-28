package com.east.wifisdk.utils;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *
 * @description:
 * @author: jamin
 * @date: 2020/4/9
 * |---------------------------------------------------------------------------------------------------------------|
 */
public class SsidUtils {

    /**
     *  去掉扫描出来 ssid 自带的引号
     * @param str
     * @return
     */
    public  static String trimQuotes(String str) {
        if (!str.isEmpty()) {
            return str.replaceAll("^\"*", "").replaceAll("\"*$", "");
        }
        return str;
    }

    /**
     * 字符串前后添加引号跟NetworkInfo返回的ssid格式就一样了
     * @param str
     * @return
     */
    public static String ssidFormat(String str) {
        if (!str.isEmpty()) {
            return "\"" + str + "\"";
        }
        return str;
    }
}
