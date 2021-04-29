package com.zxj.bleserver;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.zxj.bleserver.ble.BLEManager;
import com.zxj.bleserver.ble.BleConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author：zxj
 * @date：2021/4/17 5:32 PM
 * @class: MainActivity
 * @dec:
 */
public class BleServerActivity extends AppCompatActivity implements View.OnClickListener {
    private BLEManager bleManager;
    /**
     * 客户端读取数据的特征值
     */
    private BluetoothGattCharacteristic characteristicRead;
    private BluetoothGattServer bluetoothGattServer;
    /**
     * 当前连接的设备集合
     */
    private Map<String, BluetoothDevice> deviceMap;
    /**
     * 当前连接的设备mac地址集合
     */
    public static ArrayList addressList = new ArrayList();
    /**
     * 蓝牙内容显示
     */
    private TextView tvContent;
    /**
     * 开启蓝牙服务按钮
     */
    private Button btnOperBleserver;
    /**
     * 向周边设备发送消息按钮
     */
    private Button btnSendMsgToClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvContent = findViewById(R.id.tv_content);
        btnOperBleserver = findViewById(R.id.btn_open_bleserver);
        btnSendMsgToClient = findViewById(R.id.btn_send_msg);
        btnOperBleserver.setOnClickListener(this);
        btnSendMsgToClient.setOnClickListener(this);
        initPermision();

    }

    private void initPermision() {
        XXPermissions.with(this)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .permission(Permission.ACCESS_FINE_LOCATION)
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {

                        initBle();

                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                    }
                });
    }

    private void initBle() {
        if (deviceMap == null) {
            deviceMap = new HashMap<>();
        }
        bleManager = BLEManager.getInstance(this);
//        if(bleManager==null){
//            LogUtil.LOG_UTIL.e("zxj_bleService:", "bleManager==null");
//        }else{
//            LogUtil.LOG_UTIL.e("zxj_bleService:", "bleManager!=null");
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_bleserver:
                startBle();
                break;
            case R.id.btn_send_msg:
                //发送一个随机数
                sendDataAll("一一一一一一"+String.valueOf(new Random().nextInt(90) % (90 - 10 + 1) + 10));
                break;
        }
    }

    private void startBle() {
//        if (bleManager.isSupportBle()) {
//            Toast.makeText(this,"设备不支持蓝牙服务",Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (!bleManager.enableBluetooth()) {
            Toast.makeText(this, "打开蓝牙失败", Toast.LENGTH_SHORT).show();
            return;
        }
        //开启ble服务
        bleManager.startAdvertising(BleConfig.DEVICE_NAME, advertiseCallback);
    }

    /**
     * Ble服务监听
     */
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            //todo 开启服务成功
            addService();
        }
    };

    /**
     * 添加读写服务UUID，特征值等
     */
    private void addService() {
        BluetoothGattService gattService = new BluetoothGattService(BleConfig.UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //只读的特征值
        characteristicRead = new BluetoothGattCharacteristic(BleConfig.UUID_CHAR_READ,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        //只写的特征值
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(BleConfig.UUID_CHAR_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ
                        | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);
        //将特征值添加至服务里
        gattService.addCharacteristic(characteristicRead);
        gattService.addCharacteristic(characteristicWrite);
        //监听客户端的连接
        bluetoothGattServer = bleManager.getBluetoothManager().openGattServer(this, gattServerCallback);
        bluetoothGattServer.addService(gattService);
        Toast.makeText(this, "蓝牙服务开启成功-" + BleConfig.DEVICE_NAME, Toast.LENGTH_SHORT).show();
        Log.e("zxj-ble", "蓝牙服务开启成功-" + BleConfig.DEVICE_NAME);
    }


    private BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        /**
         * 监听设备连接的数据
         *
         * @param device
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            String addressID = device.getAddress();
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //todo 连接成功
                addDeviceList(device);
                if (bluetoothGattServer != null) {
                    bluetoothGattServer.connect(device, false);
                }
//                BleConfig.bleState = BleConfig.BLE_CONNECTED;
                addressList.add(addressID);
//                Toast.makeText(BleServerActivity.this,"蓝牙连接成功-addressid="+addressID,Toast.LENGTH_SHORT).show();
                Log.e("zxj-ble", "监听到客户端设备并将设备添加至服务---->设备addressId=" + addressID);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //todo 连接断开
                releaseDevice(device);
                addressList.remove(addressID);
//                BleConfig.bleState = BleConfig.BLE_DISCONNECTED;
//                Toast.makeText(BleServerActivity.this,"蓝牙断开连接-addressid="+addressID,Toast.LENGTH_SHORT).show();
                Log.e("zxj-ble", "监听到客户端设备断开连接----->设备addressId=" + addressID);
            }
        }

        /**
         * 监听客户端发来的数据
         * @param device
         * @param requestId
         * @param characteristic
         * @param preparedWrite
         * @param responseNeeded
         * @param offset
         * @param value
         */
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            String data = new String(value);
            //告诉客户端发送成功
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
            characteristicRead.setValue((data + "success").getBytes());
            bluetoothGattServer.notifyCharacteristicChanged(device, characteristicRead, false);
            Log.e("zxj-ble", "收到客户端" + device.getAddress() + "的消息：" + data + "------>requestId=" + requestId);
        }
    };

    /**
     * 指定设备写入数据给客户端
     */
    public void sendData(BluetoothDevice device, String msg) {
        characteristicRead.setValue(msg.getBytes());
        bluetoothGattServer.notifyCharacteristicChanged(device, characteristicRead, false);
    }

    /**
     * 全部设备写入数据给客户端
     */
    public void sendDataAll(String msg) {
        Log.e("zxj-ble","发送的数据byte length-"+msg.getBytes().length);
        characteristicRead.setValue(msg.getBytes());
        for (Iterator<BluetoothDevice> iterator = deviceMap.values().iterator(); iterator.hasNext(); ) {
            BluetoothDevice next = iterator.next();
            if (next != null) {
                Log.e("zxj-ble", "发送数据给-" + next.getAddress() + "，内容为：" + msg);
                bluetoothGattServer.notifyCharacteristicChanged(next, characteristicRead, false);
            }
        }
    }

    /**
     * 移除单个设备
     *
     * @param device
     */
    private void releaseDevice(BluetoothDevice device) {
        if (device != null) {
            String deviceKey = device.getAddress() == null ? "1234" : device.getAddress();
            if (bluetoothGattServer != null) {
                bluetoothGattServer.cancelConnection(device);
            }
            deviceMap.remove(deviceKey);
        }
    }

    /**
     * 移除全部
     *
     * @param devices
     */
    private void releaseDevice(Map devices) {
        if (devices != null && devices.size() > 0) {
            for (Iterator<BluetoothDevice> iterator = devices.values().iterator(); iterator.hasNext(); ) {
                BluetoothDevice next = iterator.next();
                if (next != null) {
                    if (bluetoothGattServer != null) {
                        bluetoothGattServer.cancelConnection(next);
                    }
                }
            }
            devices.clear();
        }
    }

    private void addDeviceList(BluetoothDevice device) {
        String key = device.getAddress() == null ? "1234" : device.getAddress();
        deviceMap.put(key, device);
    }

    private ArrayList getAddress() {
        return addressList;

    }


    @Override
    public void onDestroy() {
        if (bleManager != null) {
            bleManager.stopAdvertising(advertiseCallback);
        }
        releaseDevice(deviceMap);
        if (bluetoothGattServer != null) {
            bluetoothGattServer.clearServices();
            bluetoothGattServer.close();
            bluetoothGattServer = null;
        }
        super.onDestroy();
    }
}