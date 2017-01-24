package org.slim.theming.frontend;

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
import android.view.MenuItem;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.themecore.Theme;
import org.slim.theming.frontend.adapters.ThemeContentPagerAdapter;
import org.slim.theming.frontend.helpers.BroadcastHelper;

public class ThemeContentActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private CoordinatorLayout mCoordinator;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private Snackbar mLoadingSnackbar;

    private Theme mTheme;
    private OverlayThemeInfo mOverlayInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_content);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayHomeAsUpEnabled(true);
        mTheme = getIntent().getParcelableExtra(BroadcastHelper.EXTRA_THEME);
        bar.setTitle(mTheme.name);

        mCoordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mLoadingSnackbar = Snackbar.make(mCoordinator, R.string.loading, Snackbar.LENGTH_INDEFINITE);

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
