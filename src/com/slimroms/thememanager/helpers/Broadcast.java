package com.slimroms.thememanager.helpers;

import android.content.IntentFilter;

public class Broadcast {
    public static final String ACTION_BACKEND_CONNECTED = "ACTION_BACKEND_CONNECTED";
    public static final String ACTION_BACKEND_DISCONNECTED = "ACTION_BACKEND_DISCONNECTED";

    public static final String EXTRA_BACKEND_NAME = "EXTRA_BACKEND_NAME";
    public static final String EXTRA_THEME_PACKAGE = "EXTRA_THEME_PACKAGE";

    private static IntentFilter mBackendConnectFilter;
    public static IntentFilter getBackendConnectFilter() {
        if (mBackendConnectFilter == null) {
            mBackendConnectFilter = new IntentFilter();
            mBackendConnectFilter.addAction(Broadcast.ACTION_BACKEND_CONNECTED);
            mBackendConnectFilter.addAction(Broadcast.ACTION_BACKEND_DISCONNECTED);
        }
        return mBackendConnectFilter;
    }
}
