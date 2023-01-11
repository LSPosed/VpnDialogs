package com.android.vpndialogs;

import static com.android.vpndialogs.Constant.ALWAYS_ON_VPN_APP;
import static com.android.vpndialogs.Constant.TYPE_VPN_SERVICE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.IConnectivityManager;
import android.net.VpnManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.provider.Settings;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.net.VpnConfig;

import org.lsposed.wsa.vpndialogs.Bridge;

public class ConfirmDialog extends AlertActivity
        implements DialogInterface.OnClickListener, ImageGetter {
    private static final String TAG = "VpnConfirm";

    private final int mVpnType;

    private String mPackage;

    private VpnManager mVm;
    private IConnectivityManager mService;

    public ConfirmDialog() {
        this(TYPE_VPN_SERVICE);
    }

    public ConfirmDialog(int vpnType) {
        mVpnType = vpnType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPackage = getCallingPackage();
        mService = IConnectivityManager.Stub.asInterface(
                ServiceManager.getService(Context.CONNECTIVITY_SERVICE));
        mVm = getSystemService(VpnManager.class);

        if (prepareVpn()) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        if (getSystemService(UserManager.class).hasUserRestriction(UserManager.DISALLOW_CONFIG_VPN)) {
            finish();
            return;
        }
        final String alwaysOnVpnPackage = Settings.Secure.getString(getContentResolver(), ALWAYS_ON_VPN_APP);
        // Can't prepare new vpn app when another vpn is always-on
        if (alwaysOnVpnPackage != null && !alwaysOnVpnPackage.equals(mPackage)) {
            finish();
            return;
        }
        View view = View.inflate(this, R.layout.confirm, null);
        ((TextView) view.findViewById(R.id.warning)).setText(
                Html.fromHtml(getString(R.string.warning, getVpnLabel()),
                        this, null /* tagHandler */));
        mAlertParams.mTitle = getText(R.string.prompt);
        mAlertParams.mPositiveButtonText = getText(android.R.string.ok);
        mAlertParams.mPositiveButtonListener = this;
        mAlertParams.mNegativeButtonText = getText(android.R.string.cancel);
        mAlertParams.mView = view;
        setupAlert();

        Bridge.Window_setCloseOnTouchOutside(getWindow(), false);
        int SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS = 0x00080000;
        Bridge.Window_addPrivateFlags(getWindow(), SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS);
        Button button = mAlert.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setFilterTouchesWhenObscured(true);
    }

    private boolean prepareVpn() {
        var userId = Bridge.UserHandle_myUserId();
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                return mService.prepareVpn(mPackage, null, userId);
            }
            return Bridge.VpnManager_prepareVpn(mVm, mPackage, null, userId);
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    private CharSequence getVpnLabel() {
        try {
            return VpnConfig.getVpnLabel(this, mPackage);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Drawable getDrawable(String source) {
        // Should only reach this when fetching the VPN icon for the warning string.
        final Drawable icon = getDrawable(R.drawable.ic_vpn_dialog);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());

        final TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true)) {
            icon.setTint(getColor(tv.resourceId));
        } else {
            Log.w(TAG, "Unable to resolve theme color");
        }

        return icon;
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        var userId = Bridge.UserHandle_myUserId();
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                if (mService.prepareVpn(null, mPackage, userId)) {
                    mService.setVpnPackageAuthorization(mPackage, userId, mVpnType);
                    setResult(RESULT_OK);
                }
            } else {
                if (Bridge.VpnManager_prepareVpn(mVm, null, mPackage, userId)) {
                    Bridge.VpnManager_setVpnPackageAuthorization(mVm, mPackage, userId, mVpnType);
                    setResult(RESULT_OK);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onClick", e);
        }
    }
}
