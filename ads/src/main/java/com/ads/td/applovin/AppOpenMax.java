package com.ads.td.applovin;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.ads.td.ads.TdAdCallback;
import com.ads.td.ads.wrapper.ApAdError;
import com.ads.td.billing.AppPurchase;
import com.ads.td.dialog.ResumeLoadingDialog;
import com.ads.td.event.TdLogEventManager;
import com.ads.td.funtion.AdType;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;

import java.util.ArrayList;
import java.util.List;

public class AppOpenMax implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private static final String TAG = "AppOpenMax";
    private MaxAppOpenAd appOpenAd;
    private Application myApplication;
    private static volatile AppOpenMax INSTANCE;
    private Activity currentActivity;
    private Dialog dialog = null;
    private final List<Class> disabledAppOpenList;
    private boolean isAppResumeEnabled = true;
    private boolean isInterstitialShowing = false;
    private boolean disableAdResumeByClickAction = false;
    private boolean displayAdResume = false;
    private boolean isInitialized = false; // on  - off ad resume on app
    private TdAdCallback tdAdCallback;

    private boolean isShowAppOpenSplash = false;
    private boolean isTimeOut = false;
    private Runnable rTimeout;
    private Handler hTimeout;
    private MaxAppOpenAd appOpenSplash;

    private MaxAppOpenAd openAdSplash;
    private boolean isShowingAd = false;


    public static synchronized AppOpenMax getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppOpenMax();
        }
        return INSTANCE;
    }

    private AppOpenMax() {
        disabledAppOpenList = new ArrayList<>();
    }

    public void init(Application application, String appOpenAdId) {
        isInitialized = true;
        disableAdResumeByClickAction = false;
        this.myApplication = application;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        loadAdResumeMax(application, appOpenAdId);
    }

    // Load App Open Splash
    public void initOpenSplash(Activity activity, String idOpenSplash, int timeOut, TdAdCallback tdAdCallback) {
        AppLovinSdk.initializeSdk(activity, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(AppLovinSdkConfiguration config) {
                appOpenSplash = new MaxAppOpenAd(idOpenSplash, activity);
                showAdSplashIfReady(activity, timeOut, tdAdCallback);
            }
        });
    }

    public void showAdSplashIfReady(Activity activity, int timeOut, TdAdCallback tdAdCallback) {
        isTimeOut = false;
        hTimeout = new Handler();
        rTimeout = new Runnable() {
            @Override
            public void run() {
                isTimeOut = true;
                if (appOpenSplash.isReady()) {
                    appOpenSplash.showAd();
                    return;
                }

                if (tdAdCallback != null) {
                    tdAdCallback.onAdClosed();
                    isShowAppOpenSplash = false;
                }
            }
        };
        hTimeout.postDelayed(rTimeout, timeOut);

        appOpenSplash.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                if (!isTimeOut) {
                    showOpenSplash(activity, tdAdCallback);
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {

            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
            }
        });
        appOpenSplash.loadAd();
    }

    private void showOpenSplash(Activity activity, TdAdCallback tdAdCallback) {
        if (hTimeout != null && rTimeout != null) {
            hTimeout.removeCallbacks(rTimeout);
        }
        if (appOpenSplash.isReady()) {
            appOpenSplash.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    tdAdCallback.onAdLoaded();
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {

                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    Log.d(TAG, "onInterstitialAdClosed: ");
                    try {
                        if (dialog != null && !(activity).isDestroyed())
                            dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (tdAdCallback != null) {
                        tdAdCallback.onAdClosed();
                    }
                }

                @Override
                public void onAdClicked(MaxAd ad) {
                    if (tdAdCallback != null) {
                        tdAdCallback.onAdClicked();
                    }
                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    dismissDialogLoading();
                    if (tdAdCallback != null) {
                        tdAdCallback.onAdFailedToLoad(new ApAdError(error));
                    }
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    appOpenAd.loadAd();
                    dismissDialogLoading();
                    if (tdAdCallback != null) {
                        tdAdCallback.onAdFailedToShow(new ApAdError(error));
                    }
                }
            });

            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    dialog = new ResumeLoadingDialog(activity);
                    try {
                        dialog.show();
                    } catch (Exception e) {
                        tdAdCallback.onAdClosed();
                        tdAdCallback.onNextAction();
                        return;
                    }
                } catch (Exception e) {
                    dialog = null;
                    e.printStackTrace();
                }
                new Handler().postDelayed(() -> {
                    appOpenSplash.showAd();
                }, 800);
            }
        }
    }

    // Policy Max: Load sau Ad Splash
    public void loadAdResumeMax(Application application, String appOpenAdId) {
        appOpenAd = new MaxAppOpenAd(appOpenAdId, application);
        appOpenAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onAdLoaded: ");
                if (tdAdCallback != null) {
                    tdAdCallback.onAdLoaded();
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                displayAdResume = true;
                Log.d(TAG, "onAdDisplayed: ");
                if (tdAdCallback != null) {
                    tdAdCallback.onAdImpression();
                }
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.d(TAG, "onAdHidden: ");
                appOpenAd.loadAd();
                dismissDialogLoading();
                displayAdResume = false;
                if (tdAdCallback != null) {
                    tdAdCallback.onAdClosed();
                }
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.d(TAG, "onAdClicked: ");
                disableAdResumeByClickAction = true;
                if (tdAdCallback != null) {
                    tdAdCallback.onAdClicked();
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.d(TAG, "onAdLoadFailed: ");
                dismissDialogLoading();
                if (tdAdCallback != null) {
                    tdAdCallback.onAdFailedToLoad(new ApAdError(error));
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.d(TAG, "onAdDisplayFailed: ");
                appOpenAd.loadAd();
                dismissDialogLoading();
                if (tdAdCallback != null) {
                    tdAdCallback.onAdFailedToShow(new ApAdError(error));
                }
            }
        });
        appOpenAd.loadAd();
    }

    /**
     * Disable app open app on specific activity
     *
     * @param activityClass
     */
    public void disableAppResumeWithActivity(Class activityClass) {
        Log.d(TAG, "disableAppResumeWithActivity: " + activityClass.getName());
        disabledAppOpenList.add(activityClass);
    }

    public void enableAppResumeWithActivity(Class activityClass) {
        Log.d(TAG, "enableAppResumeWithActivity: " + activityClass.getName());
        disabledAppOpenList.remove(activityClass);
    }

    public void disableAppResume() {
        isAppResumeEnabled = false;
    }

    public void enableAppResume() {
        isAppResumeEnabled = true;
    }

    public boolean isInterstitialShowing() {
        return isInterstitialShowing;
    }

    public void setInterstitialShowing(boolean interstitialShowing) {
        isInterstitialShowing = interstitialShowing;
    }

    /**
     * Call disable ad resume when click a button, auto enable ad resume in next start
     */
    public void disableAdResumeByClickAction() {
        disableAdResumeByClickAction = true;
    }

    public void setDisableAdResumeByClickAction(boolean disableAdResumeByClickAction) {
        this.disableAdResumeByClickAction = disableAdResumeByClickAction;
    }

    public void setAppOpenMaxCallback(TdAdCallback tdAdCallback) {
        this.tdAdCallback = tdAdCallback;
    }


    private void showAdIfReady() {
        if (appOpenAd == null
                || !AppLovinSdk.getInstance(myApplication).isInitialized()
                || currentActivity == null
                || AppPurchase.getInstance().isPurchased(currentActivity)
        ) {
            return;
        }
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)
                && isNetworkAvailable()
        ) {
            try {
                dismissDialogLoading();
                dialog = new ResumeLoadingDialog(currentActivity);
                try {
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "showAdIfReady: " + e.getMessage());
            }
            if (appOpenAd.isReady()) {
                new Handler().postDelayed(() -> appOpenAd.showAd(), 500);
            } else {
                appOpenAd.loadAd();
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onResume() {
        if (!isAppResumeEnabled) {
            Log.d(TAG, "onResume: app resume is disabled");
            return;
        }

        if (disableAdResumeByClickAction) {
            Log.d(TAG, "onResume:ad resume disable ad by action");
            disableAdResumeByClickAction = false;
            return;
        }

        if (isInterstitialShowing) {
            Log.d(TAG, "onResume: interstitial is showing");
            return;
        }

        if (displayAdResume) {
            Log.d(TAG, "onResume: AppOpen is showing");
            return;
        }


        try {
            for (Class activity : disabledAppOpenList) {
                if (activity.getName().equals(currentActivity.getClass().getName())) {
                    Log.d(TAG, "onStart: activity is disabled");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        showAdIfReady();
    }

    private void dismissDialogLoading() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
        Log.d(TAG, "onActivityStarted: " + currentActivity);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
        Log.d(TAG, "onActivityResumed: " + currentActivity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
        Log.d(TAG, "onActivityDestroyed: null");
    }

    public void loadOpenMaxSplash(Context context, String idOpenSplash, int timeDelay, int timeOut, boolean isShowAdIfReady, AppLovinCallback adCallback) {
        if (!isNetworkAvailable()) {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    adCallback.onNextAction();
                }
            }, timeDelay);
        } else {
            final long currentTimeMillis = System.currentTimeMillis();
            final Runnable timeOutRunnable = () -> {
                adCallback.onNextAction();
                isShowingAd = false;
            };
            final Handler handler = new Handler();
            handler.postDelayed(timeOutRunnable, timeOut);
            openAdSplash = new MaxAppOpenAd(idOpenSplash, context);
            openAdSplash.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd maxAd) {
                    handler.removeCallbacks(timeOutRunnable);
                    if (isShowAdIfReady) {
                        long elapsedTime = System.currentTimeMillis() - currentTimeMillis;
                        if (elapsedTime >= timeDelay) {
                            elapsedTime = 0L;
                        }

                        Handler handler1 = new Handler();
                        Runnable showAppOpenSplashRunnable = () -> {
                            showAdIfReady(context, adCallback);
                        };
                        handler1.postDelayed(showAppOpenSplashRunnable, elapsedTime);
                    } else {
                        adCallback.onAdSplashReady();
                    }
                }

                @Override
                public void onAdDisplayed(MaxAd maxAd) {
                    Log.d(TAG, "onAdDisplayed: ");
                }

                @Override
                public void onAdHidden(MaxAd maxAd) {
                    Log.d(TAG, "onAdHidden: ");
                }

                @Override
                public void onAdClicked(MaxAd maxAd) {
                    Log.d(TAG, "onAdClicked: ");
                }

                @Override
                public void onAdLoadFailed(String s, MaxError maxError) {
                    Log.d(TAG, "onAdLoadFailed: ");
                    openAdSplash.loadAd();
                }

                @Override
                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
                    Log.d(TAG, "onAdDisplayFailed: ");
                }
            });
            openAdSplash.loadAd();
        }
    }

    private void showAdIfReady(Context context, AppLovinCallback adCallback) {
        if (!AppLovinSdk.getInstance(context).isInitialized()) {
            if (adCallback != null) {
                adCallback.onNextAction();
                return;
            }
            return;
        }

        if (openAdSplash.isReady()) {
            openAdSplash.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd maxAd) {
                    if (adCallback != null) {
                        adCallback.onAdLoaded();
                    }
                }

                @Override
                public void onAdDisplayed(MaxAd maxAd) {
                    isShowingAd = true;
                    if (adCallback != null) {
                        adCallback.onAdDisplayed();
                    }
                }

                @Override
                public void onAdHidden(MaxAd maxAd) {
                    if (adCallback != null) {
                        adCallback.onNextAction();
                    }
                }

                @Override
                public void onAdClicked(MaxAd maxAd) {
                    if (adCallback != null) {
                        adCallback.onAdClicked();
                    }
                }

                @Override
                public void onAdLoadFailed(String s, MaxError maxError) {
                    if (adCallback != null) {
                        adCallback.onAdLoadFailed();
                    }
                }

                @Override
                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
                    if (adCallback != null) {
                        adCallback.onAdDisplayFailed();
                    }
                }
            });
            openAdSplash.setRevenueListener(new MaxAdRevenueListener() {
                @Override
                public void onAdRevenuePaid(MaxAd maxAd) {
                    TdLogEventManager.logPaidAdImpression(context, maxAd, AdType.APP_OPEN);
                }
            });
            openAdSplash.showAd();
        } else {
            openAdSplash.loadAd();
        }
    }

    public void onCheckShowSplashWhenFail(AppCompatActivity activity, AppLovinCallback adCallback, int timeDelay) {
        (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
            public void run() {
                if (openAdSplash != null && !isShowingAd) {
                    showAdIfReady(activity, adCallback);
                }

            }
        }, timeDelay);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) myApplication.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
