package android.app;

import android.app.IBinderSession;
import android.content.ComponentName;

/** @hide */
oneway interface IServiceConnection {
    void connected(
        in ComponentName name,
        IBinder service,
        in @nullable IBinderSession session,
        boolean dead
    );
}
