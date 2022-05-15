package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import com.android.internal.net.VpnConfig;

public interface IConnectivityManager extends IInterface {
    boolean prepareVpn(String oldPackage, String newPackage, int userId) throws RemoteException;

    void setVpnPackageAuthorization(String packageName, int userId, int vpnType) throws RemoteException;

    VpnConfig getVpnConfig(int userId) throws RemoteException;

    abstract class Stub extends Binder implements IConnectivityManager {
        public static IConnectivityManager asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }
    }
}
