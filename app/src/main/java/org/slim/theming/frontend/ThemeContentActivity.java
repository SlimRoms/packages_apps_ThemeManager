package org.slim.theming.frontend;

import android.content.ComponentName;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.themecore.Theme;
import org.slim.theming.frontend.adapters.ThemeContentPagerAdapter;
import org.slim.theming.frontend.helpers.BroadcastHelper;

public class ThemeContentActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private Snackbar mLoadingSnackbar;

    private Theme mTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_content);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayHomeAsUpEnabled(true);

        final String themePackage = getIntent().getStringExtra(BroadcastHelper.EXTRA_THEME_PACKAGE);
        final ComponentName backendName = getIntent().getParcelableExtra(BroadcastHelper.EXTRA_BACKEND_NAME);
        if (!TextUtils.isEmpty(themePackage) && backendName != null) {
            try {
                mTheme = App.getInstance().getBackend(backendName).getThemeByPackage(themePackage);
                bar.setTitle(mTheme.name);
            }
            catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        CoordinatorLayout coordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mLoadingSnackbar = Snackbar.make(coordinator, R.string.loading, Snackbar.LENGTH_INDEFINITE);

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

        setupTabLayout();
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
                    final ThemeContentPagerAdapter adapter
                            = new ThemeContentPagerAdapter(getSupportFragmentManager(), overlayThemeInfo);
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
}
