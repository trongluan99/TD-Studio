package com.ads.td.ads.bannerAds;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.td.R;
import com.ads.td.ads.TdAd;
import com.ads.td.ads.TdAdCallback;
import com.ads.td.funtion.AdCallback;

public class TdBannerAdView extends RelativeLayout {

    private String TAG = "TdBannerAdView";

    public TdBannerAdView(@NonNull Context context) {
        super(context);
        init();
    }

    public TdBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TdBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    public TdBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_banner_control, this);
    }

    public void loadBanner(Activity activity, String idBanner) {
        loadBanner(activity, idBanner, new TdAdCallback());
    }

    public void loadBanner(Activity activity, String idBanner, TdAdCallback tdAdCallback) {
        TdAd.getInstance().loadBanner(activity, idBanner, tdAdCallback);
    }

    public void loadInlineBanner(Activity activity, String idBanner, String inlineStyle) {
        TdAd.getInstance().loadInlineBanner(activity, idBanner, inlineStyle);
    }

    public void loadInlineBanner(Activity activity, String idBanner, String inlineStyle, AdCallback adCallback) {
        TdAd.getInstance().loadInlineBanner(activity, idBanner, inlineStyle, adCallback);
    }

    public void loadBannerFragment(Activity activity, String idBanner) {
        TdAd.getInstance().loadBannerFragment(activity, idBanner, getRootView());
    }

    public void loadBannerFragment(Activity activity, String idBanner, AdCallback adCallback) {
        TdAd.getInstance().loadBannerFragment(activity, idBanner, getRootView(), adCallback);
    }

    public void loadInlineBannerFragment(Activity activity, String idBanner, String inlineStyle) {
        TdAd.getInstance().loadBannerInlineFragment(activity, idBanner, getRootView(), inlineStyle);
    }

    public void loadInlineBannerFragment(Activity activity, String idBanner, String inlineStyle, AdCallback adCallback) {
        TdAd.getInstance().loadBannerInlineFragment(activity, idBanner, getRootView(), inlineStyle, adCallback);
    }
}