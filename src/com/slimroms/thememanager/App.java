package com.slimroms.thememanager;

import android.app.Application;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.slimroms.themecore.Broadcast;
import com.slimroms.themecore.IThemeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();
    private static App mInstance;
    private final HashMap<ComponentName, IThemeService> mBackends = new HashMap<>();
    private final List<ServiceConnection> mConnections = new ArrayList<>();
    private final List<ComponentName> mBusyBackends = new ArrayList<>();

    public static App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        registerReceiver(mBusyReceiver, Broadcast.getBackendBusyFilter());
    }

    public void bindBackends() {
        final String backendAction = "org.slim.theming.BACKEND";
        final Intent filterIntent = new Intent(backendAction);
        final List<ResolveInfo> services = getPackageManager().queryIntentServices(filterIntent, 0);
        for (ResolveInfo ri : services) {
            if (ri.serviceInfo.exported) {
                Log.i(TAG, "Found backend: " + ri.serviceInfo.name);
                // perform the signature check
                final int signatureCheckResult = getPackageManager()
                        .checkSignatures(getPackageName(), ri.serviceInfo.packageName);
                if (signatureCheckResult == PackageManager.SIGNATURE_MATCH || isDebug()) {
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
                                    final Intent eventIntent = new Intent(Broadcast.ACTION_BACKEND_CONNECTED);
                                    eventIntent.putExtra(Broadcast.EXTRA_BACKEND_NAME, componentName);
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(eventIntent);
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
                            final Intent eventIntent = new Intent(Broadcast.ACTION_BACKEND_DISCONNECTED);
                            eventIntent.putExtra(Broadcast.EXTRA_BACKEND_NAME, componentName);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(eventIntent);
                        }
                    };

                    final Intent backendIntent = new Intent(backendAction);
                    backendIntent.setPackage(ri.serviceInfo.packageName);
                    bindService(backendIntent, backendConnection, BIND_AUTO_CREATE);
                } else {
                    // found security issue
                    Log.i(TAG, ri.serviceInfo.name + " encountered a signature mismatch: "
                            + signatureCheckResult + "! Skipping...");
                }
            }
        }
    }

    public void unbindBackends() {
        synchronized (mBackends) {
            mBackends.clear();
        }
        synchronized (mConnections) {
            while (!mConnections.isEmpty()) {
                unbindService(mConnections.get(0));
                mConnections.remove(0);
            }
        }
    }

    public IThemeService getBackend(ComponentName name) {
        return mBackends.get(name);
    }

    public Set<ComponentName> getBackendNames() {
        return mBackends.keySet();
    }

    public static boolean isDebug() {
        return true;
    }

    public boolean isBackendBusy(ComponentName backendName) {
        return backendName != null && mBusyBackends.contains(backendName);
    }

    public boolean isAnyBackendBusy() {
        return !mBusyBackends.isEmpty();
    }

    private final BroadcastReceiver mBusyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ComponentName backendName = intent.getParcelableExtra(Broadcast.EXTRA_BACKEND_NAME);
            if (backendName != null) {
                if (intent.getAction().equals(Broadcast.ACTION_BACKEND_BUSY) && !mBusyBackends.contains(backendName)) {
                    synchronized (mBusyBackends) {
                        mBusyBackends.add(backendName);
                    }
                }
                else if (intent.getAction().equals(Broadcast.ACTION_BACKEND_NOT_BUSY)) {
                    synchronized (mBusyBackends) {
                        mBusyBackends.remove(backendName);
                    }
                }
            }
        }
    };
}
