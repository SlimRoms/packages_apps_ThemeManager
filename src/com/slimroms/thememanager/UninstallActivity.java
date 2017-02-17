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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.slimroms.themecore.IThemeService;
import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayGroup;

import com.slimroms.thememanager.adapters.OverlayGroupAdapter;
import com.slimroms.thememanager.helpers.BroadcastHelper;
import com.slimroms.thememanager.helpers.MenuTintHelper;
import com.slimroms.thememanager.views.LineDividerItemDecoration;

public class UninstallActivity extends AppCompatActivity {
    private CoordinatorLayout mCoordinator;
    private RecyclerView mRecycler;
    private OverlayGroupAdapter mAdapter;
    private TextView mEmptyView;
    private Snackbar mLoadingSnackbar;

    private OverlayGroup mOverlayGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uninstall);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayHomeAsUpEnabled(true);

        mCoordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for (Pair<Theme, OverlayThemeInfo> p : mThemes) {
//                    OverlayGroup group = p.second.groups.get(OverlayGroup.OVERLAYS);
//                    List<Overlay> overlays = group.overlays;
//                    if (overlays == null || overlays.isEmpty()) continue;
//                    boolean needsUninstall = false;
//                    for (Overlay overlay : overlays) {
//                        if (mAdapter.getSelected().contains(overlay)) {
//                            if (overlay.checked) {
//                                overlay.checked = false;
//                                needsUninstall = true;
//                            }
//                        }
//                    }
//                    if (!needsUninstall) continue;
//                    try {
//                        App.getInstance().getBackend(App.getInstance().getCurrentBackend()).uninstallOverlays(p.first, p.second);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//                mRecycler.setVisibility(View.GONE);
//                mEmptyView.setVisibility(View.VISIBLE);
//                loadThemes();
//            }
//        });

        mLoadingSnackbar = Snackbar.make(mCoordinator, R.string.loading, Snackbar.LENGTH_INDEFINITE);
        mRecycler = (RecyclerView) findViewById(R.id.list);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new LineDividerItemDecoration(this));
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        loadThemes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                BroadcastHelper.getBackendConnectFilter());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onPause();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadThemes();
        }
    };

    private void loadThemes() {
        if (!App.getInstance().getBackendNames().isEmpty()) {
            new AsyncTask<Void, Void, OverlayGroup>() {
                @Override
                protected void onPreExecute() {
                    mLoadingSnackbar.show();
                }

                @Override
                protected OverlayGroup doInBackground(Void... v) {
                    final OverlayGroup group = new OverlayGroup();
                    for (ComponentName backendName : App.getInstance().getBackendNames()) {
                        final IThemeService backend = App.getInstance().getBackend(backendName);
                        try {
                            backend.getInstalledOverlays(group);
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }
                    }
                    return group;
                }

                @Override
                protected void onPostExecute(OverlayGroup group) {
                    mOverlayGroup = group;
                    mAdapter = new OverlayGroupAdapter(UninstallActivity.this, mOverlayGroup);
                    mRecycler.setAdapter(mAdapter);
                    mEmptyView.setVisibility((mOverlayGroup.overlays.isEmpty()) ? View.VISIBLE : View.GONE);
                    mLoadingSnackbar.dismiss();
                }
            }.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overlay_group, menu);
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
                Boolean newValue = null;
                for (Overlay overlay : mOverlayGroup.overlays) {
                    if (newValue == null)
                        newValue = !overlay.checked;
                    overlay.checked = newValue;
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
