package com.android.internal.net;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;


public class VpnConfig {
    public static final String LEGACY_VPN = "[Legacy VPN]";
    public String user;
    public String interfaze;
    public String session;
    public PendingIntent configureIntent;
    public long startTime = -1;
    public boolean legacy;

    public static CharSequence getVpnLabel(Context context, String packageName)
            throws PackageManager.NameNotFoundException {
        throw new RuntimeException("Stub!");
    }
}
