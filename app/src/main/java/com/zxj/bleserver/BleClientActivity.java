package com.zxj.bleserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.zxj.bleserver.ble.BLEManager;
import com.zxj.bleserver.ble.BleConfig;
import com.zxj.bleserver.ble.DeviceCallback;
import com.zxj.bleserver.ble.DeviceModel;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class BleClientActivity extends AppCompatActivity implements View.OnClickListener, DeviceCallback {
    private BLEManager bleManager;

    /**
     * 正在扫描
     */
    private boolean isSearching;
    /**
     *
     */
    private DeviceModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_client);
        Button btnStartConnectBle=findViewById(R.id.btn_start_connect);
        Button btnSendMsgClient=findViewById(R.id.btn_send_msg_client);
        btnStartConnectBle.setOnClickListener(this);
        btnSendMsgClient.setOnClickListener(this);
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

        bleManager = BLEManager.getInstance(this);
        if (!bleManager.isSupportBle()) {
            Toast.makeText(BleClientActivity.this, "不支持协议", Toast.LENGTH_LONG).show();
            return;
        }
        openBluetooth();
    }

    /**
     * 判断蓝牙是否开启，没有开启的话打开蓝牙
     */
    private void openBluetooth() {
        bleManager.enableBluetooth();
    }
    /**
     * 发送数据
     *
     * @param msg
     */
    public void sendData(String msg) {
        if (bleManager.bluetoothGatt == null) {
            return;
        }
        //找到服务
        BluetoothGattService service = bleManager.bluetoothGatt.getService(BleConfig.UUID_SERVER);
        if (service == null) {
            return;
        }
        //拿到写的特征值
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleConfig.UUID_CHAR_WRITE);
        bleManager.bluetoothGatt.setCharacteristicNotification(characteristic, true);
        characteristic.setValue(msg.getBytes());
        bleManager.bluetoothGatt.writeCharacteristic(characteristic);
        Log.e("TAG", "--> 发送数据成功：" + msg);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start_connect:
                if(!bleManager.isenableblueTooth()){
                    bleManager.enableBluetooth();
                    Toast.makeText(BleClientActivity.this, "蓝牙为开启", Toast.LENGTH_LONG).show();
                    Log.e("zxj-ble", "手机蓝牙未打开" );
                    return;
                }
                if (bleManager == null) {
                    Toast.makeText(BleClientActivity.this, "蓝牙初始化失败", Toast.LENGTH_LONG).show();
                    Log.e("zxj-ble", "蓝牙manager 初始化失败" );
                    return;
                }
                if (!isSearching) {
                    Log.e("zxj-ble", "开始扫描  siSearchiing= true" );
                    bleManager.startScan(this);
                    isSearching = true;
                } else {
                    Log.e("zxj-ble", "停止扫描  siSearchiing= false" );
                    bleManager.stopScan();
                    isSearching = false;
                }
                break;
            case R.id.btn_send_msg_client:
                sendData("fromC----"+new Random().nextInt(90) % (90 - 10 + 1) + 10);
                break;
        }
    }

    @Override
    public void result(BluetoothDevice device, int rssi) {
        if (device != null && device.getName() != null && BleConfig.DEVICE_NAME.equals(device.getName())) {
            if (model == null) {
                model = new DeviceModel(device, String.valueOf(rssi));
            } else {
                model.setNewData(device, String.valueOf(rssi));
            }
            Log.e("zxj", "查找到了设备：%s\n%s" + model.getName() + "/" + model.getMac());

            bleManager.stopScan();
            isSearching = false;

            BLEManager.getInstance(BleClientActivity.this).connectBlueTooth(BleClientActivity.this, model, handler);
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
//                    Log.e("zxj-ble","蓝牙连接成功");

                    break;
                case 1:
//                    Log.e("zxj-ble","接收到服务端消息");

                    break;
                case 9:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果还在搜索中，则停止扫描
        if(bleManager!=null&&isSearching){
            bleManager.stopScan();
        }
        if(bleManager!=null&&bleManager.bluetoothGatt!=null){
            bleManager.bluetoothGatt.disconnect();
            bleManager.bluetoothGatt.close();
        }

    }
}