package com.example.myapplication.database;

import java.io.Serializable;

public class User implements Serializable {
    public static String region = "cn-shanghai";//替换自己的Region
    public static String productKey = "a1nkSZq2AhA";//替换自己的 ProductKey
    public static String domain ="iot.cn-shanghai.aliyuncs.com";//提交数据的地址
    public static String accessKeyID = "";           // 替换自己的AccessKey ID
    public static String accessKeySecret = "";       // 替换自己的AccessKey Secret

    private String deviceName;   // 设备名称
    private String deviceSecret; // 注册完成服务器会返回该变量
    private String password;
    private String key;

    public String getTargetDeviceName () {
        return targetDeviceName;
    }

    public void setTargetDeviceName (String targetDeviceName) {
        this.targetDeviceName = targetDeviceName;
    }

    private String targetDeviceName;

    public User(){}

    public User(String deviceName, String deviceSecret, String password) {
        this.deviceName = deviceName;
        this.deviceSecret = deviceSecret;
        this.password = password;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
