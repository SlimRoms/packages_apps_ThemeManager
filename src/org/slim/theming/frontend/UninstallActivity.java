package org.slim.theming.frontend;

import android.content.BroadcastReceiver;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.slimroms.themecore.IThemeService;
import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.themecore.Theme;

import org.slim.theming.frontend.adapters.UninstallAdapter;
import org.slim.theming.frontend.helpers.BroadcastHelper;
import org.slim.theming.frontend.helpers.MenuTintHelper;
import org.slim.theming.frontend.views.LineDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class UninstallActivity extends AppCompatActivity {
    private FloatingActionButton mFab;
    private CoordinatorLayout mCoordinator;
    private RecyclerView mRecycler;
    private UninstallAdapter mAdapter;
    private TextView mEmpty;

    private Snackbar mLoadingSnackbar;

    private List<Object> mItems = new ArrayList<>();
    private List<Pair<Theme, OverlayThemeInfo>> mThemes = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uninstall);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayHomeAsUpEnabled(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastHelper.ACTION_BACKEND_CONNECTED);
        filter.addAction(BroadcastHelper.ACTION_BACKEND_DISCONNECTED);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        mCoordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        mEmpty = (TextView) findViewById(R.id.empty_view);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Pair<Theme, OverlayThemeInfo> p : mThemes) {
                    OverlayGroup group = p.second.groups.get(OverlayGroup.OVERLAYS);
                    List<Overlay> overlays = group.overlays;
                    if (overlays == null || overlays.isEmpty()) continue;
                    boolean needsUninstall = false;
                    for (Overlay overlay : overlays) {
                        if (mAdapter.getSelected().contains(overlay)) {
                            if (overlay.checked) {
                                overlay.checked = false;
                                needsUninstall = true;
                            }
                        }
                    }
                    if (!needsUninstall) continue;
                    try {
                        App.getInstance().getBackend(App.getInstance().getCurrentBackend()).uninstallOverlays(p.first, p.second);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mRecycler.setVisibility(View.GONE);
                mEmpty.setVisibility(View.VISIBLE);
                loadThemes();
            }
        });

        mLoadingSnackbar = Snackbar.make(mCoordinator, R.string.loading, Snackbar.LENGTH_INDEFINITE);

        loadThemes();

        mRecycler = (RecyclerView) findViewById(R.id.list);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new LineDividerItemDecoration(this));
        mRecycler.setItemAnimator(new DefaultItemAnimator());
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastHelper.ACTION_BACKEND_CONNECTED)) {
                if (App.getInstance().getBackend(App.getInstance().getCurrentBackend()) != null) {
                    loadThemes();
                }
            }
        }
    };

    private void loadThemes() {
        if (App.getInstance().getBackend(App.getInstance().getCurrentBackend()) != null) {
            new AsyncTask<Void, Void, List<Object>>() {
                @Override
                protected void onPreExecute() {
                    mLoadingSnackbar.show();
                }
                @Override
                protected List<Object> doInBackground(Void... v) {
                    List<Object> list = new ArrayList<>();
                    IThemeService service = App.getInstance().getBackend(App.getInstance().getCurrentBackend());
                    List<Theme> themes = new ArrayList<>();
                    try {
                        service.getThemePackages(themes);
                        for (Theme theme : themes) {
                            OverlayThemeInfo info = new OverlayThemeInfo();
                            service.getThemeContent(theme, info);
                            List<Overlay> overlays = info.groups.get(OverlayGroup.OVERLAYS).overlays;
                            if (overlays == null || overlays.isEmpty()) continue;
                            list.add(theme);
                            mThemes.add(new Pair<>(theme, info));
                            for (Overlay overlay : overlays) {
                                if (overlay == null || !overlay.checked) continue;
                                list.add(overlay);
                            }
                            if (list.get(list.size() - 1).equals(theme)) {
                                list.remove(theme);
                            }
                        }
                        return list;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(List<Object> list) {
                    mItems.addAll(list);
                    mAdapter = new UninstallAdapter(UninstallActivity.this, list);
                    mRecycler.setAdapter(mAdapter);

                    Log.d("TEST", "size=" + list.size());
                    if (list.size() == 0) {
                        mRecycler.setVisibility(View.GONE);
                        mEmpty.setVisibility(View.VISIBLE);
                    }
                    mLoadingSnackbar.dismiss();
                }
            }.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, R.id.action_select_all, 0, getString(R.string.action_select_all))
                .setIcon(R.drawable.select_all)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuTintHelper.tintMenu(menu, ContextCompat.getColor(this, android.R.color.white));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_all:
                for (Object object : mItems) {
                    if (object instanceof Overlay) {
                        if (!mAdapter.getSelected().contains(object)) {
                            mAdapter.getSelected().add((Overlay) object);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
