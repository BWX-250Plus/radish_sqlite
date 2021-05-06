package com.example.myapplication.launch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.LoginActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.RegisterActivity;

import ru.alexbykov.nopermission.PermissionHelper;

public class LaunchActivity extends AppCompatActivity {
    private String TAG = LaunchActivity.class.getSimpleName().toString();

    private TextView tvLaunch;
    private int count = 5;

    private PermissionHelper permissionHelper; // 动态权限申请
    String[] PermissionString={//需要提醒用户申请的权限
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.REQUEST_INSTALL_PACKAGES,
    };


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                count--;
                if (count == 0) {
                    Intent intent = new Intent (LaunchActivity.this, LoginActivity.class);
                    startActivity (intent);

                    finish ();
                } else {
                    tvLaunch.setText ("跳过 "+String.valueOf (count));
                    handler.sendEmptyMessageDelayed (1, 1000);
                }
            }
            return true;
        }
    });

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_launch);

        tvLaunch = findViewById(R.id.launch_tv);
        tvLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 1;
            }
        });

        permissionHelper = new PermissionHelper(this); //权限申请使用

    }

    @Override
    protected void onStart() { // activity 可见时会调运这个方法
        super.onStart();
        checkPermission();
    }

    private void checkPermission(){
        permissionHelper.check(PermissionString).onSuccess(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: 允许" );
            }
        }).onDenied(new Runnable() {
            @Override
            public void run() {
//                permissionHelper.startApplicationSettingsActivity();
                Log.e(TAG, "run: 权限被拒绝" );
            }
        }).onNeverAskAgain(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: 权限被拒绝，下次不会在询问了" );
            }
        }).run();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);

        tvLaunch = findViewById (R.id.launch_tv);
        handler.sendEmptyMessageDelayed (1, 1000);
    }
}