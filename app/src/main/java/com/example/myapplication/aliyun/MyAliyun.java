package com.example.myapplication.aliyun;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

import com.example.myapplication.LoginActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.MyMqttClient;
import com.example.myapplication.SharedPreferencesHelper;
import com.example.myapplication.database.User;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MyAliyun {
    private final String TAG = MyAliyun.class.getSimpleName();
    private Context context;

    public static int MqttPort = 1883;//端口号
    public static int KeepAlive = 60;//心跳包时间
    public static boolean useSSL = false;//是否使用SSL;默认单向认证,忽略证书连接.
    public static boolean CleanSession = true;//清空session

    public static String ClientId = "";//ClientId
    public static String MqttUserString = "";//用户名
    public static String MqttPwdString = "";//密码
    public static String MqttIPString = "";//IP地址

    public MyAliyun(Context context) {
        this.context = context;
    }

    // 创建一个单任务线程池
    public static ExecutorService SingleThreadExecutor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Toast.makeText(context, (String) msg.obj, Toast.LENGTH_LONG).show();
                    ((Activity)context).finish();
                    break;

                case 2:
                    Toast.makeText(context, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
            return true;
        }
    });


    public void RegisterDevice(SharedPreferencesHelper sharedPreferencesHelper, String inputName, User user) {

        SingleThreadExecutor.execute(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DefaultProfile profile = DefaultProfile.getProfile(User.region, User.accessKeyID, User.accessKeySecret);
                        IAcsClient client = new DefaultAcsClient(profile);
                        CommonRequest request = new CommonRequest();

                        request.setMethod(MethodType.POST);
                        request.setDomain(User.domain);
                        request.setVersion("2018-01-20");
                        request.setAction("RegisterDevice");
                        request.putQueryParameter("RegionId", User.region);
                        request.putQueryParameter("ProductKey", User.productKey);
                        request.putQueryParameter("DeviceName", inputName);
                        try {
                            CommonResponse response = client.getCommonResponse(request);
                            /*解析数据*/
                            String ToastString = "";
                            JSONObject jsonObject = new JSONObject(response.getData());
                            boolean Success = jsonObject.getBoolean("Success");
                            if (Success) {
                                String Data = jsonObject.getString("Data");
                                JSONObject jsonObject1 = new JSONObject(Data);
                                user.setDeviceSecret(jsonObject1.getString("DeviceSecret"));//提取DeviceSecret

                                sharedPreferencesHelper.put("deviceSecret", user.getDeviceSecret()); // 储存DeviceSecret
                                sharedPreferencesHelper.put("deviceName", inputName);                // 存储注册注册的设备名字
                                sharedPreferencesHelper.put("password", user.getPassword());         // 保存密码

                                user.setDeviceName(inputName);
                                ToastString = "成功注册设备:DeviceName:" + user.getDeviceName() + "  deviceSecret:" + user.getDeviceSecret();

                            } else {
                                sharedPreferencesHelper.remove("password");
                                ToastString = jsonObject.getString("Code");
                            }
                            Message msg = handler.obtainMessage();//取出来了所有的数据以后,刷新
                            msg.obj = ToastString;
                            msg.what = 1;
                            handler.sendMessage(msg);
                            Log.e(TAG, "onCreate: " + response.getData());
                        } catch (Exception e) {
                            Log.e(TAG, "onCreate: " + e.toString());
                        }
                    }
                }));

    }

    public void connect(User user) {
        /* IP地址 */
        MqttIPString = User.productKey + ".iot-as-mqtt." + User.region + ".aliyuncs.com";
        /*ClientId*/
        ClientId = user.getDeviceName() + "|securemode=3,signmethod=hmacsha1|";
        /*用户名*/
        MqttUserString = user.getDeviceName() + "&" + User.productKey;
        try {//计算密码
            MqttPwdString = encryptHMAC(
                    "hmacsha1",
                    "clientId" + user.getDeviceName() + "deviceName" + user.getDeviceName() + "productKey" + User.productKey,
                    user.getDeviceSecret());
            MyMqttClient.sharedCenter().setRstConnect();//连接MQTT
        } catch (Exception e) {
            Toast.makeText(context, "密码计算错误", Toast.LENGTH_LONG).show();
        }
    }

    /*计算MQTT密码**********************************************************************/
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static String encryptHMAC(String signMethod, String content, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key.getBytes("utf-8"), signMethod);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);
        byte[] data = mac.doFinal(content.getBytes("utf-8"));
        return bytesToHexString(data);
    }
}
