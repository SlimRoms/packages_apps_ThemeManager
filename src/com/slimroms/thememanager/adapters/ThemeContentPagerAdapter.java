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

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.ArrayMap;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.themecore.Theme;

import com.slimroms.thememanager.R;
import com.slimroms.thememanager.fragments.BootAnimationGroupFragment;
import com.slimroms.thememanager.fragments.FontGroupFragment;
import com.slimroms.thememanager.fragments.OverlayGroupFragment;
import com.slimroms.thememanager.fragments.SoundsGroupFragment;
import com.slimroms.thememanager.fragments.WallpaperGroupFragment;

import java.util.ArrayList;
import java.util.Collections;

public class ThemeContentPagerAdapter extends FragmentPagerAdapter {
    private OverlayThemeInfo mOverlayInfo;
    private Theme mTheme;

    private ArrayList<String> mKeys = new ArrayList<>();
    private ArrayMap<String, String> mTitles = new ArrayMap<>();

    public ThemeContentPagerAdapter(FragmentManager fm,
                                    OverlayThemeInfo overlayInfo, Theme theme, Context context) {
        super(fm);
        mOverlayInfo = overlayInfo;
        mTheme = theme;

        mKeys.addAll(mOverlayInfo.groups.keySet());
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
        final OverlayGroup group = mOverlayInfo.groups.get(key);
        switch (key) {
            case OverlayGroup.WALLPAPERS:
                //page with wallpapers
                return WallpaperGroupFragment.newInstance(group);
            case OverlayGroup.BOOTANIMATIONS:
                // page with boot animations
                return BootAnimationGroupFragment.newInstance(group);
            case OverlayGroup.FONTS:
                // page with fonts
                return FontGroupFragment.newInstance(group);
            case OverlayGroup.SOUNDS:
                return SoundsGroupFragment.newInstance(group);
            default:
                // page with overlays
                return OverlayGroupFragment.newInstance(group,
                        (mTheme != null) ? mTheme.themeVersion : null,
                        (mTheme != null) ? mTheme.themeVersionCode : 0);
        }
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
