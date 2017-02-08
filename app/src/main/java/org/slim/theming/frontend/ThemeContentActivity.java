package org.slim.theming.frontend;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.MenuItem;

import android.view.View;
import com.slimroms.themecore.IThemeService;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.themecore.Theme;
import org.slim.theming.frontend.adapters.ThemeContentPagerAdapter;
import org.slim.theming.frontend.helpers.BroadcastHelper;

public class ThemeContentActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private Snackbar mLoadingSnackbar;
    private FloatingActionButton mFab;
    private CoordinatorLayout mCoordinator;

    private Theme mTheme;
    private OverlayThemeInfo mOverlayInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_content);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayHomeAsUpEnabled(true);

        Log.d("TEST", "ThemeContentActivity.onCreate");
        Log.d("TEST", "savedInstanceState=" + (savedInstanceState != null));

        String themePackage = null;
        ComponentName backendName = null;
        if (savedInstanceState != null) {
            themePackage = savedInstanceState.getString(BroadcastHelper.EXTRA_THEME_PACKAGE);
            backendName = savedInstanceState.getParcelable(BroadcastHelper.EXTRA_BACKEND_NAME);
        }
        if (TextUtils.isEmpty(themePackage)) {
            themePackage = getIntent().getStringExtra(BroadcastHelper.EXTRA_THEME_PACKAGE);
        }
        if (backendName == null) {
            backendName = getIntent().getParcelableExtra(BroadcastHelper.EXTRA_BACKEND_NAME);
        }
        final String themePackageName = themePackage;
        final ComponentName backendComponent = backendName;
        Log.d("TEST", "themePackage = " + themePackage);

        mCoordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mLoadingSnackbar = Snackbar.make(mCoordinator, R.string.loading, Snackbar.LENGTH_INDEFINITE);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(mFabListener);

        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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

        if (!TextUtils.isEmpty(themePackageName) && backendComponent != null) {
            try {
                if (App.getInstance().getBackend(backendName) != null) {
                    mTheme = App.getInstance().getBackend(backendComponent).getThemeByPackage(themePackageName);
                    bar.setTitle(mTheme.name);
                    setupTabLayout();
                } else {
                    BroadcastReceiver receiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            Log.d("TEST", "onReceive=" + intent.getAction());
                            Log.d("TEST", "theme=" + themePackageName);
                            Log.d("TEST", "backend-" + backendComponent.flattenToString());
                            try {
                                if (App.getInstance().getBackend(backendComponent) != null) {
                                    mTheme = App.getInstance().getBackend(backendComponent).getThemeByPackage(themePackageName);
                                    if (mTheme != null) {
                                        bar.setTitle(mTheme.name);
                                        setupTabLayout();
                                    }
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    LocalBroadcastManager.getInstance(this)
                            .registerReceiver(receiver,
                                    new IntentFilter(BroadcastHelper.ACTION_BACKEND_CONNECTED));
                }
            }
            catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if (mTheme != null) {
            out.putString(BroadcastHelper.EXTRA_THEME_PACKAGE, mTheme.packageName);
            out.putParcelable(BroadcastHelper.EXTRA_BACKEND_NAME, mTheme.backendName);
        }
    }

    private void setupTabLayout() {
        mLoadingSnackbar.show();
        new AsyncTask<Theme, Void, OverlayThemeInfo>() {
            @Override
            protected OverlayThemeInfo doInBackground(Theme... themes) {
                try {
                    final OverlayThemeInfo result = new OverlayThemeInfo();
                    App.getInstance().getBackend(themes[0].backendName).getThemeContent(themes[0], result);
                    return result;
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
                    final ThemeContentPagerAdapter adapter
                            = new ThemeContentPagerAdapter(getSupportFragmentManager(), mOverlayInfo, getBaseContext());
                    mViewPager.setAdapter(adapter);
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
                        mFab.setEnabled(false);
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

                        mFab.setEnabled(true);
                    }
                }.execute();
            } else {
                Snackbar.make(mCoordinator, R.string.no_overlays_selected, Snackbar.LENGTH_SHORT).show();
            }
        }
    };
}
