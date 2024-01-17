package com.td.module.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;

import com.ads.td.ads.TdAd;
import com.ads.td.config.TdAdConfig;
import com.ads.td.ads.nativeAds.TdAdPlacer;
import com.ads.td.ads.nativeAds.TdAdAdapter;
import com.ads.td.ads.wrapper.ApAdValue;
import com.td.module.R;
import com.td.module.adapter.ListSimpleAdapter;

import java.util.ArrayList;
import java.util.List;

public class SimpleListActivity extends AppCompatActivity {
    private static final String TAG = "SimpleListActivity";
    TdAdAdapter adAdapter;
    int layoutCustomNative = com.ads.td.R.layout.custom_native_admod_medium;
    String idNative = "";
    SwipeRefreshLayout swRefresh;
    ListSimpleAdapter originalAdapter;
    RecyclerView recyclerView;
    TdAdPlacer.Listener listener = new TdAdPlacer.Listener() {
        @Override
        public void onAdLoaded(int i) {
            Log.i(TAG, "onAdLoaded native list: " + i);
        }

        @Override
        public void onAdRemoved(int i) {
            Log.i(TAG, "onAdRemoved: " + i);
        }

        @Override
        public void onAdClicked() {

        }

        @Override
        public void onAdRevenuePaid(ApAdValue adValue) {

        }

        @Override
        public void onAdImpression() {

        }

    };

    private List<String> sampleData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);
        swRefresh = findViewById(R.id.swRefresh);
        addSampleData();
        // init adapter origin
        originalAdapter = new ListSimpleAdapter(new ListSimpleAdapter.Listener() {
            @Override
            public void onRemoveItem(int pos) {
//                adAdapter.notifyItemRemoved(adAdapter.getOriginalPosition(pos));
                adAdapter.getAdapter().notifyDataSetChanged();
//                setupAdAdapter();
            }
        });
        originalAdapter.setSampleData(sampleData);
        recyclerView = findViewById(R.id.rvListSimple);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (TdAd.getInstance().getMediationProvider() == TdAdConfig.PROVIDER_ADMOB) {
            layoutCustomNative = com.ads.td.R.layout.custom_native_admod_medium;
            idNative = getString(R.string.admod_native_id);
        } else {
            layoutCustomNative = com.ads.td.R.layout.custom_native_max_small;
            idNative = getString(R.string.applovin_test_native);
        }

        setupAdAdapter();
        swRefresh.setOnRefreshListener(() -> {
            sampleData.add("Item add new");
            adAdapter.getAdapter().notifyDataSetChanged();
            swRefresh.setRefreshing(false);
        });
    }

    private void setupAdAdapter() {
        adAdapter = TdAd.getInstance().getNativeRepeatAdapter(this, idNative, layoutCustomNative, com.ads.td.R.layout.layout_native_medium,
                originalAdapter, listener, 5);

        recyclerView.setAdapter(adAdapter.getAdapter());
        adAdapter.loadAds();
    }

    private void addSampleData() {
        for (int i = 0; i < 30; i++) {
            sampleData.add("Item " + i);
        }
    }

    @Override
    public void onDestroy() {
        adAdapter.destroy();
        super.onDestroy();
    }
}