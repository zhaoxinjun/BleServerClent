package com.zxj.bleserver.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * @Package: com.zxj.bleserver.ble
 * @CreateDate: 2021/4/17 5:17 PM
 * @ClassName: BLEManager
 * @Author: zxj
 * @Description: java类作用描述
 */
public class BLEManager {

    private static final String TAG = "BLEManager";
    private static BLEManager bleManager;
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private DeviceCallback callback;
    private BluetoothManager bluetoothManager;
    public BluetoothGatt bluetoothGatt;
    private Handler handler;

    private BLEManager(Context context) {
        init(context);
    }

    public static BLEManager getInstance(Context context) {
        if (bleManager == null) {
            synchronized (BLEManager.class) {
                if (bleManager == null) {
                    bleManager = new BLEManager(context);
                }
            }
        }
        return bleManager;
    }

    /**
     * 初始化
     *
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void init(Context context) {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    /**
     * 判断蓝牙是否可用
     */
    public boolean isenableblueTooth() {
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.isEnabled();
    }

    /**
     * 打开蓝牙
     */
    public boolean enableBluetooth() {
        if (bluetoothAdapter == null) {
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            return bluetoothAdapter.enable();
        } else {
            return true;
        }
    }

    /**
     * 设备是否支持BLE
     *
     * @return
     */
    public boolean isSupportBle() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    /**
     * 扫描设备
     */
    public void startScan(DeviceCallback callback) {
        if (bluetoothAdapter == null) {
            return;
        }
        this.callback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner != null) {
                scanner.startScan(scanCallback);
            }
        } else {
            bluetoothAdapter.startLeScan(leScanCallback);
        }
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        if (bluetoothAdapter == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            scanner.stopScan(scanCallback);
        } else {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (callback != null) {
                Log.i("data-", device.getName() + "/" + result.getRssi());
                callback.result(device, result.getRssi());
            }
        }
    };
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (callback != null) {
                Log.i("data-", device.getName() + "/" + rssi);
                callback.result(device, rssi);
            }
        }
    };
//==============================================以下服务端相关================================================================

    /**
     * 创建Ble服务端，接收连接
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startAdvertising(String name, AdvertiseCallback callback) {
        //BLE广告设置
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .build();

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .build();

        bluetoothAdapter.setName(name);
        //开启服务
        BluetoothLeAdvertiser bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, callback);
    }

    /**
     * 停止服务
     *
     * @param callback
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopAdvertising(AdvertiseCallback callback) {
        BluetoothLeAdvertiser bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        bluetoothLeAdvertiser.stopAdvertising(callback);
    }

    public void onDestroy() {
        if (bluetoothAdapter == null) {
            return;
        }
        stopScan();
    }

    public void connectBlueTooth(Context context, DeviceModel device, Handler handler) {
        this.handler = handler;
        bluetoothGatt = device.getDevice().connectGatt(context, false, bluetoothGattCallback);
    }


    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e(BleConfig.TAG, "onConnectionStateChange 连接成功");
                handler.sendMessage(handler.obtainMessage(0, "连接成功"));
                //查找服务
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                Log.e(BleConfig.TAG, "onConnectionStateChange 连接中......");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(BleConfig.TAG, "onConnectionStateChange 连接断开");
                handler.sendMessage(handler.obtainMessage(9, "连接断开"));
            } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                Log.e(BleConfig.TAG, "onConnectionStateChange 连接断开中......");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //设置读特征值的监听，接收服务端发送的数据
            BluetoothGattService service = bluetoothGatt.getService(BleConfig.UUID_SERVER);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleConfig.UUID_CHAR_READ);
            boolean b = bluetoothGatt.setCharacteristicNotification(characteristic, true);
            Log.e(BleConfig.TAG, "onServicesDiscovered 设置通知 " + b);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String data = new String(characteristic.getValue());
            Log.e(BleConfig.TAG, "<--接收到服务端消息 " + data);
            handler.sendMessage(handler.obtainMessage(1, data));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(BleConfig.TAG, "<--接收到服务端通知： " + new String(characteristic.getValue())+"  >  已收到的回调  ");
        }
    };


}

