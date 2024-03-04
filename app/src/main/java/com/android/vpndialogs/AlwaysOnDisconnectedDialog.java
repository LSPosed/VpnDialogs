package com.android.vpndialogs;

import static android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
import static com.android.vpndialogs.Constant.ALWAYS_ON_VPN_APP;
import static com.android.vpndialogs.Constant.SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.net.VpnConfig;

import org.lsposed.wsa.vpndialogs.Bridge;

public class AlwaysOnDisconnectedDialog extends AlertActivity
        implements DialogInterface.OnClickListener {

    private static final String TAG = "VpnDisconnected";

    private String mVpnPackage;

    @Deprecated
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVpnPackage = Settings.Secure.getString(getContentResolver(), ALWAYS_ON_VPN_APP);
        if (mVpnPackage == null) {
            finish();
            return;
        }

        View view = View.inflate(this, R.layout.always_on_disconnected, null);
        TextView messageView = view.findViewById(R.id.message);
        messageView.setText(getMessage(getIntent().getBooleanExtra("lockdown", false)));
        messageView.setMovementMethod(LinkMovementMethod.getInstance());

        mAlertParams.mTitle = getString(R.string.always_on_disconnected_title);
        mAlertParams.mPositiveButtonText = getString(R.string.open_app);
        mAlertParams.mPositiveButtonListener = this;
        mAlertParams.mNegativeButtonText = getString(R.string.dismiss);
        mAlertParams.mNegativeButtonListener = this;
        mAlertParams.mCancelable = false;
        mAlertParams.mView = view;
        setupAlert();

        Bridge.Window_setCloseOnTouchOutside(getWindow(), false);
        getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        getWindow().addFlags(FLAG_ALT_FOCUSABLE_IM);
        Bridge.Window_addPrivateFlags(getWindow(), SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                PackageManager pm = getPackageManager();
                final Intent intent = pm.getLaunchIntentForPackage(mVpnPackage);
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                finish();
                break;
            case BUTTON_NEGATIVE:
                finish();
                break;
            default:
                break;
        }
    }

    private CharSequence getVpnLabel() {
        try {
            return VpnConfig.getVpnLabel(this, mVpnPackage);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Can't getVpnLabel() for " + mVpnPackage, e);
            return mVpnPackage;
        }
    }

    private CharSequence getMessage(boolean isLockdown) {
        final SpannableStringBuilder message = new SpannableStringBuilder();
        final int baseMessageResId = isLockdown
                ? R.string.always_on_disconnected_message_lockdown
                : R.string.always_on_disconnected_message;
        message.append(getString(baseMessageResId, getVpnLabel()));
        message.append(getString(R.string.always_on_disconnected_message_separator));
        message.append(getString(R.string.always_on_disconnected_message_settings_link),
                new VpnSpan(), 0 /*flags*/);
        return message;
    }

    private class VpnSpan extends ClickableSpan {
        @Override
        public void onClick(View unused) {
            final Intent intent = new Intent(Settings.ACTION_VPN_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
