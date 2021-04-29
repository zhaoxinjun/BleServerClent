package com.zxj.bleserver.ble;

import android.bluetooth.BluetoothDevice;

/**
 * @Package: com.zxj.bleserver.ble
 * @CreateDate: 2021/4/17 5:17 PM
 * @ClassName: DeviceCallback
 * @Author: zxj
 * @Description: java类作用描述
 */
public interface DeviceCallback {
    void result(BluetoothDevice device, int rssi);
}
