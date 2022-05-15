package android.net;

import com.android.internal.net.VpnConfig;

public class VpnManager {
    public boolean prepareVpn(String oldPackage, String newPackage, int userId) {
        throw new RuntimeException("Stub!");
    }

    public void setVpnPackageAuthorization(String packageName, int userId, int vpnType) {
        throw new RuntimeException("Stub!");
    }

    public VpnConfig getVpnConfig(int userId) {
        throw new RuntimeException("Stub!");
    }
}
