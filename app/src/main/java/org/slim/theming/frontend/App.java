package org.slim.theming.frontend;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();
    private static App mInstance;
    private ServiceConnection mBackendConnection;
    private Gson mGson = new GsonBuilder().create();
    private Random mRandom = new Random(new Date().getTime());
    private HashMap<ComponentName, Messenger> mBackends = new HashMap<>();

    public static App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        mBackendConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mBackends.put(componentName, new Messenger(iBinder));
                Log.i(TAG, componentName.getClassName() + " service connected");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBackends.remove(componentName);
                Log.i(TAG, componentName.getClassName() + " service disconnected");
            }
        };

        final String backendAction = "org.slim.theming.BACKEND";
        final Intent filterIntent = new Intent(backendAction);
        final List<ResolveInfo> services = getPackageManager().queryIntentServices(filterIntent, 0);
        for (ResolveInfo ri : services) {
            Log.i(TAG, "Found backend: " + ri.serviceInfo.name);
            final Intent backendIntent = new Intent(backendAction);
            backendIntent.setPackage(ri.serviceInfo.packageName);
            bindService(backendIntent, mBackendConnection, BIND_AUTO_CREATE);
        }
    }
}
