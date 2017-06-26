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
package com.slimroms.thememanager.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slimroms.themecore.Broadcast;
import com.slimroms.themecore.IThemeService;
import com.slimroms.themecore.Theme;
import com.slimroms.thememanager.App;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.ThemeContentActivity;
import com.slimroms.thememanager.adapters.ThemesPackagesAdapter;
import com.slimroms.thememanager.views.LineDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ThemesPackagesFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Theme>>,
        SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = ThemesPackagesFragment.class.getSimpleName();
    private static final int LOADER_ID = 0;
    private ViewGroup mEmptyView;
    private ThemesPackagesAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler mHandler;
    private Theme mPendingTheme;
    private Runnable mEmptyViewRunnable = new Runnable() {
        @Override
        public void run() {
            mEmptyView.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    };

    public static ThemesPackagesFragment newInstance() {
        return new ThemesPackagesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_refresh, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.nav_themes);
        mHandler = new Handler();

        mEmptyView = (ViewGroup) view.findViewById(R.id.empty_view);
        final TextView emptyViewTitle = (TextView) view.findViewById(R.id.empty_view_title);
        emptyViewTitle.setText(R.string.no_themes_title);
        final TextView emptyViewDescription = (TextView) view.findViewById(R.id.empty_view_description);
        emptyViewDescription.setText(R.string.no_themes_description);

        final RecyclerView recycler = (RecyclerView) view.findViewById(R.id.list);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.addItemDecoration(new LineDividerItemDecoration(getContext()));
        recycler.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ThemesPackagesAdapter(getContext(), new ThemesPackagesAdapter.ThemeClickListener() {
            @Override
            public void onThemeClick(Theme theme) {
                Intent intent1 = new Intent("projekt.substratum.THEME");
                intent1.putExtra("certified", true);
                intent1.putExtra("hash_passthrough", "");
                intent1.putExtra("theme_legacy", false);
                intent1.putExtra("theme_mode", "");
                intent1.setPackage(theme.packageName);
                mPendingTheme = theme;
                startActivityForResult(intent1, 101);
            }
        });
        recycler.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<List<Theme>> onCreateLoader(int id, Bundle args) {
        return new ThemePackagesLoader(getContext());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            if (data == null) return;

            byte[] decKey = data.getByteArrayExtra("encryption_key");
            byte[] ivKey = data.getByteArrayExtra("iv_encrypt_key");

            final Intent intent = new Intent(App.getInstance().getApplicationContext(),
                    ThemeContentActivity.class);
            intent.putExtra("encryption_key", decKey);
            intent.putExtra("iv_encrypt_key", ivKey);
            intent.putExtra(Broadcast.EXTRA_THEME_PACKAGE, mPendingTheme.packageName);
            intent.putExtra(Broadcast.EXTRA_BACKEND_NAME, mPendingTheme.backendName);
            ActivityCompat.startActivity(App.getInstance().getApplicationContext(), intent, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Theme>> loader, List<Theme> data) {
        mAdapter.setData(data);
        mSwipeRefreshLayout.setRefreshing(false);
        mHandler.postDelayed(mEmptyViewRunnable, 1000);
    }

    @Override
    public void onLoaderReset(Loader<List<Theme>> loader) {

    }

    private static class ThemePackagesLoader extends AsyncTaskLoader<List<Theme>> {
        private BroadcastReceiver mReceiver;

        ThemePackagesLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            if (mReceiver == null) {
                mReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        // loader should reload its data on backend connect/disconnect
                        onContentChanged();
                    }
                };
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver,
                        Broadcast.getBackendConnectFilter());
            }

            forceLoad();
        }

        @Override
        public List<Theme> loadInBackground() {
            final List<Theme> result = new ArrayList<>();

            for (ComponentName backendName : App.getInstance().getBackendNames()) {
                final IThemeService backend = App.getInstance().getBackend(backendName);
                try {
                    backend.getThemePackages(result);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

            return result;
        }

        @Override
        protected void onReset() {
            if (mReceiver != null) {
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
                mReceiver = null;
            }
        }
    }
}
