package com.plutus.qmkfd.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.plutus.common.admore.AMSDK;
import com.plutus.common.admore.api.AMBanner;
import com.plutus.common.admore.api.AMInterstitial;
import com.plutus.common.admore.api.AMNative;
import com.plutus.common.admore.api.AMNativeAdView;
import com.plutus.common.admore.api.AMRewardVideoAd;
import com.plutus.common.admore.api.AdError;
import com.plutus.common.admore.beans.AdSource;
import com.plutus.common.admore.listener.AMBannerListener;
import com.plutus.common.admore.listener.AMInterstitialListener;
import com.plutus.common.admore.listener.AMNativeListener;
import com.plutus.common.admore.listener.AMRewardVideoListener;
import com.plutus.common.admore.listener.ImpressionEventListener;
import com.plutus.common.core.utils.location.LocationHelper;

import java.util.ArrayDeque;
import java.util.Deque;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AMRewardVideoAd mRewardVideoAd;
    private AMInterstitial mInterstitialAd;
    private AMNative mNativeAd;
    private AMBanner mBannerAd;
    private final Deque<AMNativeAdView> nativeViewDeque = new ArrayDeque<>();
    private final Deque<AMNativeAdView> bannerViewDeque = new ArrayDeque<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取定位信息，可以提升ecpm
        LocationHelper.get().init(this);
        LocationHelper.get().startLocating(new LocationHelper.LocationUpdatedCallback() {
            @Override
            public void onUpdated() {
                Log.d(TAG, LocationHelper.get().getLongitude());
                Log.d(TAG, LocationHelper.get().getLatitude());
                AMSDK.setCurrentLatitude(LocationHelper.get().getLatitude());
                AMSDK.setCurrentLongitude(LocationHelper.get().getLongitude());
            }
        });
        // 设置用户微信open id，可以提升优量汇广告的ecpm
        AMSDK.setWxOpenId("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationHelper.get().stopLocating();
    }

    public void loadReward(View view) {
        mRewardVideoAd = new AMRewardVideoAd(this, AdmoreConstant.REWARD_PLACEMENT_ID);
        mRewardVideoAd.setAdListener(new AMRewardVideoListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.d(TAG, "onRewardedVideoAdLoaded");
                Toast.makeText(MainActivity.this, "load结束", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRewardedVideoAdFailed(AdError adError) {
                Log.e(TAG, "onRewardedVideoAdFailed:" + adError.getFullErrorInfo());
            }

            @Override
            public void onRewardedVideoAdPlayStart(AdSource atAdInfo) {
                Log.d(TAG, "onRewardedVideoAdPlayStart");
            }

            @Override
            public void onRewardedVideoAdPlayEnd(AdSource atAdInfo) {
                Log.d(TAG, "onRewardedVideoAdPlayEnd");
            }

            @Override
            public void onRewardedVideoAdPlayFailed(AdError adError, AdSource atAdInfo) {
                //注意：禁止在此回调中执行广告的加载方法进行重试，否则会引起很多无用请求且可能会导致应用卡顿
                Log.e(TAG, "onRewardedVideoAdPlayFailed:" + adError.getFullErrorInfo());
            }

            @Override
            public void onRewardedVideoAdClosed(AdSource atAdInfo) {
                //建议在此回调中调用load进行广告的加载，方便下一次广告的展示（不需要调用isAdReady()）
                Log.d(TAG, "onRewardedVideoAdClosed");
                mRewardVideoAd.load(MainActivity.this);
            }

            @Override
            public void onReward(AdSource atAdInfo) {
                //建议在此回调中下发奖励，一般在onRewardedVideoAdClosed之前回调
                Log.d(TAG, "onReward");
            }

            @Override
            public void onRewardedVideoAdPlayClicked(AdSource atAdInfo) {
                Log.d(TAG, "onRewardedVideoAdPlayClicked");
            }
        });
        Toast.makeText(this, "开始加载...", Toast.LENGTH_LONG).show();
        // 模拟s2s传递的信息，这些信息要传递给各adn平台服务器，如果不需要s2s回调，可以不调用
        mRewardVideoAd.setS2SInfo("%7B%22request_token%22:%222877693875555cb488c07611efceaf36%22,%22version%22:215,%22ad_through%22:1%7D");
        mRewardVideoAd.load(this);
    }

    public void loadInterstitial(View view) {
        // 插屏广告
        mInterstitialAd = new AMInterstitial(this, AdmoreConstant.INTERSTITIAL_PLACEMENT_ID);
        mInterstitialAd.setAdListener(new AMInterstitialListener() {
            @Override
            public void onInterstitialAdLoaded() {
                Log.d(TAG, "inter ad loaded");
                Toast.makeText(MainActivity.this, "load结束", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onInterstitialAdClicked(AdSource var1) {
                Log.d(TAG, "inter ad clicked " + var1);
            }

            @Override
            public void onInterstitialAdShow(AdSource var1) {
                Log.d(TAG, "inter ad show " + var1);
            }

            @Override
            public void onInterstitialAdClose(AdSource var1) {
                Log.d(TAG, "inter ad close " + var1);
            }

            @Override
            public void onInterstitialAdVideoStart(AdSource var1) {
                Log.d(TAG, "inter ad video start " + var1);
            }

            @Override
            public void onInterstitialAdVideoEnd(AdSource var1) {
                Log.d(TAG, "inter ad video end " + var1);
            }

            @Override
            public void onInterstitialAdVideoError(AdError var1) {
                Log.d(TAG, "inter ad video error " + var1);
            }

            @Override
            public void onReward(AdSource adSource) {
                Log.d(TAG, "inter onReward " + adSource);
            }
        });
        mInterstitialAd.load(this);
        Toast.makeText(MainActivity.this, "开始加载...", Toast.LENGTH_LONG).show();
    }

    public void loadNative(View view) {
        // 信息流广告
        mNativeAd = new AMNative(this, AdmoreConstant.NATIVE_PLACEMENT_ID);
        mNativeAd.setAdListener(new AMNativeListener() {
            @Override
            public void onNativeAdLoaded() {
                Log.d(TAG, "onNativeAdLoaded");
                Toast.makeText(MainActivity.this, "load结束", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNativeAdClicked(AdSource adSource) {
                Log.d(TAG, "onNativeAdClicked " + adSource);
            }

            @Override
            public void onNativeAdShow(AdSource adSource) {
                Log.d(TAG, "onNativeAdShow " + adSource);
            }

            @Override
            public void onNativeAdVideoStart(AdSource adSource) {
                Log.d(TAG, "onNativeAdVideoStart " + adSource);
            }

            @Override
            public void onNativeAdVideoEnd(AdSource adSource) {
                Log.d(TAG, "onNativeAdVideoEnd " + adSource);
            }

            @Override
            public void onNativeAdVideoProgress(int progress) {
                Log.d(TAG, "onNativeAdVideoProgress " + progress);
            }

            @Override
            public void onNativeAdVideoError(AdError adSource) {
                Log.d(TAG, "onNativeAdVideoError " + adSource);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height, int adnType) {
                Log.d(TAG, "onRenderSuccess " + view + width + " " + height);
                if (!nativeViewDeque.isEmpty()) {
                    AMNativeAdView nativeAdView = nativeViewDeque.peek();
                    if (nativeAdView != null) {
                        nativeAdView.renderNativeAdToActivity(MainActivity.this, adnType, view, new ImpressionEventListener() {
                            @Override
                            public void onImpression() {
                                Log.d(TAG, "on impression");
                            }
                        });
                    }
                }
            }

            @Override
            public void onRenderFail(int code, String msg) {
                Log.d(TAG, "onRenderFail " + code + " " + msg);
            }

            @Override
            public void onDislikeRemoved() {
                removeNative(null);
            }
        });
        // 传入一个activity，每次都新建一个native view出来
        mNativeAd.load(this);
        Toast.makeText(MainActivity.this, "开始加载...", Toast.LENGTH_LONG).show();
    }

    public void isReady(View view) {
        Log.d(TAG, "is ready" + mRewardVideoAd.isAdReady());
    }

    public void show(View view) {
        mRewardVideoAd.show(this);
    }

    public void showInter(View view) {
        mInterstitialAd.show(this);
    }

    public void showNative(View view) {
        if (mNativeAd == null) {
            return;
        }
        // 保证每次show native ad，都是用一个全新的view container
        nativeViewDeque.push(new AMNativeAdView(this));
        Log.d(TAG, "deque size is " + nativeViewDeque.size());
        mNativeAd.show(this);
    }

    public void removeNative(View view) {
        AMNativeAdView nativeAdView = nativeViewDeque.pop();
        Log.d(TAG, "nativeViewDeque size after remove " + nativeViewDeque.size());
        if (nativeAdView != null) {
            nativeAdView.removeSelfFromActivity();
        }
    }

    public void loadBanner(View view) {
        // Banner广告
        mBannerAd = new AMBanner(this, AdmoreConstant.BANNER_PLACEMENT_ID);
        mBannerAd.setAdListener(new AMBannerListener() {

            @Override
            public void onBannerAdLoaded() {
                Log.d(TAG, "banner ad loaded");
                Toast.makeText(MainActivity.this, "load结束", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onBannerRenderFail(int code, String msg) {
                Log.d(TAG, "banner ad error " + code + " " + msg);
            }

            @Override
            public void onBannerAdClicked(AdSource adSource) {
                Log.d(TAG, "banner ad clicked");
            }

            @Override
            public void onBannerAdShow(AdSource adSource) {
                Log.d(TAG, "banner ad show");
            }

            @Override
            public void onRenderSuccess(View view, float width, float height, int adnType) {
                Log.d(TAG, "onRenderSuccess " + view + width + " " + height);
                if (!bannerViewDeque.isEmpty()) {
                    AMNativeAdView nativeAdView = bannerViewDeque.peek();
                    if (nativeAdView != null) {
                        nativeAdView.renderBannerAdToActivity(MainActivity.this, adnType, view, new ImpressionEventListener() {
                            @Override
                            public void onImpression() {
                                Log.d(TAG, "on impression");
                            }
                        });
                    }
                }
            }

            @Override
            public void onDislikeRemoved() {
                removeBanner(null);
            }
        });
        // 传入一个activity，每次都新建一个native view出来
        mBannerAd.load(this);
        Toast.makeText(MainActivity.this, "开始加载", Toast.LENGTH_LONG).show();
    }

    public void showBanner(View view) {
        if (mBannerAd == null) {
            return;
        }
        // 保证每次show native ad，都是用一个全新的view container
        bannerViewDeque.push(new AMNativeAdView(this));
        Log.d(TAG, "deque size is " + bannerViewDeque.size());
        mBannerAd.show(this);
    }

    public void removeBanner(View view) {
        AMNativeAdView nativeAdView = bannerViewDeque.pop();
        Log.d(TAG, "bannerViewDeque size after remove " + bannerViewDeque.size());
        if (nativeAdView != null) {
            nativeAdView.removeSelfFromActivity();
        }
    }
}