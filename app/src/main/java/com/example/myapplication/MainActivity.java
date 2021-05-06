package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.myapplication.adapter.EquipmentAdapter;
import com.example.myapplication.database.DataBase;
import com.example.myapplication.database.EquipmentBean;
import com.example.myapplication.database.User;
import com.example.myapplication.device.DeviceControl;
import com.example.myapplication.device.Equipment;
import com.example.myapplication.device.TargetEquipment;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity
        implements EquipmentAdapter.InnerItemOnclickListener, AdapterView.OnItemClickListener,
        View.OnClickListener {

    private String TAG = MainActivity.class.getSimpleName().toString();
    private MyHandler mHandler = null;

    public final static int REGISTER_CODE = 0x01;

    ImageView ivBack, ivAddEquipments;

    boolean statusOfSwitch = false;
    List<Equipment> mDatas = new ArrayList<>();  // 存储ListView的数据,实际我们存放的是目标设备的DeciceName

    EquipmentAdapter mAdapter;
    boolean ListViewLongClick = false;//是不是长按

    private ListView lvEquimentList;
    private DataBase dataBase;

    private String equipmentName;
    private TargetEquipment targetEquipment;
    private int listOfIndex;

    // 定时器用于轮训订阅主题
    private Timer timerSubscribeTopic = null;
    private TimerTask TimerTaskSubscribeTopic = null;

    // 公共定时器
    private Timer tmr = null;
    private TimerTask tmrTask = null;
    private int querySwitchCnt = 0;
    private boolean switchDataReturned = true;        // 开关数据返回标志
    // private

    private String SubscribeTopic = "";
    private String PublishTopic = "";
    private String ControlSwitchOn = "", ControlSwitchOff = "", QueryStatusData = "", QueryTHData = "", QueryAllData = "";
    JSONObject jsonObject = new JSONObject();

    View layoutAddEquipment;
    View rl_download;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTER_CODE && resultCode == REGISTER_CODE) {
            Bundle bundle = data.getExtras();
            TargetEquipment equipment = (TargetEquipment) bundle.getSerializable("targetEquipmentReturnData");
            mDatas.get(listOfIndex).setHumidity(equipment.getHumidity());
            mDatas.get(listOfIndex).setTemperature(equipment.getTemperature());
            mDatas.get(listOfIndex).setStatus(equipment.getStatus());
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutAddEquipment = findViewById(R.id.include_layout_title);
        rl_download = findViewById(R.id.rl_download);

        // 设置订阅主题成功回调方法
        MyMqttClient.sharedCenter().setOnServerSubscribeCallback(new MyMqttClient.OnServerSubscribeSuccessCallback() {
            @Override
            public void callback(String Topic, int qos) {
                if (Topic.equals(SubscribeTopic)) {
                    stopTimerSubscribeTopic();//订阅到主题,停止订阅
                }
            }
        });

        if (tmr == null) {
            tmr = new Timer();
        }
        if (tmrTask == null) {
            tmrTask = new TimerTask() {
                @Override
                public void run() {
                    if (switchDataReturned == false) {
                        if (querySwitchCnt != 0) querySwitchCnt--;
                        else
                            MyMqttClient.sharedCenter().setSendData(PublishTopic, QueryStatusData, 0, false);
                    }
                }
            };
        }
        if (tmr != null && tmrTask != null)
            tmr.schedule(tmrTask, 10000, 500);  // 这个定时器6秒启动后，会一直运行

        // 获得手机的DeviceName
        equipmentName = this.getSharedPreferences("LuoBo", Context.MODE_PRIVATE).getString("deviceName", null);
        SubscribeTopic = "/" + User.productKey + "/" + equipmentName + "/user/get";     // 组合设备订阅的主题
        PublishTopic = "/" + User.productKey + "/" + equipmentName + "/user/update";    // 组合设备发布的主题

        dataBase = new DataBase(getApplicationContext(), 1); // 创建数据库;
        mHandler = new MyHandler();

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);

        ivAddEquipments = findViewById(R.id.iv_add_equipments);
        ivAddEquipments.setOnClickListener(this);

        mAdapter = new EquipmentAdapter(mDatas, this);
        mAdapter.setOnInnerItemOnClickListener(this);
        lvEquimentList = findViewById(R.id.lv_equiment_list);
        lvEquimentList.setAdapter(mAdapter);
        lvEquimentList.setOnItemClickListener(this);

        //ListView 长按
        lvEquimentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListViewLongClick = true;
                showNormalDialog(position);//长按显示对话框
                return false;
            }
        });

        startTimerSubscribeTopic();
    }

    @Override
    public void itemClick(View v) {
        int position;
        position = (Integer) v.getTag();

        switch (v.getId()) {
            case R.id.ibtn_equipment_switch:
                if (switchDataReturned == true) {
                    try {
                        JSONObject jSwitch = new JSONObject(ControlSwitchOn);
                        jSwitch.put("TargetDevice", mDatas.get(position).getId());       // TargetDevice 要和规则引擎设置的一致
                        jSwitch.put("requestCode", 1);
                        jSwitch.put("switch", "1");
                        ControlSwitchOn = jSwitch.toString();  // 控制继电器吸合

                        jSwitch.put("requestCode", 2);
                        jSwitch.put("switch", "0");
                        ControlSwitchOff = jSwitch.toString(); // 控制继电器断开

                        jSwitch.put("requestCode", 3);
                        jSwitch.put("switch", "get");
                        QueryStatusData = jSwitch.toString();  // 查询继电器状态
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (statusOfSwitch == false) {
                        statusOfSwitch = true;
                        mDatas.get(position).setSwitchOfPic(R.drawable.power_open);
                        MyMqttClient.sharedCenter().setSendData(PublishTopic, ControlSwitchOn, 0, false);
                    } else {
                        statusOfSwitch = false;
                        mDatas.get(position).setSwitchOfPic(R.drawable.power_close);
                        MyMqttClient.sharedCenter().setSendData(PublishTopic, ControlSwitchOff, 0, false);
                    }
                    switchDataReturned = false;
                    querySwitchCnt = 6; // 3秒后没有收到数据就快速查询
                    mAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Log.e("整体item----->", position + "");
        if (!ListViewLongClick) {
            Intent intent = new Intent(MainActivity.this,
                    DeviceControl.class);
            targetEquipment = new TargetEquipment();
            targetEquipment.setId(mDatas.get(position).getId());
            targetEquipment.setName(mDatas.get(position).getName());
            targetEquipment.setMasterEquipmentName(equipmentName);
            targetEquipment.setSwitchOfPic(mDatas.get(position).getSwitchOfPic());
            Bundle bundle = new Bundle();
            bundle.putSerializable("targetEquipment", targetEquipment);
            listOfIndex = position;
            intent.putExtra("bundle", bundle);
            startActivityForResult(intent, 1);
        }
        ListViewLongClick = false;
    }

    private void showNormalDialog(final int index) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("删除设备");
        normalDialog.setMessage("确定要删除设备吗？");
        normalDialog.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataBase.delete(mDatas.get(index));//数据库删除选中的ClientId
                mDatas.remove(index);//从ListView删除
                mAdapter.notifyDataSetChanged();
            }
        });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        // 显示
        normalDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;

            case R.id.iv_add_equipments:
                Intent intent = new Intent(MainActivity.this, ActivityBindDevices.class);
                startActivity(intent);
                break;
        }
    }

    class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mAdapter.notifyDataSetChanged();    // 刷新显示页面
            } else if (msg.what == 2) {
                String data = (String) msg.obj;
                Log.e(TAG, "handleMessage: " + data);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    String Name = jsonObject.getString("DeviceName");
                    int index = -1;
                    for (int i = 0; i < mDatas.size(); i++) {
                        if (Name.equals(mDatas.get(i).getId())) {
                            index = i;
                            break;
                        }
                    }
                    if (index == -1) return;

                    // 能执行到说明肯定是有数据返回的
                    mDatas.get(index).setStatus("状态：在线");
                    int resultCode = jsonObject.getInt("resultCode");

                    switch (resultCode) {
                        case 1:
                        case 2:
                        case 3:
                        case 5:    // 请求码5是查找全部的数据
                            String status = jsonObject.getString("switch");  // 获取开关的状态
                            if (status.equals(new String("1"))) {
                                statusOfSwitch = true;
                                mDatas.get(index).setSwitchOfPic(R.drawable.power_open);
                            } else if (status.equals(new String("0"))) {
                                statusOfSwitch = false;
                                mDatas.get(index).setSwitchOfPic(R.drawable.power_close);
                            }
                            if (resultCode == 5) {
                                String temperature = jsonObject.getString("temperature");
                                String humidity = jsonObject.getString("humidity");
                                mDatas.get(index).setHumidity(humidity);
                                mDatas.get(index).setTemperature(temperature);
                            }
                            switchDataReturned = true; // 标记数据返回
                            break;

                        case 4:
                            String temperature = jsonObject.getString("temperature");
                            String humidity = jsonObject.getString("humidity");
                            mDatas.get(index).setHumidity(humidity);
                            mDatas.get(index).setTemperature(temperature);
                            break;
                    }
                } catch (Exception e) {
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    // 解析菜单资源文件
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }



    private void getData() {

    }

    /**
     * 定时器每隔1S尝试订阅主题
     */
    private void startTimerSubscribeTopic() {
        if (timerSubscribeTopic == null) {
            timerSubscribeTopic = new Timer();
        }
        if (TimerTaskSubscribeTopic == null) {
            TimerTaskSubscribeTopic = new TimerTask() {
                @Override
                public void run() {
                    MyMqttClient.sharedCenter().setSubscribe(SubscribeTopic, 0);//订阅主题
                    Log.e(TAG, "Subscribe: " + SubscribeTopic);
                }
            };
        }
        if (timerSubscribeTopic != null && TimerTaskSubscribeTopic != null)
            timerSubscribeTopic.schedule(TimerTaskSubscribeTopic, 0, 1000);
    }

    private void stopTimerSubscribeTopic() {
        if (timerSubscribeTopic != null) {
            timerSubscribeTopic.cancel();
            timerSubscribeTopic = null;
        }
        if (TimerTaskSubscribeTopic != null) {
            TimerTaskSubscribeTopic.cancel();
            TimerTaskSubscribeTopic = null;
        }

        // 订阅成功后开始查询设备状态
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 4; i++) {
                    for (Equipment e : mDatas) {
                        Log.e(TAG, "目标设备: " + e.getId());

                        try {
                            jsonObject.put("TargetDevice", e.getId());       // TargetDevice 要和规则引擎设置的一致
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

                        MyMqttClient.sharedCenter().setSendData(PublishTopic, QueryAllData, 0, false);

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }

                switchDataReturned = true; // 标记数据返回

                // 获取完数据后把隐藏的数据显示出来
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lvEquimentList.setVisibility(View.VISIBLE);
                        layoutAddEquipment.setVisibility(View.VISIBLE);
                        rl_download.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * MQTT接收数据
         * {"TargetDevice":"APPMqtt","aaaa":666666}
         */
        MyMqttClient.sharedCenter().setOnServerReadStringCallback(new MyMqttClient.OnServerReadStringCallback() {
            @Override
            public void callback(String Topic, MqttMessage Msg, byte[] MsgByte) {
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = Msg.toString();
                mHandler.sendMessage(msg);
            }
        });

        Bundle bundle = getIntent().getBundleExtra("bundle");
        EquipmentBean equipment = (EquipmentBean) bundle.getSerializable("equipment");
        if (equipment != null) {
            boolean existed = false;
            for (Equipment e : mDatas) {
                if (e.getId().equals(equipment.getId())) existed = true;
            }
            if (existed==false) {
                dataBase.insert(equipment);//插入数据库
            }
        }

        TargetEquipment targetEquipment = (TargetEquipment) bundle.getSerializable("targetEquipmentReturnData");
        if (targetEquipment != null) {
            mDatas.get(listOfIndex).setHumidity(targetEquipment.getHumidity());
            mDatas.get(listOfIndex).setTemperature(targetEquipment.getTemperature());
            mDatas.get(listOfIndex).setStatus(targetEquipment.getStatus());
            mDatas.get(listOfIndex).setSwitchOfPic(targetEquipment.getSwitchOfPic());
            mAdapter.notifyDataSetChanged();
            Log.e(TAG, "设备状态：" + targetEquipment.getStatus() + targetEquipment.getSwitchOfPic());
        } else {
            // 使用任务取出数据库里保存的数据
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mDatas.clear();
                    List<EquipmentBean> equipments = dataBase.queryEquipments();
                    for (int i = 0; i < equipments.size(); i++) {
                        Equipment e = new Equipment();
                        e.setName(equipments.get(i).getName());
                        e.setId(equipments.get(i).getId());
                        e.setRegion(equipments.get(i).getRegion());
                        e.setPicture("R.drawable.add_equipment");
                        mDatas.add(e);
                    }
                    Message msg = mHandler.obtainMessage();  // 拿出数据后告诉主线程可以更新listview控件了
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                }
            }).start();
        }
        bundle.clear();
        Log.e(TAG, "未运行");
    }
}
