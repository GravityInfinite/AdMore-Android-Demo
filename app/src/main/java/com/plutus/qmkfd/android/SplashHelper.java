package com.plutus.qmkfd.android;

import android.util.Log;
import android.view.ViewGroup;

import com.plutus.common.admore.api.AMSplash;
import com.plutus.common.admore.beans.AdSource;
import com.plutus.common.admore.beans.DefaultSplashAdSourceConfig;
import com.plutus.common.admore.listener.AMSplashListener;
import com.plutus.common.core.utils.Utils;

import java.util.Arrays;

public class SplashHelper {
    private static final String TAG = "SplashHelper";
    private static final AMSplash splashAd;
    private static volatile boolean isTimeOut;
    private static Callback sCallback;

    static {
        // 配置兜底广告组
        splashAd = new AMSplash(Utils.context(), AdmoreConstant.SPLASH_PLACEMENT_ID, Arrays.asList(
                DefaultSplashAdSourceConfig.buildFromJson("{\"name\":\"快手兜底\",\"id\":852,\"app_id\":\"554400009\",\"adn_type\":2,\"slot_id\":\"5544000672\",\"conf\":\"{}\",\"price\":5.9}"),
                DefaultSplashAdSourceConfig.buildFromJson("{\"name\":\"腾讯兜底广告\",\"id\":844,\"app_id\":\"1111911745\",\"adn_type\":1,\"slot_id\":\"9062946556885868\",\"conf\":\"{}\",\"price\":0.5}"),
                DefaultSplashAdSourceConfig.buildFromJson("{\"name\":\"穿山甲兜底广告\",\"id\":843,\"app_id\":\"5184357\",\"adn_type\":0,\"slot_id\":\"887721973\",\"conf\":\"{}\",\"price\":0.1}")));
    }

    private static final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "time out");
            isTimeOut = true;
            beforeEnd();
        }
    };

    /**
     * 提前预加载广告，建议在应用退出到后台时调用
     * @param
     */
    public static void preloadNextSplash() {
        if (!splashAd.isAdReady() && !splashAd.isAdLoading()) {
            Log.d(TAG, "pre load next splash");
            splashAd.setAdListener(null);
            splashAd.load();
        } else {
            Log.d(TAG, "not pre load " + splashAd.isAdReady() + " " + splashAd.isAdLoading());
        }
    }

    public static void loadAndShow(ViewGroup splashContainer, long timeout, Callback callback) {
        sCallback = callback;
        splashAd.setAdListener(new AMSplashListener() {
            @Override
            public void onSplashAdLoaded() {
                Log.d(TAG, "onSplashAdLoaded");
                if (isTimeOut) return;
                if (splashAd.isAdReady()) {
                    splashAd.showSplashAd(splashContainer);
                } else {
                    // 虽然load结束，但是没有填充
                    Log.d(TAG, "load end without ad hit");
                    beforeEnd();
                }
            }

            @Override
            public void onLoadFailed() {
                Log.d(TAG, "onLoadFailed");
                if (isTimeOut) return;
                beforeEnd();
            }

            @Override
            public void onSplashAdShow(AdSource adSource) {
                Log.d(TAG, "onSplashAdShow");
                if (isTimeOut) return;
                Utils.removeUiThreadCallbacks(timeoutRunnable);
            }

            @Override
            public void onSplashAdClicked(AdSource adSource) {
                Log.d(TAG, "onSplashAdClicked");
            }

            @Override
            public void onSplashShowFail(int code, String errorMsg) {
                Log.d(TAG, "onSplashShowFail " + code + " " + errorMsg);
                if (isTimeOut) return;
                beforeEnd();
            }

            @Override
            public void onAdDismiss() {
                Log.d(TAG, "onAdDismiss");
                if (isTimeOut) return;
                beforeEnd();
            }

            @Override
            public void onAdSkip() {
                Log.d(TAG, "onAdSkip");
            }

            @Override
            public void onAdTimeOver() {
                Log.d(TAG, "onAdTimeOver");
            }
        });
        if (splashAd.isAdReady()) {
            Log.d(TAG, "splash show have cache");
            splashAd.showSplashAd(splashContainer);
        } else {
            Log.d(TAG, "splash activity load");
            splashAd.load();
        }
        isTimeOut = false;
        Utils.runOnUiThreadDelay(timeoutRunnable, timeout);
    }

    /**
     * 判断当前广告是否ready
     * @return
     */
    public static boolean isAdReady() {
        return splashAd.isAdReady();
    }

    private static void beforeEnd() {
        Utils.removeUiThreadCallbacks(timeoutRunnable);
        if (sCallback != null) {
            sCallback.onEndSplash();
        }
    }

    public interface Callback {
        void onEndSplash();
    }
}
