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
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import com.slimroms.themecore.Broadcast;
import com.slimroms.themecore.IThemeService;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.themecore.Theme;
import com.slimroms.thememanager.adapters.ThemeContentPagerAdapter;

import java.util.ArrayList;

public class ThemeContentActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private Snackbar mLoadingSnackbar;
    private FloatingActionButton mFab;
    private CoordinatorLayout mCoordinator;
    private TabLayout mTabLayout;

    private Theme mTheme;
    private OverlayThemeInfo mOverlayInfo;
    private String mThemePackageName;
    private ComponentName mBackendComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_content);

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

        mThemePackageName = getIntent().getStringExtra(Broadcast.EXTRA_THEME_PACKAGE);
        mBackendComponent = getIntent().getParcelableExtra(Broadcast.EXTRA_BACKEND_NAME);
        if (!TextUtils.isEmpty(mThemePackageName) && mBackendComponent != null) {
            try {
                if (App.getInstance().getBackend(mBackendComponent) != null) {
                    mTheme = App.getInstance().getBackend(mBackendComponent)
                            .getThemeByPackage(mThemePackageName);
                }
                setupTabLayout();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mFab.setVisibility(App.getInstance().isBackendBusy(mBackendComponent) ? View.GONE : View.VISIBLE);
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
            if (intent.getAction().equals(Broadcast.ACTION_BACKEND_CONNECTED)) {
                if (App.getInstance().getBackend(mBackendComponent) != null) {
                    try {
                        App.getInstance().getBackend(mBackendComponent).getThemePackages(new ArrayList<Theme>());
                        mTheme = App.getInstance().getBackend(mBackendComponent).getThemeByPackage(mThemePackageName);
                        setupTabLayout();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (App.getInstance().getBackend(mBackendComponent) == null) {
                    mTheme = null;
                    mLoadingSnackbar.show();
                }
            }
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

    private void setupTabLayout() {
        if (mTheme == null) return;
        setTitle(mTheme.name);
        new AsyncTask<Theme, Void, OverlayThemeInfo>() {
            @Override
            protected void onPreExecute() {
                mLoadingSnackbar.show();
            }

            @Override
            protected OverlayThemeInfo doInBackground(Theme... themes) {
                try {
                    final OverlayThemeInfo result = new OverlayThemeInfo();
                    if (App.getInstance().getBackend(themes[0].backendName) != null) {
                        App.getInstance().getBackend(themes[0].backendName).getThemeContent(themes[0], result);
                        return result;
                    }
                    return null;
                }
                catch (RemoteException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(OverlayThemeInfo overlayThemeInfo) {
                if (overlayThemeInfo != null) {
                    mOverlayInfo = overlayThemeInfo;
                    if (!ThemeContentActivity.this.isDestroyed()) {
                        final ThemeContentPagerAdapter adapter
                                = new ThemeContentPagerAdapter(getSupportFragmentManager(),
                                mOverlayInfo, mTheme, getBaseContext());
                        mViewPager.setAdapter(adapter);
                        mTabLayout.setVisibility(mOverlayInfo.groups.size() > 1 ? View.VISIBLE : View.GONE);
                    } else {
                        ThemeContentActivity.this.finish();
                        Intent intent = ThemeContentActivity.this.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                mLoadingSnackbar.dismiss();
            }
        }.execute(mTheme);
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

    private View.OnClickListener mFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOverlayInfo.getSelectedCount() > 0) {
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected void onPreExecute() {
                        mFab.setVisibility(View.GONE);
                    }

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        try {
                            return App.getInstance().getBackend(mTheme.backendName)
                                    .installOverlaysFromTheme(mTheme, mOverlayInfo);
                        }
                        catch (RemoteException ex) {
                            ex.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        if (aBoolean) {
                            try {
                                final IThemeService backend = App.getInstance().getBackend(mTheme.backendName);
                                if (backend != null && backend.isRebootRequired()) {
                                    final Snackbar snackbar = Snackbar.make(mCoordinator, R.string.reboot_required,
                                            Snackbar.LENGTH_INDEFINITE);
                                    snackbar.setAction(R.string.action_reboot, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            try {
                                                App.getInstance().getBackend(mTheme.backendName).reboot();
                                            }
                                            catch (RemoteException ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    });
                                    snackbar.show();
                                }
                            }
                            catch (RemoteException ex) {
                                ex.printStackTrace();
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
}
