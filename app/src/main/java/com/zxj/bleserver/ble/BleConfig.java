package com.zxj.bleserver.ble;

import java.util.UUID;

/**
 * @Package: com.zxj.bleserver.ble
 * @CreateDate: 2021/4/17 5:15 PM
 * @ClassName: BleConfig
 * @Author: zxj
 * @Description: java类作用描述
 */
public class BleConfig {
    public static final String TAG = "zz";
    //设备名称
    public static  String DEVICE_NAME = "HUAWEI VIE-AL10";//"HUAWEI Mate 20";//"OlaBleService123";
    //服务uuid
    public static UUID UUID_SERVER = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    //读的特征值
    public static UUID UUID_CHAR_READ = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb");
    //写的特征值
    public static UUID UUID_CHAR_WRITE = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");

    public static final String MSG_TOP = "1";
    public static final String MSG_BOTTOM = "2";
    public static final String MSG_LEFT = "3";
    public static final String MSG_RIGHT = "4";
    public static final String MSG_PAUSE = "5";
    public static final String MSG_RESUME = "6";
}
