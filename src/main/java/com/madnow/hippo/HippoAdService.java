package com.madnow.hippo;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.util.Log;

import com.hippo.ads.api.HippoAdSdk;
import com.hippo.ads.bean.BannerPosition;
import com.hippo.ads.listener.IAdsListener;
import com.hippo.ads.listener.IHippoSDKInitListener;
import com.hippo.ads.style.NativeExpressStyle;
import com.wogame.cinterface.AdInterface;
import com.wogame.common.AppMacros;
import com.wogame.util.AppInfoUtil;

/**
 * 需要在主app 的 assets里加入hippo-ads-config.json文件
 * 注释lib model 也可以放，但考虑公用库就放入主app model 也是一样）
 */
public class HippoAdService extends AdInterface {
    private static final String TAG = "HippoAdService";

    private static HippoAdService hippoAdService = null;
    private Activity mActivity;
    private static String nPlaceId;
    private static String REWARD_ID = "";
    private static String FULLSCREENVIDEO_ID = "";
    private static String BANNER_ID = "";
    private static String SPLASHAD_ID = "";
    private static String NATIVE_ID = "";

    private int mNativeExpressViewY = 0;
    private int mNativeExpressViewX = 0;
    private final float Screen_W = 1080;
    private final float Screen_H = 1920;
    private float mRScreenW = 0;
    private float gameAdW = 988; // 1080 设计屏幕宽度
    private float gameAdH = 705;
    private float mPasX;
    private float mPasY;
    private boolean mIsVideoComplete = false;

    private Resources mResources;

    public static HippoAdService getInstance() {
        if (hippoAdService == null) {
            hippoAdService = new HippoAdService();
            hippoAdService.setDelegate(hippoAdService);
        }
        return hippoAdService;
    }

    public void initWithApplication(Application app) {
    }

    public void initActivity(Activity activity,
                             final String splashId,
                             final String rewardId,
                             final String fullScreenId,
                             final String bannerId) {
        initActivity(activity,splashId,rewardId,fullScreenId,bannerId,"", 0,0, null);
    }

    public void initActivity(Activity activity,
                             final String splashId,
                             final String rewardId,
                             final String fullScreenId,
                             final String bannerId,
                             final String nativeId, final int w,final int h, final Resources resources) {
        mActivity = activity;
        SPLASHAD_ID = splashId;
        REWARD_ID = rewardId;
        FULLSCREENVIDEO_ID = fullScreenId;
        BANNER_ID = bannerId;
        NATIVE_ID = nativeId;
        mResources = resources;
        gameAdW = w;
        gameAdH = h;
        initData();
    }

    private void initData() {
        // （可选）需要服务器校验激励视频广告奖励回调时必须调用，不需要服务器校验时注释；
//        HippoAdSdk.setUserId("123");
        // （可选）开启服务器校验激励视频广告奖励回调机制，不需要服务器校验时注释；
//        HippoAdSdk.setShouldVerifyRewarded();
        // 使用本地广告配置，需要服务器下发广告配置时注释；
        HippoAdSdk.setJsonConfigFromLocal();
        // 打开调试日志，上线时注释
        HippoAdSdk.openDebugLog();
        // 初始化广告
        HippoAdSdk.init(mActivity, new IHippoSDKInitListener() {
            @Override
            public void onFailure(int i, String s) {
                Log.d(TAG, "HippiSDK 初始化失败：errorCode=" + i + ", errorMessage:" + s);
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "HippiSDK 初始化成功");
//                if (FilesUtil.getSettingNote(mActivity, "onLoad") == "0") {
//                    FilesUtil.saveSettingNote(mActivity, "onLoad", "1");
//                    Log.d(TAG, "HippiSDK 初始化成功2");
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            /**
//                             *要执行的操作
//                             */
//                            onLoad();
//                        }
//                    }, 2000);//2秒后执行Runnable中的run方法
//                } else {
                    Log.d(TAG, "HippiSDK 初始化成功3");
                    onLoad();
//                }

            }
        });


        HippoAdSdk.setAdsListener(new IAdsListener() {
            @Override
            public void hippoAdsLoaded(String hippoAdsId, String adsType) {
                Log.d(TAG, "MainActivity adsType:" + adsType + " 加载成功");
                if (mCallBack != null) {
                    mCallBack.onAdStatusListen(getType(adsType), AppMacros.CALL_AD_LOADED, nPlaceId, hippoAdsId, "", "");
                }
            }

            @Override
            public void hippoAdsShown(String hippoAdsId, String adsType) {
                Log.d(TAG, "MainActivity adsType:" + adsType + " 展示成功");
                if (mCallBack != null) {
                    mCallBack.onAdStatusListen(getType(adsType), AppMacros.CALL_AD_SHOW, nPlaceId, hippoAdsId, "", "");
                }
            }

            @Override
            public void hippoAdsClicked(String hippoAdsId, String adsType) {
                Log.d(TAG, "MainActivity adsType:" + adsType + " 点击成功");
                if (mCallBack != null) {
                    mCallBack.onAdStatusListen(getType(adsType), AppMacros.CALL_AD_CLICK, nPlaceId, hippoAdsId, "", "");
                }
            }

            @Override
            public void hippoAdsClosed(String hippoAdsId, String adsType, String extraJson) {
                Log.d(TAG, "MainActivity adsType:" + adsType + " 关闭成功");
                if (adsType == "rewardVideo") {
                    if (mIsVideoComplete) {
                        if (mCallBack != null) {
                            mCallBack.onCallBack(AppMacros.AT_RewardVideo, AppMacros.CALL_SUCCESS, nPlaceId,hippoAdsId,"","");
                            mCallBack.onAdStatusListen(AppMacros.AT_RewardVideo, AppMacros.CALL_SUCCESS, nPlaceId, hippoAdsId, "", "");
                        }
                    } else {
                        if (mCallBack != null) {
                            mCallBack.onCallBack(AppMacros.AT_RewardVideo, AppMacros.CALL_CANCEL, nPlaceId,"","","");
                            mCallBack.onAdStatusListen(AppMacros.AT_RewardVideo, AppMacros.CALL_CANCEL, nPlaceId, hippoAdsId, "", "");
                        }
                    }
                    mIsVideoComplete = false;
                }
                else {
                    int type = getType(adsType);
                    if (mCallBack != null) {
                        mCallBack.onCallBack(type,AppMacros.CALL_CANCEL, nPlaceId,hippoAdsId,"","");
                        mCallBack.onAdStatusListen(type, AppMacros.CALL_FALIED, nPlaceId, hippoAdsId, "", "");
                    }
                }
            }

            @Override
            public void hippoAdsVideoComplete(String hippoAdsId, String adsType, String extraJson) {
                Log.d(TAG, "MainActivity adsType:" + adsType + " 奖励");
                if (adsType == "rewardVideo") {
                    mIsVideoComplete = true;
                }
            }

            @Override
            public void hippoAdsError(String hippoAdsId, String adsType, int code, String message) {
                Log.d("TAG", "MainActivity adsType:" + adsType + " 加载失败 errCode:" + code + ", message:" + message);
            }
        });
    }

    private int getType(final String adsType){
        int type = 0;
        if(adsType == "rewardVideo"){
            type = AppMacros.AT_RewardVideo;
        }
        else  if(adsType == "fullScreenVideo"){
            type = AppMacros.AT_Interstitial;
        }
        return type;
    }

    private void onLoad() {
        //加载并展示开屏广告
        long time = AppInfoUtil.getFirstInstallTime(mActivity);
        long tttt = System.currentTimeMillis();
        long tttt3 = tttt - time;
        Log.d(TAG, "MainActivity adsType:" + tttt3);
//        if(tttt3 > 20*1000){
        if(!SPLASHAD_ID.isEmpty()) HippoAdSdk.loadAndShowSplashAd(SPLASHAD_ID);
//        }
//        else {
//            Log.d("hippo_sdk", "MainActivity adsType333:" + tttt3);
//        }
        // 加载激励视频广告
        if(!REWARD_ID.isEmpty()) HippoAdSdk.loadRewardVideoAds(REWARD_ID);
        // 加载全屏视频广告
        if(!FULLSCREENVIDEO_ID.isEmpty()) HippoAdSdk.loadFullScreenVideoAds(FULLSCREENVIDEO_ID);

        // 多态加载横幅广告
        // 第一种方法：json配置控制banner高度
        if(!BANNER_ID.isEmpty()) HippoAdSdk.loadBannerAds(BANNER_ID);
        // 第二种方法：横幅广告可支持的多种尺寸比例:600*300，600*400，600*500，600*260，600*90，600*150，640*100，690*388
        //HippoAdSdk.loadBannerAds(BANNER_ID, int width, int height);
        // 加载插页广告
        //HippoAdSdk.loadInterstitialAds(String hippoAdsId);
        //加载信息流广告
        if(!NATIVE_ID.isEmpty()){
            NativeExpressStyle style = new NativeExpressStyle();
            style.setRadius(20);
            mRScreenW = mResources.getDisplayMetrics().widthPixels;
            float heightPixels = mResources.getDisplayMetrics().heightPixels;
            mPasX = mRScreenW / Screen_W;
            mPasY = heightPixels / Screen_H;
            float expressViewWidth = gameAdW * mPasX;//UIUtils.px2dip(mActivity, gameAdW * mPasX);
            float expressViewHeight = gameAdH * mPasY;//UIUtils.px2dip(mActivity, gameAdH * mPasY);
            HippoAdSdk.loadNativeExpressAd(NATIVE_ID, (int) expressViewWidth, (int) expressViewHeight, style);
        }
    }
/****************************************************************************************************/

    /****************************************************************************************************/

    public void showAd(final int type, final String placeId, final int x, final int y) {
        Log.d(TAG, "showVideo:" + type + " placeId:" + placeId);
        nPlaceId = placeId;
        boolean isFailure = false;
        if (type == AppMacros.AT_RewardVideo) {
            if (HippoAdSdk.isLoaded(REWARD_ID)) {
                HippoAdSdk.showRewardVideoAds(REWARD_ID, mActivity);
            } else {
                isFailure = true;
            }
        } else if (type == AppMacros.AT_Banner_Bottom) {
            if (HippoAdSdk.isLoaded(BANNER_ID)) {
                HippoAdSdk.showBannerAds(mActivity, BANNER_ID, BannerPosition.BOTTOM);
            }
            else {
                isFailure = true;
            }
        } else if (type == AppMacros.AT_Interstitial) {
            if (HippoAdSdk.isLoaded(FULLSCREENVIDEO_ID)) {
                HippoAdSdk.showFullScreenVideoAds(FULLSCREENVIDEO_ID, mActivity);
            }
            else {
                isFailure = true;
            }
        } else if (type == AppMacros.AT_Native) {
            if (HippoAdSdk.isLoaded(NATIVE_ID)) {
                mNativeExpressViewY = y;
                mNativeExpressViewX = 0;
                //设置信息流广告位置
                HippoAdSdk.setNativeExpressPosition(NATIVE_ID, mNativeExpressViewX, mNativeExpressViewY);
                //展示信息流广告
                HippoAdSdk.showNativeExpressAd(NATIVE_ID, mActivity);
            }
            else {
                isFailure = true;
            }
        }

        if (mCallBack != null && isFailure) {
            mCallBack.onCallBack(type, AppMacros.CALL_FALIED, nPlaceId,"","","");
        }
    }

    public void closeAd(final int type) {
        Log.d(TAG, "closeAd:" + type);
        if (type == AppMacros.AT_Banner_Bottom) {
            HippoAdSdk.hideBanner();
        } else if (type == AppMacros.AT_Native) {
            //隐藏信息流广告
            HippoAdSdk.hideNativeExpressAd();
        }
    }

    public boolean checkAD(final int type, final String placeId) {
        if (type == AppMacros.AT_Interstitial) {
            if (HippoAdSdk.isLoaded(FULLSCREENVIDEO_ID)) {
                return true;
            }
        }
        return false;
    }


    public void onResume() {
        HippoAdSdk.onAppResume(mActivity);
    }

    public void onPause() {
        HippoAdSdk.onAppPause(mActivity);
    }
}
