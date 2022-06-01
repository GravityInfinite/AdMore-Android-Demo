package com.plutus.qmkfd.android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.plutus.common.admore.AMSDK;
import com.plutus.common.admore.listener.AMSDKInitListener;
import com.plutus.common.core.utils.Utils;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class SplashActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "SplashActivity";
    private FrameLayout splashContainer; // 开屏广告容器
    private boolean isColdStart = true; // 是否为冷启动

    /**
     * 热启动的时候，调用这个方法来将开屏广告页覆盖在其他页面之上
     * @param activity
     */
    public static void showAdActivity(Activity activity) {
        Intent intent = new Intent(activity, SplashActivity.class);
        intent.putExtra("is_cold_start", false);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Intent intent = getIntent();
        if (intent != null) {
            isColdStart = intent.getBooleanExtra("is_cold_start", true);
        }

        Log.d(TAG, "cold start is " + isColdStart);

        // 全屏沉浸式，去掉status bar
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        splashContainer = findViewById(R.id.splash_container);

        if (isColdStart) {
            String[] perms;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                perms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS};
            } else {
                perms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_SMS,};
            }
            if (EasyPermissions.hasPermissions(this, perms)) {
                allPermissionGranted();
            } else {
                Log.d(TAG, "check permission");
                EasyPermissions.requestPermissions(
                        new PermissionRequest.Builder(SplashActivity.this, 1, perms)
                                .setRationale("需要您授权才能继续游戏，请在接下来的弹窗里选择【允许授权】")
                                .build());
            }
        } else {
            allPermissionGranted();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        allPermissionGranted();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        allPermissionGranted();
    }

    private void allPermissionGranted() {
        if (isColdStart) {
            // 只在冷启动的时候需要初始化
            AMSDK.setLogDebug(true);
            AMSDK.setDebugAdnType(-2);// -2是ALL
            AMSDK.setClientVersion(1);
            AMSDK.enableMoreThreadMode();
            AMSDK.setIsProxyEnabled(false);
            AMSDK.setTargetPressureEnabled(false);
            AMSDK.setChannel("admore_test");
            AMSDK.init(AdmoreConstant.APP_ID, AdmoreConstant.APP_KEY, AdmoreConstant.TEST_USER_ID, new AMSDKInitListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "success");
                }

                @Override
                public void onFail(String errorMsg) {
                    Log.d(TAG, "fail " + errorMsg);
                }
            });
        }
        Log.d(TAG, "splash container status " + splashContainer.getVisibility());
        splashContainer.setVisibility(View.VISIBLE);
        Utils.runOnUiThreadDelay(new Runnable() {
            @Override
            public void run() {
                SplashHelper.loadAndShow(splashContainer, 8000, new SplashHelper.Callback() {
                    @Override
                    public void onEndSplash() {
                        if (!SplashActivity.this.isFinishing()) {
                            SplashActivity.this.finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        if (isColdStart) {
                            Log.d(TAG, "go to main activity");
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        }
                    }
                });
            }
        }, 500); // 必须延迟一下，不然腾讯广告展示会有问题

    }
}