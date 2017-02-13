package org.slim.theming.frontend;

import android.content.*;
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

import java.util.ArrayList;

public class ThemeContentActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private Snackbar mLoadingSnackbar;
    private FloatingActionButton mFab;
    private CoordinatorLayout mCoordinator;

    private Theme mTheme;
    private OverlayThemeInfo mOverlayInfo;
    private String mThemePackageName;
    private ComponentName mBackendComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_content);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastHelper.ACTION_BACKEND_CONNECTED);
        filter.addAction(BroadcastHelper.ACTION_BACKEND_DISCONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayHomeAsUpEnabled(true);

        if (App.isDebug()) {
            Log.d("TEST", "ThemeContentActivity.onCreate");
            Log.d("TEST", "savedInstanceState=" + (savedInstanceState != null));
        }

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

        mThemePackageName = getIntent().getStringExtra(BroadcastHelper.EXTRA_THEME_PACKAGE);
        mBackendComponent = getIntent().getParcelableExtra(BroadcastHelper.EXTRA_BACKEND_NAME);
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
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastHelper.ACTION_BACKEND_CONNECTED)) {
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
                mTheme = null;
                mLoadingSnackbar.show();
            }
        }
    };

    private void setupTabLayout() {
        mLoadingSnackbar.show();
        if (mTheme == null) return;
        new AsyncTask<Theme, Void, OverlayThemeInfo>() {
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
                    final ThemeContentPagerAdapter adapter
                            = new ThemeContentPagerAdapter(getSupportFragmentManager(), mOverlayInfo, mTheme, getBaseContext());
                    if (!ThemeContentActivity.this.isDestroyed()) {
                        mViewPager.setAdapter(adapter);
                    } else {
                        if (App.isDebug()) {
                            Log.d("TEST", "isDestroyed");
                        }
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
