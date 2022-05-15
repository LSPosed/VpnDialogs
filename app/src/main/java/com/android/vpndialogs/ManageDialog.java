package com.android.vpndialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.net.IConnectivityManager;
import android.net.VpnManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.net.VpnConfig;

import org.lsposed.wsa.vpndialogs.Bridge;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class ManageDialog extends AlertActivity implements
        DialogInterface.OnClickListener, Handler.Callback {
    private static final String TAG = "VpnManage";

    private VpnConfig mConfig;

    private VpnManager mVm;
    private IConnectivityManager mService;

    private TextView mDuration;
    private TextView mDataTransmitted;
    private TextView mDataReceived;
    private boolean mDataRowsHidden;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        var userId = Bridge.UserHandle_myUserId();
        try {
            mVm = getSystemService(VpnManager.class);
            mService = IConnectivityManager.Stub.asInterface(
                    ServiceManager.getService(Context.CONNECTIVITY_SERVICE));

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                mConfig = mService.getVpnConfig(userId);
            } else {
                mConfig = Bridge.VpnManager_getVpnConfig(mVm, userId);
            }

            // mConfig can be null if we are a restricted user, in that case don't show this dialog
            if (mConfig == null) {
                finish();
                return;
            }

            View view = View.inflate(this, R.layout.manage, null);
            if (mConfig.session != null) {
                ((TextView) view.findViewById(R.id.session)).setText(mConfig.session);
            }
            mDuration = view.findViewById(R.id.duration);
            mDataTransmitted = view.findViewById(R.id.data_transmitted);
            mDataReceived = view.findViewById(R.id.data_received);
            mDataRowsHidden = true;

            if (mConfig.legacy) {
                mAlertParams.mTitle = getText(R.string.legacy_title);
            } else {
                mAlertParams.mTitle = VpnConfig.getVpnLabel(this, mConfig.user);
            }
            if (mConfig.configureIntent != null) {
                mAlertParams.mPositiveButtonText = getText(R.string.configure);
                mAlertParams.mPositiveButtonListener = this;
            }
            mAlertParams.mNeutralButtonText = getText(R.string.disconnect);
            mAlertParams.mNeutralButtonListener = this;
            mAlertParams.mNegativeButtonText = getText(android.R.string.cancel);
            mAlertParams.mNegativeButtonListener = this;
            mAlertParams.mView = view;
            setupAlert();

            if (mHandler == null) {
                mHandler = new Handler(Looper.myLooper());
            }
            mHandler.sendEmptyMessage(0);
        } catch (Exception e) {
            Log.e(TAG, "onResume", e);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (!isFinishing()) {
            finish();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        try {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                mConfig.configureIntent.send();
            } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                final int myUserId = Bridge.UserHandle_myUserId();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    if (mConfig.legacy) {
                        mService.prepareVpn(VpnConfig.LEGACY_VPN, VpnConfig.LEGACY_VPN, myUserId);
                    } else {
                        mService.prepareVpn(mConfig.user, VpnConfig.LEGACY_VPN, myUserId);
                    }
                } else {
                    if (mConfig.legacy) {
                        Bridge.VpnManager_prepareVpn(mVm, VpnConfig.LEGACY_VPN, VpnConfig.LEGACY_VPN, myUserId);
                    } else {
                        Bridge.VpnManager_prepareVpn(mVm, mConfig.user, VpnConfig.LEGACY_VPN, myUserId);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onClick", e);
            finish();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        mHandler.removeMessages(0);

        if (!isFinishing()) {
            if (mConfig.startTime != -1) {
                long seconds = (SystemClock.elapsedRealtime() - mConfig.startTime) / 1000;
                mDuration.setText(String.format(Locale.ROOT, "%02d:%02d:%02d",
                        seconds / 3600, seconds / 60 % 60, seconds % 60));
            }

            String[] numbers = getNumbers();
            if (numbers != null) {
                // First unhide the related data rows.
                if (mDataRowsHidden) {
                    findViewById(R.id.data_transmitted_row).setVisibility(View.VISIBLE);
                    findViewById(R.id.data_received_row).setVisibility(View.VISIBLE);
                    mDataRowsHidden = false;
                }

                // [1] and [2] are received data in bytes and packets.
                mDataReceived.setText(getString(R.string.data_value_format,
                        numbers[1], numbers[2]));

                // [9] and [10] are transmitted data in bytes and packets.
                mDataTransmitted.setText(getString(R.string.data_value_format,
                        numbers[9], numbers[10]));
            }
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
        return true;
    }

    private String[] getNumbers() {
        try (var in = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/net/dev")))) {
            // See dev_seq_printf_stats() in net/core/dev.c.
            String prefix = mConfig.interfaze + ':';

            while (true) {
                String line = in.readLine().trim();
                if (line.startsWith(prefix)) {
                    String[] numbers = line.substring(prefix.length()).split(" +");
                    for (int i = 1; i < 17; ++i) {
                        if (!numbers[i].equals("0")) {
                            return numbers;
                        }
                    }
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
