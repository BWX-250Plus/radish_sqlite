package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.example.myapplication.aliyun.MyAliyun;
import com.example.myapplication.database.User;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.alexbykov.nopermission.PermissionHelper;

public class LoginActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName().toString();
    public static final String SUPER_PASSWORD = "zx333666<>";

    private User user;

    // android 里的轻量级存储数据-键值对
    public static SharedPreferencesHelper sharedPreferencesHelper;

    private EditText editTextUserName = null;
    private EditText editTextPassword = null;
    private Button btnLogin = null;

    private boolean isFirst;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUserName = findViewById(R.id.et_userName);
        editTextPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.login);

        sharedPreferencesHelper = new SharedPreferencesHelper(LoginActivity.this, "LuoBo"); //存储数据
        // sharedPreferencesHelper.clear();

        // 创建一个用户信息对象
        user = new User();

        /**
         * 连接上服务器
         */
        MyMqttClient.sharedCenter().setOnServerConnectedCallback(new MyMqttClient.OnServerConnectedCallback() {
            @Override
            public void callback() {
                connected = true;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getDeviceName() != null && user.getDeviceSecret() != null) {

                    if (SUPER_PASSWORD.equals(editTextPassword.getText().toString().trim())) {
                        user.setPassword(SUPER_PASSWORD);
                    }

                    if (user.getPassword() == null) {
                        Toast.makeText(LoginActivity.this, "密钥不正确!!!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if ((user.getDeviceName().equals(editTextUserName.getText().toString().trim()))
                            && (user.getPassword().equals(editTextPassword.getText().toString().trim())
                            || SUPER_PASSWORD.equals(editTextPassword.getText().toString().trim()))) {
                        editTextUserName.setText(user.getDeviceName());
                        editTextPassword.setText(user.getPassword());

                        AlertDialog dialog = new AlertDialog.Builder(LoginActivity.this).setCancelable(false)
                                .setTitle("正在登录").setMessage("玩命加载服务器...")
                                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();

                        new MyAliyun(LoginActivity.this).connect(user);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while(true){
                                    if (connected) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("user", user);
                                        intent.putExtra("bundle", bundle);
                                        startActivity(intent);
                                        finish();
                                        break;
                                    }
                                }
                            }
                        }).start();
                        return;
                    }
                }
                Toast.makeText(LoginActivity.this, "用户名或者密钥不正确!!!", Toast.LENGTH_LONG).show();
            }
        });

        TextView tvRegister = findViewById(R.id.tv_register);
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", user);
                intent.putExtra("bundle", bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* 获取存储的DeviceName,DeviceSecret ---------------------------------------------------*/
        user.setDeviceName((String) sharedPreferencesHelper.getSharedPreference("deviceName", null));
        user.setDeviceSecret((String) sharedPreferencesHelper.getSharedPreference("deviceSecret", null));
        user.setPassword((String) sharedPreferencesHelper.getSharedPreference("password", null));
        user.setKey((String) sharedPreferencesHelper.getSharedPreference("key", null));

        if (user.getDeviceName() != null && user.getDeviceSecret() != null) {
            editTextUserName.setText(user.getDeviceName());
            editTextPassword.setText(user.getPassword());
        }
    }
}