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

import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.themecore.Theme;

import com.slimroms.thememanager.R;
import com.slimroms.thememanager.fragments.UninstallFragment;

import java.util.ArrayList;
import java.util.Collections;

public class UninstallPagerAdapter extends FragmentPagerAdapter {
    private ArrayMap<Pair<String, ComponentName>, OverlayThemeInfo> mThemes;
    private Theme mTheme;

    private ArrayList<String> mKeys = new ArrayList<>();
    private ArrayMap<String, String> mTitles = new ArrayMap<>();

    public UninstallPagerAdapter(FragmentManager fm,
                                 ArrayMap<Pair<String, ComponentName>, OverlayThemeInfo> themes, Theme theme, Context context) {
        super(fm);
        mThemes = themes;
        mTheme = theme;

        for (Pair<String, ComponentName> pair : themes.keySet()) {
            mKeys.add(pair.first);
        }
        Collections.sort(mKeys);
        if (mKeys.contains(OverlayGroup.OVERLAYS)) {
            Collections.swap(mKeys, mKeys.indexOf(OverlayGroup.OVERLAYS), 0);
        }

        for (String key : mKeys) {
            String title;
            switch (key) {
                case OverlayGroup.OVERLAYS:
                    title = context.getString(R.string.group_title_overlays);
                    break;
                case OverlayGroup.FONTS:
                    title = context.getString(R.string.group_title_fonts);
                    break;
                case OverlayGroup.BOOTANIMATIONS:
                    title = context.getString(R.string.group_title_bootanimations);
                    break;
                case OverlayGroup.WALLPAPERS:
                    title = context.getString(R.string.group_title_wallpapers);
                    break;
                default:
                    title = key;
                    break;
            }
            mTitles.put(key, title);
        }
    }

    @Override
    public Fragment getItem(int position) {
        final String key = mKeys.get(position);
        OverlayThemeInfo info = new OverlayThemeInfo();
        for (Pair<String, ComponentName> pair : mThemes.keySet()) {
            if (pair.first.equals(key)) {
                info = mThemes.get(pair);
                break;
            }
        }
        return UninstallFragment.newInstance(info);
    }

    @Override
    public int getCount() {
        return mKeys.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(mKeys.get(position));
    }
}