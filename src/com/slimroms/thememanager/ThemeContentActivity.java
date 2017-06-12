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
import android.support.annotation.StringRes;
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
import android.view.ViewGroup;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.slimroms.themecore.*;
import com.slimroms.thememanager.adapters.ThemeContentPagerAdapter;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ThemeContentActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private Snackbar mLoadingSnackbar;
    private FloatingActionButton mFab;
    private CoordinatorLayout mCoordinator;
    private TabLayout mTabLayout;
    private ViewGroup mOngoingView;
    private TextView mOngoingMessageView;
    private LottieAnimationView mOngoingAnimationView;

    private Theme mTheme;
    private OverlayThemeInfo mOverlayInfo;
    private String mThemePackageName;
    private ComponentName mBackendComponent;
    private boolean mIsBusy = false;

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
        mOngoingView = (ViewGroup) findViewById(R.id.ongoing_view);
        mOngoingMessageView = (TextView) findViewById(R.id.ongoing_message);
        mOngoingAnimationView = (LottieAnimationView) findViewById(R.id.ongoing_image);

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

        try {
            final IThemeService backend = App.getInstance().getBackend(mBackendComponent);
            if (backend != null && backend.isRebootRequired()
                    && (getIntent().getIntExtra("reboot", 0) == 1)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.reboot_required);
                builder.setPositiveButton(R.string.action_reboot,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ProgressDialog.show(ThemeContentActivity.this,
                                getString(R.string.restarting),
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
                                    App.getInstance().getBackend(mBackendComponent).reboot();
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
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        } catch (RemoteException exc) {
        }
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
        if (!mIsBusy) {
            super.onBackPressed();
        }
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
                final String message = intent.getStringExtra(Broadcast.EXTRA_MESSAGE);
                mOngoingMessageView.setText(message);
            } else {
                mOngoingMessageView.setText(null);
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
                        try {
                            final IThemeService backend =
                                 App.getInstance().getBackend(mTheme.backendName);
                            if (backend != null && backend.isRebootRequired()) {
                                intent.putExtra("reboot", 1);
                            }
                        } catch (Exception e) {
                        }
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
        @StringRes int messageId;

        @Override
        public void onClick(View view) {
            messageId = -1;

            if (mOverlayInfo.getSelectedCount() == 0) {
                messageId = R.string.no_overlays_selected;
            } else {
                // see if there is any uncompilable style selected
                for (OverlayGroup group : mOverlayInfo.groups.values()) {
                    if (!group.styles.isEmpty() && group.selectedStyle.isEmpty()) {
                        messageId = R.string.no_style_selected;
                    }
                }
            }

            if (messageId == -1) {
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected void onPreExecute() {
                        mIsBusy = true;
                        mFab.setVisibility(View.GONE);
                        mOngoingView.setVisibility(View.VISIBLE);
                        mOngoingAnimationView.playAnimation();
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
                                final IThemeService backend =
                                        App.getInstance().getBackend(mTheme.backendName);
                                if (backend != null && backend.isRebootRequired()) {
                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(ThemeContentActivity.this);
                                    builder.setMessage(R.string.reboot_required);
                                    builder.setPositiveButton(R.string.action_reboot,
                                            new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            ProgressDialog.show(ThemeContentActivity.this,
                                                    getString(R.string.restarting),
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
                                                        backend.reboot();
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
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.setCancelable(false);
                                    builder.show();
                                }
                            }
                            catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                        mOverlayInfo.clearSelection();
                        final Intent intent = new Intent(Broadcast.ACTION_REDRAW);
                        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);

                        mOngoingAnimationView.pauseAnimation();
                        mOngoingView.setVisibility(View.GONE);
                        mFab.setVisibility(View.VISIBLE);
                        mIsBusy = false;
                    }
                }.execute();
            } else {
                Snackbar.make(mCoordinator, messageId, Snackbar.LENGTH_SHORT).show();
            }
        }
    };
}
