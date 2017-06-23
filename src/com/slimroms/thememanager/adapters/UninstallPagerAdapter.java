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
package com.slimroms.thememanager.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.themecore.Theme;

import com.slimroms.thememanager.R;
import com.slimroms.thememanager.fragments.UninstallFragment;

import java.util.ArrayList;
import java.util.Collections;

public class UninstallPagerAdapter extends FragmentPagerAdapter {
    private ArrayMap<Pair<String, ComponentName>, OverlayThemeInfo> mThemes = new ArrayMap<>();
    private Theme mTheme;
    private Context mContext;

    private ArrayList<String> mKeys = new ArrayList<>();
    private ArrayList<UninstallFragment> mFragments = new ArrayList<>();

    public UninstallPagerAdapter(FragmentManager fm,
                                 ArrayMap<Pair<String, ComponentName>, OverlayThemeInfo> themes, Context context) {
        super(fm);
        mContext = context;
        setThemes(themes);
    }

    public void setThemes(ArrayMap<Pair<String, ComponentName>, OverlayThemeInfo> themes) {
        Log.d("TEST", "setThemes");
        mThemes.clear();
        //mThemes.putAll((SimpleArrayMap<Pair<String, ComponentName>, OverlayThemeInfo>) themes);

        //for (Pair<String, ComponentName> pair : themes.keySet()) {
          //  mKeys.add(pair.first);
        //}
        //Collections.sort(mKeys);

        ArrayList<String> keys = new ArrayList<>();
        for (Pair<String, ComponentName> pair : themes.keySet()) {
            keys.add(pair.first);
        }
        for (String key : mKeys) {
            if (!keys.contains(key)) {
                int index = mKeys.indexOf(key);
                mKeys.remove(index);
                mThemes.remove(index);
            }
        }
        for (String key : keys) {
            int index = keys.indexOf(key);
            if (!mKeys.contains(key)) {
                mKeys.add(index, key);
            }
            Pair<String, ComponentName> p = themes.keyAt(index);
            OverlayThemeInfo info = themes.get(p);
            mThemes.put(p, info);
        }

        for (String key : mKeys) {
            OverlayThemeInfo info = getThemeInfoForKey(key);
            if (info != null && !mFragments.isEmpty()) {
                UninstallFragment fragment = mFragments.get(mKeys.indexOf(key));
                if (fragment != null) {
                    fragment.setOverlays(info);
                }
            }
        }
        notifyDataSetChanged();
    }

    private OverlayThemeInfo getThemeInfoForKey(String key) {
        for (Pair<String, ComponentName> pair : mThemes.keySet()) {
            if (pair.first.equals(key)) {
                return mThemes.get(pair);
            }
        }
        return null;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("TEST", "getItem - " + mKeys.get(position));
        final String key = mKeys.get(position);
        OverlayThemeInfo info = new OverlayThemeInfo();
        for (Pair<String, ComponentName> pair : mThemes.keySet()) {
            if (pair.first.equals(key)) {
                info = mThemes.get(pair);
                break;
            }
        }
        if (mFragments.isEmpty() || mFragments.get(position) == null) {
            mFragments.add(position, UninstallFragment.newInstance(info));
        }
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mKeys.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mKeys.get(position);
    }
}