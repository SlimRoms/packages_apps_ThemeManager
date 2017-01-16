package org.slim.theming.frontend;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.slimroms.themecore.IThemeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();
    private static App mInstance;
    private final HashMap<ComponentName, IThemeService> mBackends = new HashMap<>();
    private final List<ServiceConnection> mConnections = new ArrayList<>();

    public static App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        final String backendAction = "org.slim.theming.BACKEND";
        final Intent filterIntent = new Intent(backendAction);
        final List<ResolveInfo> services = getPackageManager().queryIntentServices(filterIntent, 0);
        for (ResolveInfo ri : services) {
            if (ri.serviceInfo.exported) {
                Log.i(TAG, "Found backend: " + ri.serviceInfo.name);
                final ServiceConnection backendConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        final IThemeService backend = IThemeService.Stub.asInterface(iBinder);
                        try {
                            // if backend is unusable in current ROM setup, drop the connection
                            if (backend.isAvailable()) {
                                synchronized (mBackends) {
                                    mBackends.put(componentName, backend);
                                }
                                synchronized (mConnections) {
                                    mConnections.add(this);
                                }
                                Log.i(TAG, componentName.getClassName() + " service connected");
                            }
                            else {
                                unbindService(this);
                            }
                        }
                        catch (RemoteException ex) {
                            Log.e(TAG, componentName.getClassName() + " remote exception");
                            ex.printStackTrace();
                            unbindService(this);
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {
                        synchronized (mBackends) {
                            if (mBackends.containsKey(componentName))
                                mBackends.remove(componentName);
                        }
                        synchronized (mConnections) {
                            if (mConnections.contains(this))
                                mConnections.remove(this);
                        }
                        Log.i(TAG, componentName.getClassName() + " service disconnected");
                    }
                };

                final Intent backendIntent = new Intent(backendAction);
                backendIntent.setPackage(ri.serviceInfo.packageName);
                bindService(backendIntent, backendConnection, BIND_AUTO_CREATE);
            }
        }
    }

    public void unbindBackends() {
        while (!mConnections.isEmpty()) {
            unbindService(mConnections.get(0));
        }
    }
}
