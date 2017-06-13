/*
 * Copyright (C) 2016-2017 Projekt Substratum
 *
 * Modified/reimplemented for use by SlimRoms :
 *
 * Copyright (C) 2017 SlimRoms Project
 * Copyright (C) 2017 Victor Lapin
 * Copyright (C) 2017 Griffin Millender
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.slimroms.thememanager;

import android.app.Application;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.system.Os;
import android.util.Log;
import com.slimroms.themecore.Broadcast;
import com.slimroms.themecore.IThemeService;
import com.slimroms.themecore.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
        getCacheDir();
    }

    public void bindBackends() {
        final Intent filterIntent = new Intent(Broadcast.ACTION_BACKEND_QUERY);
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

                final Intent backendIntent = new Intent(Broadcast.ACTION_BACKEND_QUERY);
                backendIntent.setPackage(ri.serviceInfo.packageName);
                try {
                    bindService(backendIntent, backendConnection, BIND_AUTO_CREATE);
                }
                catch (SecurityException ex) {
                    Log.i(TAG, ri.serviceInfo.name + " encountered a security exception! Skipping...", ex);
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

    public int checkSignature(String packageName) {
        return getPackageManager().checkSignatures(packageName, "android");
    }

    @Override
    public File getCacheDir() {
        boolean error = false;
        final File appCache = new File("/data/system/theme/cache/", this.getPackageName());

        if (!appCache.exists()) {
            if (appCache.mkdir()) {
                try {
                    Shell.chmod(appCache.getAbsolutePath(), 700);
                }
                catch (Exception ex1) {
                    ex1.printStackTrace();
                    error = true;
                }
            } else {
                error = true;
            }
        }

        if (!error) {
            Log.i(App.TAG, "Using cache dir: " + appCache.getAbsolutePath());
            return appCache;
        } else {
            final File fallback = super.getCacheDir();
            Log.i(App.TAG, "Using fallback cache dir: " + fallback.getAbsolutePath());
            return fallback;
        }
    }
}
