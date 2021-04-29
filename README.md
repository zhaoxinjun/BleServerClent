# BleServerClent
Android Ble通信，实现服务端/客户端两个设备通过蓝牙通信


        <activity android:name=".BleClientActivity"></activity>
        <activity android:name=".BleServerActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

因只做基础通信，方便期间将 BleClientActivity 和 BleServerActivity 放在在一起。通过修改配置文件的启动Activity来打包成不同的服务端和客户端。

使用的时候，可先下载运行demo，打包服务端和客户端 apk看效果，然后拷贝ble包相关代码。用法参考demo activity。

BleConfig.java 内相关UUID 读写特征值可按需修改替换。

demo apk操作步骤：
1.服务端 点击“作为中心设备，开启蓝牙服务”

2客户端 点击“开始扫描 并连接指定蓝牙设备”

服务端要先开启，等待客户端接入。正常情况下此时两个设备已经连接

3.即可通过按钮 点击互相发送消息。

具体通信及回调数据 demo里面已经打印Log 可在控制台查看。

     

