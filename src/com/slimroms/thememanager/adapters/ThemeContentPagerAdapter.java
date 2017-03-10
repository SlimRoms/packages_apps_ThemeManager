/*
 * Copyright (C) 2017 SlimRoms Project
 * Copyright (C) 2017 Victor Lapin
 * Copyright (C) 2017 Griffin Millender
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            default:
                // page with overlays
                return OverlayGroupFragment.newInstance(group, mTheme != null);
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
