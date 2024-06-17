package com.td.module;

import com.ads.td.admob.Admob;
import com.ads.td.admob.AppOpenManager;
import com.ads.td.ads.TdAd;
import com.ads.td.application.AdsMultiDexApplication;
import com.ads.td.billing.AppPurchase;
import com.ads.td.config.AdjustConfig;
import com.ads.td.config.TdAdConfig;

import java.util.ArrayList;
import java.util.List;

public class App extends AdsMultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        initAds();
        initBilling();
    }

    private void initAds() {
        String environment = BuildConfig.DEBUG ? TdAdConfig.ENVIRONMENT_DEVELOP : TdAdConfig.ENVIRONMENT_PRODUCTION;
        mTdAdConfig = new TdAdConfig(this, environment);

        AdjustConfig adjustConfig = new AdjustConfig(true, getString(R.string.adjust_token));
        mTdAdConfig.setAdjustConfig(adjustConfig);
        mTdAdConfig.setFacebookClientToken(getString(R.string.facebook_client_token));
        mTdAdConfig.setAdjustTokenTiktok(getString(R.string.tiktok_token));

        mTdAdConfig.setIdAdResume("");

        TdAd.getInstance().init(this, mTdAdConfig);
        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        Admob.getInstance().setOpenActivityAfterShowInterAds(true);
        AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity.class);
    }

    private void initBilling(){
        List<String> listIAP = new ArrayList<>();
        listIAP.add("android.test.purchased");
        List<String> listSub = new ArrayList<>();
        AppPurchase.getInstance().initBilling(this, listIAP, listSub);
    }
}
