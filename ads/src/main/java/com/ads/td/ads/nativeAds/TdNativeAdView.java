package com.ads.td.ads.nativeAds;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.td.R;
import com.ads.td.ads.TdAd;
import com.ads.td.ads.TdAdCallback;
import com.ads.td.ads.wrapper.ApNativeAd;
import com.facebook.shimmer.ShimmerFrameLayout;

public class TdNativeAdView extends RelativeLayout {

    private int layoutCustomNativeAd = 0;
    private ShimmerFrameLayout layoutLoading;
    private FrameLayout layoutPlaceHolder;
    private String TAG = "TdNativeAdView";

    public TdNativeAdView(@NonNull Context context) {
        super(context);
        init();
    }

    public TdNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TdNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    public TdNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TdNativeAdView, 0, 0);
        // Get layout native view custom and  layout loading
        layoutCustomNativeAd = typedArray.getResourceId(R.styleable.TdNativeAdView_layoutCustomNativeAd, 0);
        int idLayoutLoading = typedArray.getResourceId(R.styleable.TdNativeAdView_layoutLoading, 0);
        if (idLayoutLoading != 0)
            layoutLoading = (ShimmerFrameLayout) LayoutInflater.from(getContext()).inflate(idLayoutLoading, null);

        init();
    }

    private void init() {
        layoutPlaceHolder = new FrameLayout(getContext());
        addView(layoutPlaceHolder);
        if (layoutLoading != null)
            addView(layoutLoading);

    }

    public void setLayoutCustomNativeAd(int layoutCustomNativeAd) {
        this.layoutCustomNativeAd = layoutCustomNativeAd;
    }

    public void setLayoutLoading(int idLayoutLoading) {
        this.layoutLoading = (ShimmerFrameLayout) LayoutInflater.from(getContext()).inflate(idLayoutLoading, null);
        addView(layoutLoading);
    }

    public void populateNativeAdView(Activity activity, ApNativeAd nativeAd){
        if(layoutLoading == null){
            Log.e(TAG, "populateNativeAdView error : layoutLoading not set"  );
            return;
        }
        TdAd.getInstance().populateNativeAdView(activity, nativeAd, layoutPlaceHolder, layoutLoading);
    }

    public void loadNativeAd(Activity activity, String idAd ) {
        loadNativeAd(activity, idAd, new TdAdCallback(){});
    }
    public void loadNativeAd(Activity activity, String idAd, TdAdCallback tdAdCallback) {
        if(layoutLoading == null){
            setLayoutLoading(R.layout.loading_native_medium);
        }
        if (layoutCustomNativeAd == 0){
            layoutCustomNativeAd = R.layout.custom_native_admod_medium_rate;
            setLayoutCustomNativeAd(layoutCustomNativeAd);
        }
        TdAd.getInstance().loadNativeAd(activity, idAd, layoutCustomNativeAd, layoutPlaceHolder, layoutLoading, tdAdCallback);
    }

    public void loadNativeAd(Activity activity, String idAd, int layoutCustomNativeAd, int idLayoutLoading) {
        setLayoutLoading(idLayoutLoading);
        setLayoutCustomNativeAd(layoutCustomNativeAd);
        loadNativeAd(activity,idAd);
    }

    public void loadNativeAd(Activity activity, String idAd, int layoutCustomNativeAd, int idLayoutLoading, TdAdCallback tdAdCallback) {
        setLayoutLoading(idLayoutLoading);
        setLayoutCustomNativeAd(layoutCustomNativeAd);
        loadNativeAd(activity,idAd, tdAdCallback);
    }
}