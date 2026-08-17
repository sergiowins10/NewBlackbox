package top.niunaijun.blackbox.fake.delegate;

import android.app.IBinderSession;
import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

import top.niunaijun.blackbox.utils.compat.BuildCompat;
import black.android.app.BRIServiceConnectionO;


public class ServiceConnectionDelegate extends IServiceConnection.Stub {

    private static final Map<IBinder, ServiceConnectionDelegate>
            sServiceConnectDelegate = new HashMap<>();

    private final IServiceConnection mConn;
    private final ComponentName mComponentName;

    private ServiceConnectionDelegate(
            IServiceConnection mConn,
            ComponentName targetComponent
    ) {
        this.mConn = mConn;
        this.mComponentName = targetComponent;
    }

    public static ServiceConnectionDelegate getDelegate(IBinder iBinder) {
        return sServiceConnectDelegate.get(iBinder);
    }

    public static IServiceConnection createProxy(
            IServiceConnection base,
            Intent intent
    ) {
        final IBinder iBinder = base.asBinder();

        ServiceConnectionDelegate delegate =
                sServiceConnectDelegate.get(iBinder);

        if (delegate == null) {
            try {
                iBinder.linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        sServiceConnectDelegate.remove(iBinder);
                        iBinder.unlinkToDeath(this, 0);
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            delegate = new ServiceConnectionDelegate(
                    base,
                    intent != null ? intent.getComponent() : null
            );

            sServiceConnectDelegate.put(iBinder, delegate);
        }

        return delegate;
    }

    @Override
    public void connected(
            ComponentName name,
            IBinder service,
            IBinderSession session,
            boolean dead
    ) throws RemoteException {

        ComponentName componentName =
                mComponentName != null ? mComponentName : name;

        mConn.connected(
                componentName,
                service,
                session,
                dead
        );
    }
}
