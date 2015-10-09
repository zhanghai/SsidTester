/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.zjuqsc.android.ssidtester;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final int UPDATE_SSID_INTERVAL_MS = 500;

    private static final String SSID_ZJUWLAN = "ZJUWLAN";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.ssid)
    TextView mSsidText;
    @Bind(R.id.is_zjuwlan)
    TextView mIsZjuwlanText;
    @Bind(R.id.timestamp)
    TextView mTimestampText;
    @Bind(R.id.fab)
    FloatingActionButton mFab;

    private String mSsid;
    private boolean mIsZjuwlan;
    private ZonedDateTime mTimestamp;

    private DateTimeFormatter mTimestampFormatter;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateSsidRunnable = new Runnable() {
        @Override
        public void run() {
            updateSsid();
            mHandler.postDelayed(this, UPDATE_SSID_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copySsidToClipboard();
                Snackbar.make(view, R.string.copied_to_clipboard, Snackbar.LENGTH_LONG).show();
            }
        });

        mTimestampFormatter = DateTimeFormatter.ofPattern(getString(R.string.timestamp_pattern));

        if (savedInstanceState == null) {
            final Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_in);
            animation.setInterpolator(new FastOutSlowInInterpolator());
            // Crazy support library resets our visibility and alpha if we use layout_anchor.
            //mFab.setVisibility(View.INVISIBLE);
            mFab.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            mFab.getViewTreeObserver().removeOnPreDrawListener(this);
                            mFab.setVisibility(View.INVISIBLE);
                            return true;
                        }
            });
            // Delay 350ms for activity_open_enter animation.
            mFab.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mFab.startAnimation(animation);
                }
            }, 350);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHandler.post(mUpdateSsidRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHandler.removeCallbacks(mUpdateSsidRunnable);
    }

    private void updateSsid() {

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        mSsid = wifiManager.getConnectionInfo().getSSID();
        mIsZjuwlan = mSsid != null && mSsid.contains(SSID_ZJUWLAN);
        mTimestamp = ZonedDateTime.now();

        boolean ssidIsEmpty = TextUtils.isEmpty(mSsid);
        mSsidText.setText(ssidIsEmpty ? getString(R.string.ssid_empty) : mSsid);
        mSsidText.setAlpha(ssidIsEmpty ? 0.6f : 1);
        mIsZjuwlanText.setText(mIsZjuwlan ? R.string.is_zjuwlan_true : R.string.is_zjuwlan_false);
        mTimestampText.setText(mTimestampFormatter.format(mTimestamp));
    }

    private void copySsidToClipboard() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String label = getString(R.string.app_name);
        String text = String.format("ssid=\"%s\", isZjuwlan=%b, timestamp=%s", mSsid, mIsZjuwlan,
                mTimestamp);
        ClipData clipData = ClipData.newPlainText(label, text);
        clipboardManager.setPrimaryClip(clipData);
    }
}
