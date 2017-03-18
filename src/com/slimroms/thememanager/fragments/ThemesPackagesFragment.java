/*
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

import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import com.slimroms.thememanager.adapters.ThemesPackagesAdapter;
import com.slimroms.thememanager.views.LineDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ThemesPackagesFragment extends Fragment {
    public static final String TAG = ThemesPackagesFragment.class.getSimpleName();

    public static ThemesPackagesFragment newInstance() {
        return new ThemesPackagesFragment();
    }

    private ViewGroup mEmptyView;
    private ThemesPackagesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.nav_themes);
        mEmptyView = (ViewGroup) view.findViewById(R.id.empty_view);
        final TextView emptyViewTitle = (TextView) view.findViewById(R.id.empty_view_title);
        emptyViewTitle.setText(R.string.no_themes_title);
        final TextView emptyViewDescription = (TextView) view.findViewById(R.id.empty_view_description);
        emptyViewDescription.setText(R.string.no_themes_description);
        mEmptyView.setVisibility(View.VISIBLE);

        final RecyclerView recycler = (RecyclerView) view.findViewById(R.id.list);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.addItemDecoration(new LineDividerItemDecoration(getContext()));
        recycler.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ThemesPackagesAdapter(getContext());
        recycler.setAdapter(mAdapter);
    }

    private BroadcastReceiver mConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ComponentName backendName = intent.getParcelableExtra(Broadcast.EXTRA_BACKEND_NAME);
            switch (intent.getAction()) {
                case Broadcast.ACTION_BACKEND_CONNECTED:
                    new AsyncTask<ComponentName, Void, List<Theme>>() {
                        @Override
                        protected List<Theme> doInBackground(ComponentName... componentNames) {
                            if (componentNames[0] != null) {
                                final IThemeService backend = App.getInstance().getBackend(componentNames[0]);
                                try {
                                    final List<Theme> result = new ArrayList<>();
                                    final int count = backend.getThemePackages(result);
                                    return (count > 0) ? result : null;
                                }
                                catch (RemoteException ex) {
                                    ex.printStackTrace();
                                    return null;
                                }
                            }
                            else
                                return null;
                        }

                        @Override
                        protected void onPostExecute(List<Theme> themes) {
                            if (themes != null) {
                                mAdapter.addThemes(themes);
                            }
                            mEmptyView.setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
                        }
                    }.execute(backendName);
                    break;
                case Broadcast.ACTION_BACKEND_DISCONNECTED:
                    mAdapter.removeThemes(backendName);
                    mEmptyView.setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mConnectReceiver,
                Broadcast.getBackendConnectFilter());
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mConnectReceiver);
        super.onStop();
    }
}
