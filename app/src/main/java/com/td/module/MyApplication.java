package com.td.module;

import com.ads.td.ads.TdAd;
import com.ads.td.config.AdjustConfig;
import com.ads.td.config.AppsflyerConfig;
import com.ads.td.config.TdAdConfig;
import com.ads.td.application.AdsMultiDexApplication;
import com.ads.td.applovin.AppLovin;
import com.ads.td.applovin.AppOpenMax;
import com.ads.td.billing.AppPurchase;
import com.ads.td.admob.Admob;
import com.ads.td.admob.AppOpenManager;
import com.td.module.activity.MainActivity;
import com.td.module.activity.SplashActivity;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends AdsMultiDexApplication {
    private final String APPSFLYER_TOKEN = "";
    private final String ADJUST_TOKEN = "";
    private final String EVENT_PURCHASE_ADJUST = "";
    private final String EVENT_AD_IMPRESSION_ADJUST = "";
    protected StorageCommon storageCommon;
    private static MyApplication context;
    public static MyApplication getApplication() {
        return context;
    }
    public StorageCommon getStorageCommon() {
        return storageCommon;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Admob.getInstance().setNumToShowAds(0);

        storageCommon = new StorageCommon();
        initBilling();
        initAds();

    }

    private void initAds() {
        String environment = BuildConfig.env_dev ? TdAdConfig.ENVIRONMENT_DEVELOP : TdAdConfig.ENVIRONMENT_PRODUCTION;
        tdAdConfig = new TdAdConfig(this, TdAdConfig.PROVIDER_ADMOB, environment);

        AdjustConfig adjustConfig = new AdjustConfig(true,ADJUST_TOKEN);
        adjustConfig.setEventAdImpression(EVENT_AD_IMPRESSION_ADJUST);

        adjustConfig.setEventNamePurchase(EVENT_PURCHASE_ADJUST);
        tdAdConfig.setAdjustConfig(adjustConfig);

        AppsflyerConfig appsflyerConfig = new AppsflyerConfig(true,APPSFLYER_TOKEN);


        listTestDevice.add("EC25F576DA9B6CE74778B268CB87E431");
        tdAdConfig.setListDeviceTest(listTestDevice);
        tdAdConfig.setIntervalInterstitialAd(15);
        tdAdConfig.setAdjustTokenTiktok("123456");

        TdAd.getInstance().init(this, tdAdConfig, false);

        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        AppLovin.getInstance().setDisableAdResumeWhenClickAds(true);
        Admob.getInstance().setOpenActivityAfterShowInterAds(true);

        if (TdAd.getInstance().getMediationProvider() == TdAdConfig.PROVIDER_ADMOB) {
            AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        } else {
            AppOpenMax.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        }
    }

    private void initBilling() {
        List<String> listINAPId = new ArrayList<>();
        listINAPId.add(MainActivity.PRODUCT_ID);
        List<String> listSubsId = new ArrayList<>();

        AppPurchase.getInstance().initBilling(getApplication(), listINAPId, listSubsId);
    }

}
