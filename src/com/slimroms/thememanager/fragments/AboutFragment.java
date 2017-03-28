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

import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.slimroms.thememanager.App;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.adapters.AboutAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AboutFragment extends Fragment {
    public static final String TAG = AboutFragment.class.getSimpleName();

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView mRecycler = (RecyclerView) view.findViewById(R.id.list);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mRecycler.setHasFixedSize(true);
        final AboutAdapter adapter = new AboutAdapter(getContext(), fillData());
        mRecycler.setAdapter(adapter);
    }

    private List<Object> fillData() {
        final List<Object> result = new ArrayList<>();
        AboutAdapter.Bean bean;

        // show versions
        result.add(AboutAdapter.ITEM_VERSIONS);
        final PackageManager pm = getContext().getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = pm.getPackageInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            bean = new AboutAdapter.Bean();
            bean.name = pm.getApplicationLabel(packageInfo.applicationInfo);
            bean.image = packageInfo.applicationInfo.loadIcon(pm);
            bean.description = String.format(Locale.getDefault(),
                    "%s (%d)", packageInfo.versionName, packageInfo.versionCode);
            bean.signatureCheckResult = App.getInstance().checkSignature(packageInfo.packageName);
            result.add(bean);
        }
        catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }
        for (ComponentName backendName : App.getInstance().getBackendNames()) {
            try {
                packageInfo = pm.getPackageInfo(backendName.getPackageName(), PackageManager.GET_META_DATA);
                bean = new AboutAdapter.Bean();
                bean.name = pm.getApplicationLabel(packageInfo.applicationInfo);
                bean.image = packageInfo.applicationInfo.loadIcon(pm);
                bean.description = String.format(Locale.getDefault(),
                        "%s (%d)", packageInfo.versionName, packageInfo.versionCode);
                bean.signatureCheckResult = App.getInstance().checkSignature(packageInfo.packageName);
                result.add(bean);
            }
            catch (PackageManager.NameNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        // show developers
        result.add(AboutAdapter.ITEM_TEAM);
        bean = new AboutAdapter.Bean();
        bean.name = getString(R.string.about_team_josue);
        bean.description = getString(R.string.about_coordinator);
        result.add(bean);
        bean = new AboutAdapter.Bean();
        bean.name = getString(R.string.about_team_victor);
        bean.description = getString(R.string.about_architect);
        result.add(bean);
        bean = new AboutAdapter.Bean();
        bean.name = getString(R.string.about_team_griffin);
        bean.description = getString(R.string.about_developer);
        result.add(bean);
        bean = new AboutAdapter.Bean();
        bean.name = getString(R.string.about_team_yank);
        bean.description = getString(R.string.about_developer);
        result.add(bean);
        bean = new AboutAdapter.Bean();
        bean.name = getString(R.string.about_team_cristian);
        bean.description = getString(R.string.about_designer);
        result.add(bean);

        // show credits
        result.add(AboutAdapter.ITEM_CREDITS);
        bean = new AboutAdapter.Bean();
        bean.name = getString(R.string.about_credits_austin);
        bean.description = getString(R.string.about_credits_austin_text);
        result.add(bean);

        return result;
    }
}
