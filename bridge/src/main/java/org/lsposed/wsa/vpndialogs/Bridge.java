package org.lsposed.wsa.vpndialogs;

import android.content.ContentResolver;
import android.net.VpnManager;
import android.os.UserHandle;
import android.view.Window;

import com.android.internal.net.VpnConfig;

public final class Bridge {
    public static int UserHandle_myUserId() {
        return UserHandle.myUserId();
    }

    public static void Window_setCloseOnTouchOutside(Window window, boolean close) {
        window.setCloseOnTouchOutside(close);
    }

    public static void Window_addPrivateFlags(Window window, int flags) {
        window.addPrivateFlags(flags);
    }

    public static boolean VpnManager_prepareVpn(VpnManager vpnManager,
                                                String oldPackage,
                                                String newPackage,
                                                int userId) {
        return vpnManager.prepareVpn(oldPackage, newPackage, userId);
    }

    public static void VpnManager_setVpnPackageAuthorization(VpnManager vpnManager,
                                                             String packageName,
                                                             int userId,
                                                             int vpnType) {
        vpnManager.setVpnPackageAuthorization(packageName, userId, vpnType);
    }

    public static VpnConfig VpnManager_getVpnConfig(VpnManager vpnManager, int userId) {
        return vpnManager.getVpnConfig(userId);
    }

    public static String Setting_getStringForUser(ContentResolver cr, String name, int userHandle) {
        return android.provider.Settings.getStringForUser(cr, name, userHandle);
    }
}
