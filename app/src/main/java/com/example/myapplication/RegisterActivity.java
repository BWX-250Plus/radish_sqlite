package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.aliyun.MyAliyun;
import com.example.myapplication.database.User;

import java.lang.reflect.Method;
import java.util.Random;

import static com.example.myapplication.LoginActivity.sharedPreferencesHelper;

public class RegisterActivity extends AppCompatActivity {
    private String TAG = RegisterActivity.class.getSimpleName().toString();
    public static final String PASSWORD_SAVE = "6666666";

    private int password = 0;

    Bundle bundle; User user;
    private EditText etUserName, etPassword;
    private TextView tvKey;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 标题栏
        TextView title = findViewById(R.id.tv_title);
        title.setText("用户注册");
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bundle = getIntent().getBundleExtra("bundle");
        user = (User) (bundle.getSerializable("user"));

        etUserName = findViewById(R.id.register_et_userName);
        etPassword = findViewById(R.id.register_et_password);
        tvKey = findViewById(R.id.register_tv_key);
        btnRegister = findViewById(R.id.register_btn);

        if (user.getDeviceName() != null) {
            etUserName.setText(user.getDeviceName());
        }

        String key = user.getKey();
       // 这计算密码部分的代码我去掉了，你们用不到
        tvKey.setText (key);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // if ((PASSWORD_SAVE.equals(etPassword.getText().toString().trim())) || (Integer.valueOf(etPassword.getText().toString().trim()).intValue() == password)) {
                    String name = etUserName.getText().toString().trim();
                    user.setPassword(etPassword.getText().toString().trim());
                    new MyAliyun(RegisterActivity.this).RegisterDevice(sharedPreferencesHelper, name, user);
//                }else {
//                    Toast.makeText(RegisterActivity.this, "密钥不正确，请联系我公司", Toast.LENGTH_LONG).show();
//                }
            }
        });

    }

}