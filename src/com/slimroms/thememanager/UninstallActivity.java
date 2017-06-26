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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.support.v4.util.SimpleArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.slimroms.themecore.Broadcast;
import com.slimroms.themecore.IThemeService;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.thememanager.adapters.UninstallPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class UninstallActivity extends AppCompatActivity {
    private static boolean sFrozen = false;
    private final HashMap<String, ComponentName> mBackendsToUninstallFrom = new HashMap<>();
    private CoordinatorLayout mCoordinator;
    private Snackbar mLoadingSnackbar;
    private ViewPager mViewPager;
    private FloatingActionButton mFab;
    private TabLayout mTabLayout;
    private ViewGroup mOngoingView;
    private TextView mOngoingMessageView;
    private final BroadcastReceiver mBusyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Broadcast.ACTION_BACKEND_BUSY)) {
                final String message = intent.getStringExtra(Broadcast.EXTRA_MESSAGE);
                mOngoingMessageView.setText(message);
            } else {
                mOngoingMessageView.setText(null);
            }
        }
    };
    private LottieAnimationView mOngoingAnimationView;
    private View mEmptyView;
    private ArrayMap<Pair<String, ComponentName>, OverlayThemeInfo> mThemes = new ArrayMap<>();
    private final BroadcastReceiver mConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupTabLayout();
        }
    };
    private View.OnClickListener mFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mThemes != null && getSelectedCount() > 0) {
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected void onPreExecute() {
                        sFrozen = true;
                        mFab.setVisibility(View.GONE);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        mOngoingView.setVisibility(View.VISIBLE);
                        mOngoingAnimationView.playAnimation();
                    }

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        boolean result = true;
                        try {
                            for (Pair<String, ComponentName> pair : mThemes.keySet()) {
                                final ComponentName backendName = pair.second;
                                if (backendName != null) {
                                    OverlayGroup overlaysToUninstall = new OverlayGroup();
                                    for (OverlayGroup group : mThemes.get(pair).groups.values()) {
                                        overlaysToUninstall.overlays.addAll(group.overlays);
                                    }
                                    final IThemeService backend = App.getInstance().getBackend(backendName);
                                    result &= backend.uninstallOverlays(overlaysToUninstall);
                                }
                            }
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                            return false;
                        }
                        return result;
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        sFrozen = false;
                        setupTabLayout();
                        if (aBoolean) {
                            handleReboot();
                            final Intent intent = new Intent(Broadcast.ACTION_REDRAW);
                            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                        }
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        mOngoingAnimationView.pauseAnimation();
                        mOngoingView.setVisibility(View.GONE);
                        mFab.setVisibility(View.VISIBLE);
                    }
                }.execute();
            } else {
                Snackbar.make(mCoordinator, R.string.no_overlays_selected, Snackbar.LENGTH_SHORT).show();
            }
        }
    };

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
        mOngoingView = (ViewGroup) findViewById(R.id.ongoing_view);
        mOngoingMessageView = (TextView) findViewById(R.id.ongoing_message);
        mOngoingAnimationView = (LottieAnimationView) findViewById(R.id.ongoing_image);

        mEmptyView = findViewById(R.id.empty_view);
        final TextView emptyViewTitle = (TextView) findViewById(R.id.empty_view_title);
        emptyViewTitle.setText(R.string.no_installed_overlays_title);
        final TextView emptyViewDescription =
                (TextView) findViewById(R.id.empty_view_description);
        emptyViewDescription.setText(R.string.no_installed_overlays_description);

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

    @Override
    public void onBackPressed() {
        if (!sFrozen) {
            super.onBackPressed();
        }
    }

    private int getSelectedCount() {
        int count = 0;
        for (OverlayThemeInfo info : mThemes.values()) {
            count += info.getSelectedCount();
        }
        return count;
    }

    private synchronized void setupTabLayout() {
        if (sFrozen) return;
        if (!App.getInstance().getBackendNames().isEmpty()) {
            new AsyncTask<Void, Void, ArrayMap<Pair<String, ComponentName>, OverlayThemeInfo>>() {
                @Override
                protected void onPreExecute() {
                    mLoadingSnackbar.show();
                }

                @Override
                protected ArrayMap<Pair<String, ComponentName>, OverlayThemeInfo> doInBackground(Void... v) {
                    synchronized (mBackendsToUninstallFrom) {
                        mBackendsToUninstallFrom.clear();
                    }
                    final ArrayMap<Pair<String, ComponentName>, OverlayThemeInfo> themes = new ArrayMap<>();

                    for (ComponentName backendName : App.getInstance().getBackendNames()) {
                        final OverlayThemeInfo themeInfo = new OverlayThemeInfo();
                        final IThemeService backend = App.getInstance().getBackend(backendName);
                        try {
                            backend.getInstalledOverlays(themeInfo);
                            if (!themeInfo.groups.isEmpty()) {
                                final String title = backend.getBackendTitle();
                                themes.put(new Pair<>(title, backendName), themeInfo);
                                synchronized (mBackendsToUninstallFrom) {
                                    mBackendsToUninstallFrom.put(title, backendName);
                                }
                            }
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }
                    }
                    return themes;
                }

                @Override
                protected void onPostExecute(ArrayMap<Pair<String, ComponentName>, OverlayThemeInfo> themes) {
                    //if (UninstallActivity.this.isDestroyed()) {
                        /*UninstallActivity.this.finish();
                        Intent intent = UninstallActivity.this.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);*/
                    //  recreate();
                    //} else {
                    if (themes != null && !themes.isEmpty()) {
                        mThemes.clear();
                        mThemes.putAll((SimpleArrayMap<Pair<String, ComponentName>, OverlayThemeInfo>) themes);
                        mEmptyView.setVisibility(View.GONE);
                    } else {
                        mEmptyView.setVisibility(View.VISIBLE);
                        synchronized (mBackendsToUninstallFrom) {
                            mBackendsToUninstallFrom.clear();
                        }
                    }

                    if (mViewPager.getAdapter() == null) {
                        final UninstallPagerAdapter adapter =
                                new UninstallPagerAdapter(getSupportFragmentManager(),
                                        mThemes, getBaseContext());
                        mViewPager.setAdapter(adapter);
                    } else {
                        UninstallPagerAdapter adapter = (UninstallPagerAdapter) mViewPager.getAdapter();
                        adapter.setThemes(mThemes);
                    }
                    mTabLayout.setVisibility(mThemes.size() > 1 ? View.VISIBLE : View.GONE);
                    mLoadingSnackbar.dismiss();
                    // }
                }
            }.execute();
        } else {
            mThemes.clear();
            final UninstallPagerAdapter adapter
                    = new UninstallPagerAdapter(getSupportFragmentManager(),
                    mThemes, getBaseContext());
            mViewPager.setAdapter(adapter);
            mTabLayout.setVisibility(mThemes.size() > 1 ? View.VISIBLE : View.GONE);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.reboot_required);
                builder.setPositiveButton(R.string.action_reboot,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ProgressDialog.show(UninstallActivity.this, getString(R.string.restarting),
                                        getString(R.string.please_wait), true, false);
                                Runnable run = new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            for (ComponentName cmpn : backends) {
                                                IThemeService backend = App.getInstance().getBackend(cmpn);
                                                backend.reboot();
                                            }
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                Executors.newSingleThreadExecutor().execute(run);
                            }
                        });
                builder.setNegativeButton(R.string.action_dismiss,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sFrozen = false;
                                setupTabLayout();
                            }
                        });
                builder.setCancelable(false);
                builder.show();
            } else {
                sFrozen = false;
                setupTabLayout();
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
