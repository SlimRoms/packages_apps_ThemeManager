/*
 * Copyright (C) 2017 SlimRoms Project
 * Copyright (C) 2017 Victor Lapin
 * Copyright (C) 2017 Griffin Millender
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.slimroms.thememanager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import com.slimroms.themecore.Broadcast;
import com.slimroms.themecore.IThemeService;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.thememanager.adapters.ThemeContentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class UninstallActivity extends AppCompatActivity {
    private CoordinatorLayout mCoordinator;
    private Snackbar mLoadingSnackbar;
    private ViewPager mViewPager;
    private FloatingActionButton mFab;
    private TabLayout mTabLayout;

    private OverlayThemeInfo mOverlayInfo;
    private final HashMap<String, ComponentName> mBackendsToUninstallFrom = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uninstall);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayHomeAsUpEnabled(true);

        mCoordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mLoadingSnackbar = Snackbar.make(mCoordinator, R.string.loading, Snackbar.LENGTH_INDEFINITE);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(mFabListener);
        mFab.setVisibility(App.getInstance().isAnyBackendBusy() ? View.GONE : View.VISIBLE);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        setupTabLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mConnectReceiver,
                Broadcast.getBackendConnectFilter());
        registerReceiver(mBusyReceiver, Broadcast.getBackendBusyFilter());
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectReceiver);
        unregisterReceiver(mBusyReceiver);
        super.onStop();
    }

    private final BroadcastReceiver mConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupTabLayout();
        }
    };

    private final BroadcastReceiver mBusyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Broadcast.ACTION_BACKEND_BUSY)) {
                mFab.setVisibility(View.GONE);
            } else {
                mFab.setVisibility(View.VISIBLE);
            }
        }
    };

    private View.OnClickListener mFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOverlayInfo != null && mOverlayInfo.getSelectedCount() > 0) {
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected void onPreExecute() {
                        mFab.setVisibility(View.GONE);
                    }

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        boolean result = false;
                        try {
                            for (String key : mOverlayInfo.groups.keySet()) {
                                final ComponentName backendName = mBackendsToUninstallFrom.get(key);
                                if (backendName != null) {
                                    final IThemeService backend = App.getInstance().getBackend(backendName);
                                    result |=  backend.uninstallOverlays(mOverlayInfo.groups.get(key));
                                }
                            }
                        }
                        catch (RemoteException ex) {
                            ex.printStackTrace();
                            return false;
                        }
                        return result;
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        if (aBoolean) {
                            if (!handleReboot()) {
                                final Intent intent = new Intent(Broadcast.ACTION_REDRAW);
                                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                            }
                        }
                        mFab.setVisibility(View.VISIBLE);
                    }
                }.execute();
            } else {
                Snackbar.make(mCoordinator, R.string.no_overlays_selected, Snackbar.LENGTH_SHORT).show();
            }
        }
    };

    private void setupTabLayout() {
        if (!App.getInstance().getBackendNames().isEmpty()) {
            new AsyncTask<Void, Void, OverlayThemeInfo>() {
                @Override
                protected void onPreExecute() {
                    mLoadingSnackbar.show();
                }

                @Override
                protected OverlayThemeInfo doInBackground(Void... v) {
                    synchronized (mBackendsToUninstallFrom) {
                        mBackendsToUninstallFrom.clear();
                    }
                    final OverlayThemeInfo result = new OverlayThemeInfo();

                    for (ComponentName backendName : App.getInstance().getBackendNames()) {
                        final OverlayGroup group = new OverlayGroup();
                        final IThemeService backend = App.getInstance().getBackend(backendName);
                        try {
                            backend.getInstalledOverlays(group);
                            if (!group.overlays.isEmpty()) {
                                final String title = backend.getBackendTitle();
                                result.groups.put(title, group);
                                synchronized (mBackendsToUninstallFrom) {
                                    mBackendsToUninstallFrom.put(title, backendName);
                                }
                            }
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }
                    }
                    return result;
                }

                @Override
                protected void onPostExecute(OverlayThemeInfo info) {
                    if (UninstallActivity.this.isDestroyed()) {
                        UninstallActivity.this.finish();
                        Intent intent = UninstallActivity.this.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("reboot", 1);
                        startActivity(intent);
                    } else {
                        if (info != null && !info.groups.isEmpty()) {
                            mOverlayInfo = info;
                        } else {
                            if (mOverlayInfo == null) {
                                mOverlayInfo = new OverlayThemeInfo();
                            } else {
                                mOverlayInfo.groups.clear();
                            }
                            mOverlayInfo.groups.put(OverlayGroup.OVERLAYS, new OverlayGroup());
                            synchronized (mBackendsToUninstallFrom) {
                                mBackendsToUninstallFrom.clear();
                            }
                        }

                        final ThemeContentPagerAdapter adapter =
                                new ThemeContentPagerAdapter(getSupportFragmentManager(),
                                        mOverlayInfo, null, getBaseContext());
                        mViewPager.setAdapter(adapter);
                        mTabLayout.setVisibility(mOverlayInfo.groups.size() > 1 ? View.VISIBLE : View.GONE);
                        mLoadingSnackbar.dismiss();
                        if (getIntent().getIntExtra("reboot", 0) == 1) {
                            handleReboot();
                        }
                    }
                }
            }.execute();
        } else {
            if (mOverlayInfo == null) {
                mOverlayInfo = new OverlayThemeInfo();
            } else {
                mOverlayInfo.groups.clear();
            }
            mOverlayInfo.groups.put(OverlayGroup.OVERLAYS, new OverlayGroup());
            final ThemeContentPagerAdapter adapter
                    = new ThemeContentPagerAdapter(getSupportFragmentManager(),
                    mOverlayInfo, null, getBaseContext());
            mViewPager.setAdapter(adapter);
            mTabLayout.setVisibility(mOverlayInfo.groups.size() > 1 ? View.VISIBLE : View.GONE);
        }
    }

    private boolean handleReboot() {
        boolean reboot = false;
        try {
            final ArrayList<ComponentName> backends = new ArrayList<>();
            for (ComponentName cmp : mBackendsToUninstallFrom.values()) {
                final IThemeService backend = App.getInstance().getBackend(cmp);
                if (backend != null) {
                    reboot |= backend.isRebootRequired();
                    backends.add(cmp);
                }
            }
            if (reboot) {
                final Snackbar snackbar = Snackbar.make(mCoordinator, R.string.reboot_required,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.action_reboot, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            for (ComponentName cmpn : backends) {
                                IThemeService backend = App.getInstance().getBackend(cmpn);
                                backend.reboot();
                            }
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                snackbar.show();
            }
        } catch (RemoteException exc) {
            exc.printStackTrace();
        }
        return reboot;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
