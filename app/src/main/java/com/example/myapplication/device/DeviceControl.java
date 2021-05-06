package com.example.myapplication.device;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.MyMqttClient;
import com.example.myapplication.R;
import com.example.myapplication.database.User;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class DeviceControl extends AppCompatActivity {
    private String TAG = DeviceControl.class.getSimpleName().toString();

    private TargetEquipment targetEquipment = null;                // 接收界面跳转过来的数据
    private String equipmentName;

    TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7, textView8;// 显示温湿度数据文本,显示设备状态
    String targetDeviceName = "";            // 要控制的设备Name
    String SubscribeTopic = "";              // 发布的主题
    String PublishTopic = "";                // 订阅的主题
    ImageButton ImageButton1;                // 控制开关按钮

    MyHandler mHandler;
    private String ControlSwitchOn = "", ControlSwitchOff = "", QueryStatusData = "", QueryTHData = "", QueryAllData="";

    // 定时器用于查询设备的状态
    private Timer timerQueryStatus = null;
    private TimerTask timerTaskQueryStatus = null;

    // 定时器用于轮训订阅主题
    private Timer timerSubscribeTopic = null;
    private TimerTask TimerTaskSubscribeTopic = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        textView1 = findViewById(R.id.tv_temperature);
        textView2 = findViewById(R.id.tv_humidity);
        textView3 = findViewById(R.id.tv_equipment_status);
        textView4 = findViewById (R.id.tv_contact_status);
        textView5 = findViewById (R.id.tv_temperature_suggest);
        textView6 = findViewById (R.id.tv_humidity_suggest);
        textView7 = findViewById (R.id.tv_exception_temperature);
        textView8 = findViewById (R.id.tv_humidity_exception);

        ImageButton1 = findViewById(R.id.btn_power);
        ImageButton1.setTag(false);//默认是关闭

        mHandler = new MyHandler();

        equipmentName = getSharedPreferences("LuoBo", Context.MODE_PRIVATE).getString("deviceName", null);
        /**
         *  获得跳转的数据
         */
        Bundle bundle = getIntent().getBundleExtra("bundle");
        targetEquipment = (TargetEquipment) bundle.getSerializable("targetEquipment");
        targetDeviceName = targetEquipment.getId ();

        textView3.setText("状态：离线");
        targetEquipment.setStatus("离线");

        // 标题栏
        TextView title = findViewById(R.id.tv_title);
        title.setText(targetEquipment.getName());
        // 设备id
        TextView redId = findViewById(R.id.tv_target_equipment_id);
        redId.setText("设备ID:"+targetDeviceName);

        TextView srcId = findViewById(R.id.tv_source_id);
        srcId.setText("下发设备ID："+equipmentName);

        TextView id = findViewById(R.id.tv_target_id);
        id.setText("上报设备ID："+targetDeviceName);

        PublishTopic = "/" + User.productKey + "/" + equipmentName + "/user/update";    // 组合设备发布的主题
        SubscribeTopic = "/" + User.productKey + "/" + equipmentName + "/user/get";     // 组合设备订阅的主题

        /**
         * 封装json数据:控制继电器吸合
         */
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("TargetDevice", targetDeviceName);       // TargetDevice 要和规则引擎设置的一致
            jsonObject.put("DeviceName", equipmentName);     // 把本机的设备名字告诉设备
            jsonObject.put("requestCode", 1);
            jsonObject.put("switch", "1");
            ControlSwitchOn = jsonObject.toString();  // 控制继电器吸合

            jsonObject.put("requestCode", 2);
            jsonObject.put("switch", "0");
            ControlSwitchOff = jsonObject.toString(); // 控制继电器断开

            jsonObject.put("requestCode", 3);
            jsonObject.put("switch", "get");
            QueryStatusData = jsonObject.toString();  // 查询继电器状态

            jsonObject.remove("switch");
            jsonObject.put("requestCode", 4);
            jsonObject.put("TH", "get");
            QueryTHData = jsonObject.toString();//请求温湿度数据

            jsonObject.remove("TH");
            jsonObject.put("requestCode", 5);
            jsonObject.put("allData", "get");
            QueryAllData = jsonObject.toString(); // 请求温湿度数据
            jsonObject.remove("allData");

        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceControl.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("targetEquipmentReturnData", targetEquipment);
                intent.putExtra("bundle", bundle);
                startActivity(intent);
                finish();
            }
        });

        ImageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((boolean) (ImageButton1.getTag()) == false) {
                    ImageButton1.setImageResource(R.drawable.power_open);
                    ImageButton1.setTag(true);

                    MyMqttClient.sharedCenter().setSendData(PublishTopic, ControlSwitchOn,0,false);
                } else {
                    ImageButton1.setImageResource(R.drawable.power_close);
                    ImageButton1.setTag(false);
                    MyMqttClient.sharedCenter().setSendData(PublishTopic, ControlSwitchOff,0,false);
                }
            }
        });

        MyMqttClient.sharedCenter().setOnServerReadStringCallback(new MyMqttClient.OnServerReadStringCallback() {
            @Override
            public void callback(String Topic, MqttMessage Msg, byte[] MsgByte) {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = Msg.toString();
                mHandler.sendMessage(msg);
            }
        });

        /**
         * 连接上服务器
         */
        MyMqttClient.sharedCenter().setOnServerConnectedCallback(new MyMqttClient.OnServerConnectedCallback() {
            @Override
            public void callback() {
                startTimerSubscribeTopic();//定时订阅主题
            }
        });

        /**
         * 订阅主题成功回调
         */
        MyMqttClient.sharedCenter().setOnServerSubscribeCallback(new MyMqttClient.OnServerSubscribeSuccessCallback() {
            @Override
            public void callback(String Topic, int qos) {
                if (Topic.equals(SubscribeTopic)){
                    stopTimerSubscribeTopic();//订阅到主题,停止订阅
                }
            }
        });
        startTimerSubscribeTopic(); // 定时订阅主题
        startTimerQueryStatus();    // 请求设备状态
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DeviceControl.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("targetEquipmentReturnData", targetEquipment);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    /**
     * {"TargetDevice":"12345663","DeviceName":"123456","data":"switch","bit":"1","temperature":"25","humidity":"36","status":"1"}
     */
    class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                String data = (String) msg.obj;
                Log.e(TAG, "handleMessage: " + data);

                try {
                    JSONObject jsonObject = new JSONObject(data);
                    String Name = jsonObject.getString("DeviceName");

                    if (Name.equals(targetDeviceName)) { // 是该设备返回的数据
                        // 能执行到说明肯定是有数据返回的
                        textView3.setText("状态：在线");
                        int resultCode = jsonObject.getInt("resultCode");
                        switch (resultCode) {
                            case 1:
                            case 2:
                            case 3:
                            case 5:    // 请求码5是查找全部的数据
                                String status = jsonObject.getString("switch");
                                if (status.equals(new String("1"))){
                                    ImageButton1.setImageResource(R.drawable.power_open);
                                    targetEquipment.setSwitchOfPic(R.drawable.power_open);
                                    ImageButton1.setTag(true);
                                    textView4.setText ("吸合");
                                }else if (status.equals(new String("0"))){
                                    ImageButton1.setImageResource(R.drawable.power_close);
                                    targetEquipment.setSwitchOfPic(R.drawable.power_close);
                                    ImageButton1.setTag(false);
                                    textView4.setText ("断开");
                                }
                                if (resultCode==5) {
                                    String temperature = jsonObject.getString("temperature");
                                    String humidity = jsonObject.getString("humidity");
                                    textView1.setText(temperature+"℃");
                                    textView2.setText(humidity+"%");
                                    targetEquipment.setHumidity(humidity);
                                    targetEquipment.setTemperature(temperature);

                                    int temp = jsonObject.getInt("temperature");
                                    int huni = jsonObject.getInt("humidity");

                                    if (temp>=28){
                                        textView5.setText("开启降温设备");
                                        textView7.setText("温度偏高");
                                    } else if (temp<=18){
                                        textView5.setText("开启取暖设备");
                                        textView7.setText("温度偏低");
                                    } else {
                                        textView7.setText("温度正常");
                                        textView5.setText("未找到指导建议");
                                    }

                                    if (temp>=85){
                                        textView6.setText("开启除湿设备");
                                        textView8.setText("湿度过大");
                                    } else if (temp<=50){
                                        textView6.setText("开启加湿设备");
                                        textView8.setText("湿度偏低");
                                    } else {
                                        textView8.setText("湿度正常");
                                        textView6.setText("未找到指导建议");
                                    }
                                }
                                break;

                            case 4:
                                String temperature = jsonObject.getString("temperature");
                                String humidity = jsonObject.getString("humidity");
                                textView1.setText(temperature+"℃");
                                textView2.setText(humidity+"%");
                                targetEquipment.setHumidity(humidity);
                                targetEquipment.setTemperature(temperature);
                                break;
                        }
                        targetEquipment.setStatus("在线");
                    }
                } catch (Exception e) {}
            }
        }
    }

    private void startTimerSubscribeTopic(){
        if (timerSubscribeTopic == null) {
            timerSubscribeTopic = new Timer();
        }
        if (TimerTaskSubscribeTopic == null) {
            TimerTaskSubscribeTopic = new TimerTask() {
                @Override
                public void run() {
                    MyMqttClient.sharedCenter().setSubscribe(SubscribeTopic, 0);//订阅主题
                    Log.e(TAG, "Subscribe: "+ SubscribeTopic);
                }
            };
        }
        if(timerSubscribeTopic != null && TimerTaskSubscribeTopic != null )
            timerSubscribeTopic.schedule(TimerTaskSubscribeTopic, 0, 1000);
    }

    private void stopTimerSubscribeTopic(){
        if (timerSubscribeTopic != null) {
            timerSubscribeTopic.cancel();
            timerSubscribeTopic = null;
        }
        if (TimerTaskSubscribeTopic != null) {
            TimerTaskSubscribeTopic.cancel();
            TimerTaskSubscribeTopic = null;
        }
    }

    // 定时器每隔3S查询一次设备状态
    private void startTimerQueryStatus(){
        if (timerQueryStatus == null) {
            timerQueryStatus = new Timer();
        }
        if (timerTaskQueryStatus == null) {
            timerTaskQueryStatus = new TimerTask() {
                @Override
                public void run() {
                // MyMqttClient.sharedCenter().setSendData(PublishTopic,QueryTHData,0,false);//请求温湿度数据
                MyMqttClient.sharedCenter().setSendData(PublishTopic,QueryAllData,0,false);//请求继电器状态
                Log.e(TAG, "SendData: "+ PublishTopic);
                }
            };
        }
        if(timerQueryStatus != null && timerTaskQueryStatus != null )
            timerQueryStatus.schedule(timerTaskQueryStatus, 500, 3000);
    }

    private void stopTimerQueryStatus(){
        if (timerQueryStatus != null) {
            timerQueryStatus.cancel();
            timerQueryStatus = null;
        }
        if (timerTaskQueryStatus != null) {
            timerTaskQueryStatus.cancel();
            timerTaskQueryStatus = null;
        }
    }

    //当活动不再可见时调用
    @Override
    protected void onStop()
    {
        super.onStop();
        stopTimerQueryStatus();
        stopTimerSubscribeTopic();
        // MyMqttClient.sharedCenter().setUnSubscribe(SubscribeTopic);
        Log.e(TAG, "onStop: " );
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        startTimerSubscribeTopic();// 定时订阅主题
        startTimerQueryStatus();   // 请求设备状态
        Log.e(TAG, "onRestart: " );
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart: " );
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: " );
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: " );
        stopTimerQueryStatus();
        stopTimerSubscribeTopic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: " );
    }
}